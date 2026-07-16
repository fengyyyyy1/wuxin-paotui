package com.wuxin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuxin.common.ResultCode;
import com.wuxin.dto.order.CreateOrderDTO;
import com.wuxin.entity.OrderLogEntity;
import com.wuxin.entity.OrderEntity;
import com.wuxin.entity.UserAddressEntity;
import com.wuxin.entity.UserEntity;
import com.wuxin.enums.OrderStatusEnum;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.OrderLogMapper;
import com.wuxin.mapper.OrderMapper;
import com.wuxin.mapper.UserAddressMapper;
import com.wuxin.mapper.UserMapper;
import com.wuxin.service.OrderService;
import com.wuxin.utils.UserContext;
import com.wuxin.vo.CancelOrderVO;
import com.wuxin.vo.ConfirmOrderVO;
import com.wuxin.vo.OrderDetailVO;
import com.wuxin.vo.OrderListVO;
import com.wuxin.vo.PageResultVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderEntity> implements OrderService {

    private static final String OPERATOR_TYPE_USER = "USER";

    private static final String CONFIRM_STATUS_ERROR_MESSAGE = "\u5f53\u524d\u8ba2\u5355\u72b6\u6001\u4e0d\u53ef\u786e\u8ba4\u6536\u8d27";

    private static final DateTimeFormatter ORDER_NO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static final int MAX_ORDER_NO_RETRY = 5;

    private final UserMapper userMapper;

    private final UserAddressMapper userAddressMapper;

    private final OrderLogMapper orderLogMapper;

    public OrderServiceImpl(UserMapper userMapper, UserAddressMapper userAddressMapper, OrderLogMapper orderLogMapper) {
        this.userMapper = userMapper;
        this.userAddressMapper = userAddressMapper;
        this.orderLogMapper = orderLogMapper;
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
}
