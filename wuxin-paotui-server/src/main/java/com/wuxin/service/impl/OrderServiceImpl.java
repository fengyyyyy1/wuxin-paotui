package com.wuxin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuxin.common.ResultCode;
import com.wuxin.dto.order.CreateOrderDTO;
import com.wuxin.dto.order.CommentOrderDTO;
import com.wuxin.entity.OrderCommentEntity;
import com.wuxin.entity.OrderLogEntity;
import com.wuxin.entity.OrderEntity;
import com.wuxin.entity.UserAddressEntity;
import com.wuxin.entity.UserEntity;
import com.wuxin.enums.OrderStatusEnum;
import com.wuxin.enums.PaymentStatusEnum;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.OrderLogMapper;
import com.wuxin.mapper.OrderCommentMapper;
import com.wuxin.mapper.OrderMapper;
import com.wuxin.mapper.UserAddressMapper;
import com.wuxin.mapper.UserMapper;
import com.wuxin.service.OrderService;
import com.wuxin.utils.UserContext;
import com.wuxin.vo.CancelOrderVO;
import com.wuxin.vo.ConfirmOrderVO;
import com.wuxin.vo.CommentOrderVO;
import com.wuxin.vo.OrderDetailVO;
import com.wuxin.vo.OrderListVO;
import com.wuxin.vo.OrderTimelineItemVO;
import com.wuxin.vo.OrderTimelineVO;
import com.wuxin.vo.PayOrderVO;
import com.wuxin.vo.PageResultVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderEntity> implements OrderService {

    private static final String OPERATOR_TYPE_USER = "USER";

    private static final String OPERATOR_TYPE_RIDER = "RIDER";

    private static final String CONFIRM_STATUS_ERROR_MESSAGE = "\u5f53\u524d\u8ba2\u5355\u72b6\u6001\u4e0d\u53ef\u786e\u8ba4\u6536\u8d27";

    private static final DateTimeFormatter ORDER_NO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final int MAX_ORDER_NO_RETRY = 5;

    private static final int MAX_PAYMENT_NO_RETRY = 5;

    private final UserMapper userMapper;

    private final UserAddressMapper userAddressMapper;

    private final OrderLogMapper orderLogMapper;

    private final OrderCommentMapper orderCommentMapper;

    public OrderServiceImpl(UserMapper userMapper, UserAddressMapper userAddressMapper,
                            OrderLogMapper orderLogMapper, OrderCommentMapper orderCommentMapper) {
        this.userMapper = userMapper;
        this.userAddressMapper = userAddressMapper;
        this.orderLogMapper = orderLogMapper;
        this.orderCommentMapper = orderCommentMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(CreateOrderDTO createOrderDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        validateAddress(createOrderDTO.getPickupAddressId(), userId, "pickup address does not exist");
        validateAddress(createOrderDTO.getDeliveryAddressId(), userId, "delivery address does not exist");

        for (int retry = 0; retry < MAX_ORDER_NO_RETRY; retry++) {
            OrderEntity orderEntity = buildOrderEntity(createOrderDTO, userId, generateOrderNo());
            try {
                boolean saved = save(orderEntity);
                if (saved && orderEntity.getId() != null) {
                    return orderEntity.getId();
                }
            } catch (DuplicateKeyException exception) {
                if (retry < MAX_ORDER_NO_RETRY - 1) {
                    continue;
                }
                throw new BusinessException(ResultCode.FAIL, "order save failed");
            }
        }

        throw new BusinessException(ResultCode.FAIL, "order save failed");
    }

    @Override
    public PageResultVO<OrderListVO> getMyOrders(Integer pageNum, Integer pageSize, Integer status) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        long safePageNum = normalizePageNum(pageNum);
        long safePageSize = normalizePageSize(pageSize);

        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getUserId, userId)
                .eq(OrderEntity::getDeleted, 0)
                .eq(status != null, OrderEntity::getStatus, status)
                .orderByDesc(OrderEntity::getCreateTime);

        Page<OrderEntity> page = page(new Page<>(safePageNum, safePageSize), queryWrapper);
        List<OrderListVO> records = page.getRecords().stream()
                .map(this::toOrderListVO)
                .toList();

        PageResultVO<OrderListVO> pageResultVO = new PageResultVO<>();
        pageResultVO.setRecords(records);
        pageResultVO.setTotal(page.getTotal());
        pageResultVO.setPageNum(safePageNum);
        pageResultVO.setPageSize(safePageSize);
        pageResultVO.setPages(page.getPages());
        return pageResultVO;
    }

    @Override
    public OrderDetailVO getOrderDetail(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (id == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getId, id)
                .eq(OrderEntity::getUserId, userId)
                .eq(OrderEntity::getDeleted, 0);

        OrderEntity orderEntity = getOne(queryWrapper, false);
        if (orderEntity == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }

        return toOrderDetailVO(orderEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfirmOrderVO confirmOrder(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<OrderEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OrderEntity::getId, id)
                .eq(OrderEntity::getUserId, userId)
                .eq(OrderEntity::getStatus, OrderStatusEnum.WAITING_CONFIRM.getCode())
                .eq(OrderEntity::getDeleted, 0)
                .set(OrderEntity::getStatus, OrderStatusEnum.COMPLETED.getCode())
                .set(OrderEntity::getUpdateTime, now);

        int affectedRows = getBaseMapper().update(null, updateWrapper);
        if (affectedRows != 1) {
            handleConfirmFailed(id, userId);
        }

        OrderLogEntity orderLog = new OrderLogEntity();
        orderLog.setOrderId(id);
        orderLog.setOldStatus(OrderStatusEnum.WAITING_CONFIRM.getCode());
        orderLog.setNewStatus(OrderStatusEnum.COMPLETED.getCode());
        orderLog.setOperatorId(userId);
        orderLog.setOperatorType(OPERATOR_TYPE_USER);
        orderLog.setRemark("\u7528\u6237\u786e\u8ba4\u6536\u8d27");
        orderLog.setCreateTime(now);
        int insertedRows = orderLogMapper.insert(orderLog);
        if (insertedRows != 1) {
            throw new BusinessException(ResultCode.FAIL, "order log save failed");
        }

        ConfirmOrderVO confirmOrderVO = new ConfirmOrderVO();
        confirmOrderVO.setOrderId(id);
        confirmOrderVO.setStatus(OrderStatusEnum.COMPLETED.getCode());
        confirmOrderVO.setStatusText(OrderStatusEnum.COMPLETED.getText());
        confirmOrderVO.setConfirmTime(now);
        return confirmOrderVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CancelOrderVO cancelOrder(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<OrderEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OrderEntity::getId, id)
                .eq(OrderEntity::getUserId, userId)
                .eq(OrderEntity::getStatus, OrderStatusEnum.WAITING_ACCEPT.getCode())
                .eq(OrderEntity::getDeleted, 0)
                .set(OrderEntity::getStatus, OrderStatusEnum.CANCELLED.getCode())
                .set(OrderEntity::getUpdateTime, now);

        int affectedRows = getBaseMapper().update(null, updateWrapper);
        if (affectedRows != 1) {
            handleCancelFailed(id, userId);
        }

        OrderLogEntity orderLog = new OrderLogEntity();
        orderLog.setOrderId(id);
        orderLog.setOldStatus(OrderStatusEnum.WAITING_ACCEPT.getCode());
        orderLog.setNewStatus(OrderStatusEnum.CANCELLED.getCode());
        orderLog.setOperatorId(userId);
        orderLog.setOperatorType(OPERATOR_TYPE_USER);
        orderLog.setRemark("用户取消订单");
        orderLog.setCreateTime(now);
        int insertedRows = orderLogMapper.insert(orderLog);
        if (insertedRows != 1) {
            throw new IllegalStateException("order log save failed");
        }

        CancelOrderVO cancelOrderVO = new CancelOrderVO();
        cancelOrderVO.setOrderId(id);
        cancelOrderVO.setStatus(OrderStatusEnum.CANCELLED.getCode());
        cancelOrderVO.setStatusText(OrderStatusEnum.CANCELLED.getText());
        cancelOrderVO.setCancelTime(now);
        return cancelOrderVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentOrderVO commentOrder(CommentOrderDTO commentOrderDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        LambdaQueryWrapper<OrderEntity> orderQueryWrapper = new LambdaQueryWrapper<>();
        orderQueryWrapper.eq(OrderEntity::getId, commentOrderDTO.getOrderId())
                .eq(OrderEntity::getUserId, userId)
                .eq(OrderEntity::getDeleted, 0)
                .last("LIMIT 1");

        OrderEntity orderEntity = getOne(orderQueryWrapper, false);
        if (orderEntity == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        if (!OrderStatusEnum.COMPLETED.getCode().equals(orderEntity.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_CANNOT_COMMENT);
        }

        LocalDateTime now = LocalDateTime.now();
        OrderCommentEntity orderComment = new OrderCommentEntity();
        orderComment.setOrderId(orderEntity.getId());
        orderComment.setUserId(userId);
        orderComment.setRiderId(orderEntity.getRiderId());
        orderComment.setScore(commentOrderDTO.getScore());
        orderComment.setContent(commentOrderDTO.getContent());
        orderComment.setIsAnonymous(commentOrderDTO.getAnonymous() == null ? 0 : commentOrderDTO.getAnonymous());
        orderComment.setCreateTime(now);
        orderComment.setUpdateTime(now);
        orderComment.setIsDeleted(0);

        try {
            int insertedRows = orderCommentMapper.insert(orderComment);
            if (insertedRows != 1 || orderComment.getId() == null) {
                throw new IllegalStateException("order comment save failed");
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ResultCode.ORDER_ALREADY_COMMENTED);
        }

        OrderLogEntity orderLog = new OrderLogEntity();
        orderLog.setOrderId(orderEntity.getId());
        orderLog.setOldStatus(OrderStatusEnum.COMPLETED.getCode());
        orderLog.setNewStatus(OrderStatusEnum.COMPLETED.getCode());
        orderLog.setOperatorId(userId);
        orderLog.setOperatorType(OPERATOR_TYPE_USER);
        orderLog.setRemark("用户评价订单");
        orderLog.setCreateTime(now);
        int insertedLogRows = orderLogMapper.insert(orderLog);
        if (insertedLogRows != 1) {
            throw new IllegalStateException("order log save failed");
        }

        CommentOrderVO commentOrderVO = new CommentOrderVO();
        commentOrderVO.setCommentId(orderComment.getId());
        commentOrderVO.setOrderId(orderEntity.getId());
        commentOrderVO.setScore(orderComment.getScore());
        commentOrderVO.setCommentTime(now);
        return commentOrderVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayOrderVO payOrder(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        OrderEntity orderEntity = getUserOrder(id, userId);
        if (PaymentStatusEnum.PAID.getCode().equals(orderEntity.getPayStatus())) {
            throw new BusinessException(ResultCode.ORDER_ALREADY_PAID);
        }
        if (!OrderStatusEnum.WAITING_ACCEPT.getCode().equals(orderEntity.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_CANNOT_PAY);
        }

        LocalDateTime now = LocalDateTime.now();
        String paymentNo = updateOrderPayment(id, userId, now);

        OrderLogEntity orderLog = new OrderLogEntity();
        orderLog.setOrderId(id);
        orderLog.setOldStatus(OrderStatusEnum.WAITING_ACCEPT.getCode());
        orderLog.setNewStatus(OrderStatusEnum.WAITING_ACCEPT.getCode());
        orderLog.setOperatorId(userId);
        orderLog.setOperatorType(OPERATOR_TYPE_USER);
        orderLog.setRemark("用户模拟支付订单");
        orderLog.setCreateTime(now);
        int insertedRows = orderLogMapper.insert(orderLog);
        if (insertedRows != 1) {
            throw new IllegalStateException("order log save failed");
        }

        PayOrderVO payOrderVO = new PayOrderVO();
        payOrderVO.setOrderId(id);
        payOrderVO.setPaymentNo(paymentNo);
        payOrderVO.setPayStatus(PaymentStatusEnum.PAID.getCode());
        payOrderVO.setPayStatusText(PaymentStatusEnum.PAID.getText());
        payOrderVO.setAmount(orderEntity.getPrice());
        payOrderVO.setPayTime(now);
        return payOrderVO;
    }

    @Override
    public OrderTimelineVO getOrderTimeline(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        OrderEntity orderEntity = getUserOrder(id, userId);
        List<OrderTimelineItemVO> timeline = new ArrayList<>();

        addTimelineItem(timeline, "ORDER_CREATED", "订单已创建", "订单创建成功", orderEntity.getCreateTime());
        if (PaymentStatusEnum.PAID.getCode().equals(orderEntity.getPayStatus())) {
            addTimelineItem(timeline, "ORDER_PAID", "支付成功", "订单支付成功", orderEntity.getPayTime());
        }
        addTimelineItem(timeline, "RIDER_ACCEPTED", "骑手已接单", "骑手接单成功", orderEntity.getAcceptTime());
        addTimelineItem(timeline, "DELIVERY_FINISHED", "骑手已完成配送", "订单配送完成", orderEntity.getFinishTime());

        LambdaQueryWrapper<OrderLogEntity> logQueryWrapper = new LambdaQueryWrapper<>();
        logQueryWrapper.eq(OrderLogEntity::getOrderId, id)
                .orderByAsc(OrderLogEntity::getCreateTime);
        List<OrderLogEntity> orderLogs = orderLogMapper.selectList(logQueryWrapper);
        for (OrderLogEntity orderLog : orderLogs) {
            appendLogTimelineItem(timeline, orderLog);
        }

        LambdaQueryWrapper<OrderCommentEntity> commentQueryWrapper = new LambdaQueryWrapper<>();
        commentQueryWrapper.eq(OrderCommentEntity::getOrderId, id)
                .eq(OrderCommentEntity::getIsDeleted, 0)
                .orderByAsc(OrderCommentEntity::getCreateTime);
        List<OrderCommentEntity> orderComments = orderCommentMapper.selectList(commentQueryWrapper);
        for (OrderCommentEntity orderComment : orderComments) {
            addTimelineItem(timeline, "ORDER_COMMENTED", "用户已评价",
                    "评分：" + orderComment.getScore() + "分", orderComment.getCreateTime());
        }

        timeline.sort(Comparator.comparing(OrderTimelineItemVO::getTime));
        for (int index = 0; index < timeline.size(); index++) {
            timeline.get(index).setSort(index + 1);
        }

        OrderTimelineVO orderTimelineVO = new OrderTimelineVO();
        orderTimelineVO.setOrderId(orderEntity.getId());
        orderTimelineVO.setOrderNo(orderEntity.getOrderNo());
        orderTimelineVO.setStatus(orderEntity.getStatus());
        orderTimelineVO.setStatusText(OrderStatusEnum.getTextByCode(orderEntity.getStatus()));
        orderTimelineVO.setPayStatus(orderEntity.getPayStatus());
        orderTimelineVO.setPayStatusText(PaymentStatusEnum.getTextByCode(orderEntity.getPayStatus()));
        orderTimelineVO.setTimeline(timeline);
        return orderTimelineVO;
    }

    private void handleConfirmFailed(Long id, Long userId) {
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getId, id)
                .eq(OrderEntity::getUserId, userId)
                .eq(OrderEntity::getDeleted, 0)
                .last("LIMIT 1");

        OrderEntity orderEntity = getOne(queryWrapper, false);
        if (orderEntity == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, CONFIRM_STATUS_ERROR_MESSAGE);
    }

    private void handleCancelFailed(Long id, Long userId) {
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getId, id)
                .eq(OrderEntity::getUserId, userId)
                .eq(OrderEntity::getDeleted, 0)
                .last("LIMIT 1");

        OrderEntity orderEntity = getOne(queryWrapper, false);
        if (orderEntity == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        throw new BusinessException(ResultCode.ORDER_STATUS_CANNOT_CANCEL);
    }

    private OrderEntity getUserOrder(Long id, Long userId) {
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getId, id)
                .eq(OrderEntity::getUserId, userId)
                .eq(OrderEntity::getDeleted, 0)
                .last("LIMIT 1");

        OrderEntity orderEntity = getOne(queryWrapper, false);
        if (orderEntity == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        return orderEntity;
    }

    private String updateOrderPayment(Long id, Long userId, LocalDateTime now) {
        for (int retry = 0; retry < MAX_PAYMENT_NO_RETRY; retry++) {
            String paymentNo = generatePaymentNo(now);
            LambdaUpdateWrapper<OrderEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(OrderEntity::getId, id)
                    .eq(OrderEntity::getUserId, userId)
                    .eq(OrderEntity::getPayStatus, PaymentStatusEnum.UNPAID.getCode())
                    .eq(OrderEntity::getStatus, OrderStatusEnum.WAITING_ACCEPT.getCode())
                    .eq(OrderEntity::getDeleted, 0)
                    .set(OrderEntity::getPayStatus, PaymentStatusEnum.PAID.getCode())
                    .set(OrderEntity::getPayTime, now)
                    .set(OrderEntity::getPaymentNo, paymentNo)
                    .set(OrderEntity::getUpdateTime, now);

            try {
                int affectedRows = getBaseMapper().update(null, updateWrapper);
                if (affectedRows == 1) {
                    return paymentNo;
                }
                handlePayFailed(id, userId);
            } catch (DuplicateKeyException exception) {
                if (retry == MAX_PAYMENT_NO_RETRY - 1) {
                    throw new IllegalStateException("payment number generation failed", exception);
                }
            }
        }
        throw new IllegalStateException("payment number generation failed");
    }

    private void handlePayFailed(Long id, Long userId) {
        OrderEntity orderEntity = getUserOrder(id, userId);
        if (PaymentStatusEnum.PAID.getCode().equals(orderEntity.getPayStatus())) {
            throw new BusinessException(ResultCode.ORDER_ALREADY_PAID);
        }
        throw new BusinessException(ResultCode.ORDER_STATUS_CANNOT_PAY);
    }

    private void appendLogTimelineItem(List<OrderTimelineItemVO> timeline, OrderLogEntity orderLog) {
        if (matchesOrderLog(orderLog, OrderStatusEnum.WAITING_CONFIRM.getCode(),
                OrderStatusEnum.COMPLETED.getCode(), OPERATOR_TYPE_USER, "用户确认收货")) {
            addTimelineItem(timeline, "ORDER_CONFIRMED", "用户已确认收货",
                    "用户确认收货", orderLog.getCreateTime());
            return;
        }
        if (matchesOrderLog(orderLog, OrderStatusEnum.WAITING_ACCEPT.getCode(),
                OrderStatusEnum.CANCELLED.getCode(), OPERATOR_TYPE_USER, "用户取消订单")) {
            addTimelineItem(timeline, "ORDER_CANCELLED", "订单已取消",
                    "用户取消订单", orderLog.getCreateTime());
            return;
        }
        if (matchesOrderLog(orderLog, OrderStatusEnum.ACCEPTED.getCode(),
                OrderStatusEnum.WAITING_ACCEPT.getCode(), OPERATOR_TYPE_RIDER, "骑手放弃订单")) {
            addTimelineItem(timeline, "RIDER_GAVE_UP", "骑手已放弃订单",
                    "骑手放弃订单", orderLog.getCreateTime());
        }
    }

    private boolean matchesOrderLog(OrderLogEntity orderLog, Integer oldStatus, Integer newStatus,
                                    String operatorType, String remark) {
        return oldStatus.equals(orderLog.getOldStatus())
                && newStatus.equals(orderLog.getNewStatus())
                && operatorType.equals(orderLog.getOperatorType())
                && remark.equals(orderLog.getRemark());
    }

    private void addTimelineItem(List<OrderTimelineItemVO> timeline, String type, String title,
                                 String description, LocalDateTime time) {
        if (time == null) {
            return;
        }
        OrderTimelineItemVO timelineItem = new OrderTimelineItemVO();
        timelineItem.setType(type);
        timelineItem.setTitle(title);
        timelineItem.setDescription(description);
        timelineItem.setTime(time);
        timeline.add(timelineItem);
    }

    private void validateAddress(Long addressId, Long userId, String notExistMessage) {
        UserAddressEntity address = userAddressMapper.selectByIdIncludeDeleted(addressId);
        if (address == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, notExistMessage);
        }
        if (!userId.equals(address.getUserId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "address does not belong to current user");
        }
        if (Integer.valueOf(1).equals(address.getIsDeleted())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "address has been deleted");
        }
    }

    private OrderEntity buildOrderEntity(CreateOrderDTO createOrderDTO, Long userId, String orderNo) {
        LocalDateTime now = LocalDateTime.now();

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderNo(orderNo);
        orderEntity.setUserId(userId);
        orderEntity.setRiderId(null);
        orderEntity.setPickupAddressId(createOrderDTO.getPickupAddressId());
        orderEntity.setDeliveryAddressId(createOrderDTO.getDeliveryAddressId());
        orderEntity.setGoodsName(createOrderDTO.getGoodsName());
        orderEntity.setGoodsDescription(createOrderDTO.getGoodsDescription());
        orderEntity.setWeight(createOrderDTO.getWeight());
        orderEntity.setDistance(createOrderDTO.getDistance());
        orderEntity.setPrice(createOrderDTO.getPrice());
        orderEntity.setStatus(OrderStatusEnum.WAITING_ACCEPT.getCode());
        orderEntity.setPayStatus(PaymentStatusEnum.UNPAID.getCode());
        orderEntity.setPayTime(null);
        orderEntity.setPaymentNo(null);
        orderEntity.setRemark(createOrderDTO.getRemark());
        orderEntity.setCreateTime(now);
        orderEntity.setUpdateTime(now);
        orderEntity.setDeleted(0);
        return orderEntity;
    }

    private OrderListVO toOrderListVO(OrderEntity orderEntity) {
        OrderListVO orderListVO = new OrderListVO();
        orderListVO.setId(orderEntity.getId());
        orderListVO.setOrderNo(orderEntity.getOrderNo());
        orderListVO.setPickupAddressId(orderEntity.getPickupAddressId());
        orderListVO.setDeliveryAddressId(orderEntity.getDeliveryAddressId());
        orderListVO.setGoodsName(orderEntity.getGoodsName());
        orderListVO.setGoodsDescription(orderEntity.getGoodsDescription());
        orderListVO.setWeight(orderEntity.getWeight());
        orderListVO.setDistance(orderEntity.getDistance());
        orderListVO.setPrice(orderEntity.getPrice());
        orderListVO.setStatus(orderEntity.getStatus());
        orderListVO.setStatusText(getStatusText(orderEntity.getStatus()));
        orderListVO.setPayStatus(orderEntity.getPayStatus());
        orderListVO.setPayStatusText(PaymentStatusEnum.getTextByCode(orderEntity.getPayStatus()));
        orderListVO.setPayTime(orderEntity.getPayTime());
        orderListVO.setPaymentNo(orderEntity.getPaymentNo());
        orderListVO.setRemark(orderEntity.getRemark());
        orderListVO.setCreateTime(orderEntity.getCreateTime());
        orderListVO.setUpdateTime(orderEntity.getUpdateTime());
        return orderListVO;
    }

    private OrderDetailVO toOrderDetailVO(OrderEntity orderEntity) {
        OrderDetailVO orderDetailVO = new OrderDetailVO();
        orderDetailVO.setId(orderEntity.getId());
        orderDetailVO.setOrderNo(orderEntity.getOrderNo());
        orderDetailVO.setPickupAddressId(orderEntity.getPickupAddressId());
        orderDetailVO.setDeliveryAddressId(orderEntity.getDeliveryAddressId());
        orderDetailVO.setGoodsName(orderEntity.getGoodsName());
        orderDetailVO.setGoodsDescription(orderEntity.getGoodsDescription());
        orderDetailVO.setWeight(orderEntity.getWeight());
        orderDetailVO.setDistance(orderEntity.getDistance());
        orderDetailVO.setPrice(orderEntity.getPrice());
        orderDetailVO.setStatus(orderEntity.getStatus());
        orderDetailVO.setStatusText(getStatusText(orderEntity.getStatus()));
        orderDetailVO.setPayStatus(orderEntity.getPayStatus());
        orderDetailVO.setPayStatusText(PaymentStatusEnum.getTextByCode(orderEntity.getPayStatus()));
        orderDetailVO.setPayTime(orderEntity.getPayTime());
        orderDetailVO.setPaymentNo(orderEntity.getPaymentNo());
        orderDetailVO.setRemark(orderEntity.getRemark());
        orderDetailVO.setCreateTime(orderEntity.getCreateTime());
        orderDetailVO.setUpdateTime(orderEntity.getUpdateTime());
        return orderDetailVO;
    }

    private String getStatusText(Integer status) {
        return OrderStatusEnum.getTextByCode(status);
    }

    private long normalizePageNum(Integer pageNum) {
        if (pageNum == null || pageNum < 1) {
            return 1L;
        }
        return pageNum.longValue();
    }

    private long normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10L;
        }
        return Math.min(pageSize.longValue(), 50L);
    }

    private String generateOrderNo() {
        int randomNumber = ThreadLocalRandom.current().nextInt(0, 1_000_000);
        return "WX" + LocalDateTime.now().format(ORDER_NO_TIME_FORMATTER) + String.format("%06d", randomNumber);
    }

    private String generatePaymentNo(LocalDateTime payTime) {
        int randomNumber = ThreadLocalRandom.current().nextInt(0, 1_000_000);
        return "PAY" + payTime.format(ORDER_NO_TIME_FORMATTER) + String.format("%06d", randomNumber);
    }
}
