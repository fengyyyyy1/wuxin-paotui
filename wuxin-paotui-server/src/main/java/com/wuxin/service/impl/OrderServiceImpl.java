package com.wuxin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuxin.common.ResultCode;
import com.wuxin.dto.order.CreateOrderDTO;
import com.wuxin.dto.order.CommentOrderDTO;
import com.wuxin.dto.order.CreateCartOrderDTO;
import com.wuxin.dto.order.SettlementPreviewDTO;
import com.wuxin.entity.OrderCommentEntity;
import com.wuxin.entity.OrderLogEntity;
import com.wuxin.entity.OrderEntity;
import com.wuxin.entity.OrderItemEntity;
import com.wuxin.entity.UserAddressEntity;
import com.wuxin.entity.UserEntity;
import com.wuxin.enums.OrderStatusEnum;
import com.wuxin.enums.OrderTypeEnum;
import com.wuxin.enums.BusinessStatusEnum;
import com.wuxin.enums.CategoryStatusEnum;
import com.wuxin.enums.MerchantAuditStatusEnum;
import com.wuxin.enums.MerchantStatusEnum;
import com.wuxin.enums.PaymentStatusEnum;
import com.wuxin.enums.ProductStatusEnum;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.OrderLogMapper;
import com.wuxin.mapper.OrderCommentMapper;
import com.wuxin.mapper.OrderMapper;
import com.wuxin.mapper.OrderItemMapper;
import com.wuxin.mapper.MerchantProductMapper;
import com.wuxin.mapper.MerchantStoreMapper;
import com.wuxin.mapper.ShoppingCartMapper;
import com.wuxin.mapper.UserAddressMapper;
import com.wuxin.mapper.UserMapper;
import com.wuxin.service.OrderService;
import com.wuxin.utils.UserContext;
import com.wuxin.vo.CancelOrderVO;
import com.wuxin.vo.ConfirmOrderVO;
import com.wuxin.vo.CommentOrderVO;
import com.wuxin.vo.CreateCartOrderVO;
import com.wuxin.vo.CartItemQueryVO;
import com.wuxin.vo.OrderDetailVO;
import com.wuxin.vo.OrderListVO;
import com.wuxin.vo.OrderItemVO;
import com.wuxin.vo.OrderTimelineItemVO;
import com.wuxin.vo.OrderTimelineVO;
import com.wuxin.vo.PayOrderVO;
import com.wuxin.vo.PageResultVO;
import com.wuxin.vo.SettlementItemVO;
import com.wuxin.vo.SettlementPreviewVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    private static final int NOT_DELETED = 0;

    private static final BigDecimal ZERO_AMOUNT = new BigDecimal("0.00");

    private final UserMapper userMapper;

    private final UserAddressMapper userAddressMapper;

    private final OrderLogMapper orderLogMapper;

    private final OrderCommentMapper orderCommentMapper;

    private final OrderItemMapper orderItemMapper;

    private final ShoppingCartMapper shoppingCartMapper;

    private final MerchantProductMapper merchantProductMapper;

    private final MerchantStoreMapper merchantStoreMapper;

    public OrderServiceImpl(UserMapper userMapper, UserAddressMapper userAddressMapper,
                            OrderLogMapper orderLogMapper, OrderCommentMapper orderCommentMapper,
                            OrderItemMapper orderItemMapper, ShoppingCartMapper shoppingCartMapper,
                            MerchantProductMapper merchantProductMapper,
                            MerchantStoreMapper merchantStoreMapper) {
        this.userMapper = userMapper;
        this.userAddressMapper = userAddressMapper;
        this.orderLogMapper = orderLogMapper;
        this.orderCommentMapper = orderCommentMapper;
        this.orderItemMapper = orderItemMapper;
        this.shoppingCartMapper = shoppingCartMapper;
        this.merchantProductMapper = merchantProductMapper;
        this.merchantStoreMapper = merchantStoreMapper;
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
    public SettlementPreviewVO previewSettlement(SettlementPreviewDTO settlementPreviewDTO) {
        Long userId = getCurrentUserId();
        SettlementContext settlement = prepareSettlement(
                userId, settlementPreviewDTO.getDeliveryAddressId());
        return toSettlementPreviewVO(settlement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateCartOrderVO createOrderFromCart(CreateCartOrderDTO createCartOrderDTO) {
        Long userId = getCurrentUserId();
        lockUser(userId);
        SettlementContext settlement = prepareSettlement(
                userId, createCartOrderDTO.getDeliveryAddressId());
        LocalDateTime now = LocalDateTime.now();

        OrderEntity order = insertProductOrder(userId, createCartOrderDTO, settlement, now);
        List<OrderItemEntity> orderItems = new ArrayList<>(settlement.cartItems().size());
        for (CartItemQueryVO cartItem : settlement.cartItems()) {
            int affectedRows = merchantProductMapper.deductStock(
                    cartItem.getProductId(), settlement.storeId(), cartItem.getQuantity(),
                    cartItem.getPrice(), now);
            if (affectedRows != 1) {
                handleStockDeductionFailed(cartItem);
            }
            orderItems.add(buildOrderItem(order.getId(), cartItem, now));
        }

        if (orderItemMapper.insertBatch(orderItems) != orderItems.size()) {
            throw new BusinessException(ResultCode.ORDER_CREATE_FAILED);
        }

        insertProductOrderLog(order.getId(), userId, now);

        int deletedRows = shoppingCartMapper.logicalDeleteSelected(userId, now);
        if (deletedRows != settlement.cartItems().size()) {
            throw new BusinessException(ResultCode.SETTLEMENT_CHANGED);
        }

        return toCreateCartOrderVO(order, settlement);
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

    private SettlementContext prepareSettlement(Long userId, Long deliveryAddressId) {
        validateSettlementAddress(deliveryAddressId, userId);
        List<CartItemQueryVO> cartItems = shoppingCartMapper.selectSelectedCartItems(userId);
        if (cartItems.isEmpty()) {
            throw new BusinessException(ResultCode.CART_NO_SELECTED_PRODUCT);
        }

        Long storeId = cartItems.get(0).getStoreId();
        String storeName = cartItems.get(0).getStoreName();
        List<SettlementItemVO> settlementItems = new ArrayList<>(cartItems.size());
        BigDecimal productAmount = ZERO_AMOUNT;
        long selectedProductCount = 0L;

        for (CartItemQueryVO cartItem : cartItems) {
            if (storeId == null || !storeId.equals(cartItem.getStoreId())) {
                throw new BusinessException(ResultCode.CART_STORE_CONFLICT);
            }
            validateSettlementItem(cartItem);
            BigDecimal subtotal = money(
                    cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            productAmount = productAmount.add(subtotal);
            selectedProductCount = Math.addExact(
                    selectedProductCount, cartItem.getQuantity().longValue());

            SettlementItemVO settlementItem = new SettlementItemVO();
            settlementItem.setProductId(cartItem.getProductId());
            settlementItem.setProductName(cartItem.getProductName());
            settlementItem.setProductImage(cartItem.getProductImage());
            settlementItem.setPrice(money(cartItem.getPrice()));
            settlementItem.setQuantity(cartItem.getQuantity());
            settlementItem.setSubtotal(subtotal);
            settlementItem.setStock(cartItem.getStock());
            settlementItems.add(settlementItem);
        }

        BigDecimal normalizedProductAmount = money(productAmount);
        BigDecimal totalAmount = money(normalizedProductAmount.add(ZERO_AMOUNT));
        return new SettlementContext(
                storeId, storeName, deliveryAddressId, cartItems, settlementItems,
                normalizedProductAmount, ZERO_AMOUNT, totalAmount, selectedProductCount);
    }

    private void validateSettlementAddress(Long deliveryAddressId, Long userId) {
        UserAddressEntity address = userAddressMapper.selectByIdIncludeDeleted(deliveryAddressId);
        if (address == null
                || !userId.equals(address.getUserId())
                || Integer.valueOf(1).equals(address.getIsDeleted())) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_EXIST);
        }
    }

    private void validateSettlementItem(CartItemQueryVO item) {
        if (item == null
                || !isOne(item.getProductExists())
                || isOne(item.getProductDeleted())) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
        if (!ProductStatusEnum.ON_SHELF.getCode().equals(item.getProductStatus())) {
            throw new BusinessException(ResultCode.PRODUCT_OFF_SHELF);
        }
        if (!isOne(item.getCategoryExists())
                || isOne(item.getCategoryDeleted())
                || !CategoryStatusEnum.ENABLED.getCode().equals(item.getCategoryStatus())) {
            throw new BusinessException(ResultCode.CATEGORY_DISABLED);
        }
        if (!isOne(item.getStoreExists()) || isOne(item.getStoreDeleted())) {
            throw new BusinessException(ResultCode.STORE_NOT_EXIST);
        }
        if (!MerchantStatusEnum.ENABLED.getCode().equals(item.getStoreStatus())) {
            throw new BusinessException(ResultCode.STORE_DISABLED);
        }
        if (!BusinessStatusEnum.OPEN.getCode().equals(item.getBusinessStatus())) {
            throw new BusinessException(ResultCode.STORE_CLOSED);
        }
        if (!isOne(item.getMerchantExists())
                || isOne(item.getMerchantDeleted())
                || !MerchantAuditStatusEnum.APPROVED.getCode().equals(item.getMerchantAuditStatus())
                || !MerchantStatusEnum.ENABLED.getCode().equals(item.getMerchantStatus())) {
            throw new BusinessException(ResultCode.STORE_DISABLED);
        }
        if (item.getStoreId() == null
                || item.getProductStoreId() == null
                || !item.getStoreId().equals(item.getProductStoreId())) {
            throw new BusinessException(ResultCode.SETTLEMENT_CHANGED);
        }
        if (item.getPrice() == null) {
            throw new BusinessException(ResultCode.SETTLEMENT_CHANGED);
        }
        if (item.getQuantity() == null
                || item.getQuantity() <= 0
                || item.getStock() == null
                || item.getStock() < item.getQuantity()) {
            throw new BusinessException(ResultCode.PRODUCT_STOCK_INSUFFICIENT);
        }
    }

    private OrderEntity insertProductOrder(Long userId, CreateCartOrderDTO createCartOrderDTO,
                                           SettlementContext settlement, LocalDateTime now) {
        for (int retry = 0; retry < MAX_ORDER_NO_RETRY; retry++) {
            OrderEntity order = buildProductOrder(
                    userId, createCartOrderDTO, settlement, generateOrderNo(), now);
            try {
                if (getBaseMapper().insert(order) == 1 && order.getId() != null) {
                    return order;
                }
            } catch (DuplicateKeyException exception) {
                if (retry < MAX_ORDER_NO_RETRY - 1) {
                    continue;
                }
                throw new BusinessException(ResultCode.ORDER_CREATE_FAILED);
            }
        }
        throw new BusinessException(ResultCode.ORDER_CREATE_FAILED);
    }

    private OrderEntity buildProductOrder(Long userId, CreateCartOrderDTO createCartOrderDTO,
                                          SettlementContext settlement, String orderNo,
                                          LocalDateTime now) {
        OrderEntity order = new OrderEntity();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setRiderId(null);
        order.setPickupAddressId(null);
        order.setDeliveryAddressId(createCartOrderDTO.getDeliveryAddressId());
        order.setGoodsName(null);
        order.setGoodsDescription(null);
        order.setWeight(null);
        order.setDistance(null);
        order.setPrice(settlement.totalAmount());
        order.setStatus(OrderStatusEnum.WAITING_ACCEPT.getCode());
        order.setRemark(createCartOrderDTO.getRemark());
        order.setCreateTime(now);
        order.setUpdateTime(now);
        order.setAcceptTime(null);
        order.setFinishTime(null);
        order.setPayStatus(PaymentStatusEnum.UNPAID.getCode());
        order.setPayTime(null);
        order.setPaymentNo(null);
        order.setOrderType(OrderTypeEnum.PRODUCT.getCode());
        order.setStoreId(settlement.storeId());
        order.setProductAmount(settlement.productAmount());
        order.setDeliveryFee(settlement.deliveryFee());
        order.setTotalAmount(settlement.totalAmount());
        order.setDeleted(NOT_DELETED);
        return order;
    }

    private OrderItemEntity buildOrderItem(Long orderId, CartItemQueryVO cartItem,
                                           LocalDateTime now) {
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrderId(orderId);
        orderItem.setProductId(cartItem.getProductId());
        orderItem.setProductName(cartItem.getProductName());
        orderItem.setProductImage(cartItem.getProductImage());
        orderItem.setProductPrice(money(cartItem.getPrice()));
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setSubtotal(money(
                cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))));
        orderItem.setCreateTime(now);
        orderItem.setUpdateTime(now);
        orderItem.setIsDeleted(NOT_DELETED);
        return orderItem;
    }

    private void handleStockDeductionFailed(CartItemQueryVO settlementItem) {
        CartItemQueryVO currentState = shoppingCartMapper.selectProductState(
                settlementItem.getProductId());
        if (currentState != null) {
            currentState.setQuantity(settlementItem.getQuantity());
        }
        validateSettlementItem(currentState);
        if (!settlementItem.getStoreId().equals(currentState.getStoreId())
                || settlementItem.getPrice().compareTo(currentState.getPrice()) != 0) {
            throw new BusinessException(ResultCode.SETTLEMENT_CHANGED);
        }
        throw new BusinessException(ResultCode.SETTLEMENT_CHANGED);
    }

    private void insertProductOrderLog(Long orderId, Long userId, LocalDateTime now) {
        OrderLogEntity orderLog = new OrderLogEntity();
        orderLog.setOrderId(orderId);
        orderLog.setOldStatus(OrderStatusEnum.WAITING_ACCEPT.getCode());
        orderLog.setNewStatus(OrderStatusEnum.WAITING_ACCEPT.getCode());
        orderLog.setOperatorId(userId);
        orderLog.setOperatorType(OPERATOR_TYPE_USER);
        orderLog.setRemark("用户从购物车创建商品订单");
        orderLog.setCreateTime(now);
        if (orderLogMapper.insert(orderLog) != 1) {
            throw new BusinessException(ResultCode.ORDER_CREATE_FAILED);
        }
    }

    private SettlementPreviewVO toSettlementPreviewVO(SettlementContext settlement) {
        SettlementPreviewVO preview = new SettlementPreviewVO();
        preview.setStoreId(settlement.storeId());
        preview.setStoreName(settlement.storeName());
        preview.setDeliveryAddressId(settlement.deliveryAddressId());
        preview.setItems(settlement.items());
        preview.setProductAmount(settlement.productAmount());
        preview.setDeliveryFee(settlement.deliveryFee());
        preview.setTotalAmount(settlement.totalAmount());
        preview.setSelectedProductCount(settlement.selectedProductCount());
        return preview;
    }

    private CreateCartOrderVO toCreateCartOrderVO(OrderEntity order,
                                                   SettlementContext settlement) {
        CreateCartOrderVO result = new CreateCartOrderVO();
        result.setOrderId(order.getId());
        result.setOrderNo(order.getOrderNo());
        result.setOrderType(order.getOrderType());
        result.setStoreId(order.getStoreId());
        result.setProductAmount(order.getProductAmount());
        result.setDeliveryFee(order.getDeliveryFee());
        result.setTotalAmount(order.getTotalAmount());
        result.setPayStatus(order.getPayStatus());
        result.setStatus(order.getStatus());
        result.setItemCount(settlement.selectedProductCount());
        return result;
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
        orderEntity.setOrderType(OrderTypeEnum.ERRAND.getCode());
        orderEntity.setStoreId(null);
        orderEntity.setProductAmount(null);
        orderEntity.setDeliveryFee(null);
        orderEntity.setTotalAmount(null);
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
        Integer orderType = orderEntity.getOrderType() == null
                ? OrderTypeEnum.ERRAND.getCode()
                : orderEntity.getOrderType();
        orderDetailVO.setOrderType(orderType);
        orderDetailVO.setOrderTypeText(OrderTypeEnum.getTextByCode(orderType));
        orderDetailVO.setStoreId(orderEntity.getStoreId());
        orderDetailVO.setProductAmount(orderEntity.getProductAmount());
        orderDetailVO.setDeliveryFee(orderEntity.getDeliveryFee());
        orderDetailVO.setTotalAmount(orderEntity.getTotalAmount());
        if (OrderTypeEnum.PRODUCT.getCode().equals(orderType)) {
            orderDetailVO.setStoreName(
                    merchantStoreMapper.selectStoreNameIncludeDeleted(orderEntity.getStoreId()));
            orderDetailVO.setItems(orderItemMapper.selectByOrderId(orderEntity.getId()).stream()
                    .map(this::toOrderItemVO)
                    .toList());
        } else {
            orderDetailVO.setItems(List.of());
        }
        orderDetailVO.setRemark(orderEntity.getRemark());
        orderDetailVO.setCreateTime(orderEntity.getCreateTime());
        orderDetailVO.setUpdateTime(orderEntity.getUpdateTime());
        return orderDetailVO;
    }

    private OrderItemVO toOrderItemVO(OrderItemEntity orderItem) {
        OrderItemVO orderItemVO = new OrderItemVO();
        orderItemVO.setProductId(orderItem.getProductId());
        orderItemVO.setProductName(orderItem.getProductName());
        orderItemVO.setProductImage(orderItem.getProductImage());
        orderItemVO.setProductPrice(orderItem.getProductPrice());
        orderItemVO.setQuantity(orderItem.getQuantity());
        orderItemVO.setSubtotal(orderItem.getSubtotal());
        return orderItemVO;
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

    private Long getCurrentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    private void lockUser(Long userId) {
        if (shoppingCartMapper.lockUser(userId) == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
    }

    private boolean isOne(Integer value) {
        return Integer.valueOf(1).equals(value);
    }

    private BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private record SettlementContext(
            Long storeId,
            String storeName,
            Long deliveryAddressId,
            List<CartItemQueryVO> cartItems,
            List<SettlementItemVO> items,
            BigDecimal productAmount,
            BigDecimal deliveryFee,
            BigDecimal totalAmount,
            Long selectedProductCount) {
    }
}
