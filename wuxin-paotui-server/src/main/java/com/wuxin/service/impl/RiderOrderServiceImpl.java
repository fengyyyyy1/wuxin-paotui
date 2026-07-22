package com.wuxin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuxin.common.ResultCode;
import com.wuxin.entity.OrderLogEntity;
import com.wuxin.entity.OrderEntity;
import com.wuxin.entity.OrderItemEntity;
import com.wuxin.entity.MerchantStoreEntity;
import com.wuxin.entity.RiderInfoEntity;
import com.wuxin.entity.UserAddressEntity;
import com.wuxin.enums.OrderStatusEnum;
import com.wuxin.enums.OrderTypeEnum;
import com.wuxin.enums.PaymentStatusEnum;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.OrderLogMapper;
import com.wuxin.mapper.OrderItemMapper;
import com.wuxin.mapper.OrderMapper;
import com.wuxin.mapper.MerchantStoreMapper;
import com.wuxin.mapper.RiderInfoMapper;
import com.wuxin.mapper.UserAddressMapper;
import com.wuxin.service.RiderOrderService;
import com.wuxin.utils.UserContext;
import com.wuxin.vo.AcceptOrderVO;
import com.wuxin.vo.FinishOrderVO;
import com.wuxin.vo.GiveUpOrderVO;
import com.wuxin.vo.HallOrderVO;
import com.wuxin.vo.PageResultVO;
import com.wuxin.vo.OrderItemVO;
import com.wuxin.vo.RiderOrderDetailVO;
import com.wuxin.vo.RiderOrderTimelineVO;
import com.wuxin.vo.RiderOrderVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RiderOrderServiceImpl implements RiderOrderService {

    private static final Integer RIDER_AUDIT_APPROVED = 1;

    private static final Integer RIDER_ENABLED_STATUS = 1;

    private static final String OPERATOR_TYPE_RIDER = "RIDER";

    private static final String FINISH_STATUS_ERROR_MESSAGE = "\u5f53\u524d\u8ba2\u5355\u72b6\u6001\u4e0d\u53ef\u5b8c\u6210\u914d\u9001";

    private final OrderMapper orderMapper;

    private final RiderInfoMapper riderInfoMapper;

    private final OrderLogMapper orderLogMapper;

    private final UserAddressMapper userAddressMapper;

    private final MerchantStoreMapper merchantStoreMapper;

    private final OrderItemMapper orderItemMapper;

    public RiderOrderServiceImpl(
            OrderMapper orderMapper,
            RiderInfoMapper riderInfoMapper,
            OrderLogMapper orderLogMapper,
            UserAddressMapper userAddressMapper,
            MerchantStoreMapper merchantStoreMapper,
            OrderItemMapper orderItemMapper) {
        this.orderMapper = orderMapper;
        this.riderInfoMapper = riderInfoMapper;
        this.orderLogMapper = orderLogMapper;
        this.userAddressMapper = userAddressMapper;
        this.merchantStoreMapper = merchantStoreMapper;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    public PageResultVO<HallOrderVO> getHallOrders(Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        getCurrentRider(userId);

        long safePageNum = normalizePageNum(pageNum);
        long safePageSize = normalizePageSize(pageSize);

        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getPayStatus, PaymentStatusEnum.PAID.getCode())
                .eq(OrderEntity::getDeleted, 0)
                .and(scope -> scope
                        .nested(errand -> errand
                                .and(type -> type
                                        .isNull(OrderEntity::getOrderType)
                                        .or()
                                        .eq(OrderEntity::getOrderType,
                                                OrderTypeEnum.ERRAND.getCode()))
                                .eq(OrderEntity::getStatus,
                                        OrderStatusEnum.WAITING_ACCEPT.getCode()))
                        .or(product -> product
                                .eq(OrderEntity::getOrderType,
                                        OrderTypeEnum.PRODUCT.getCode())
                                .eq(OrderEntity::getStatus,
                                        OrderStatusEnum.WAITING_RIDER_ACCEPT.getCode())))
                .orderByDesc(OrderEntity::getCreateTime);

        Page<OrderEntity> page = orderMapper.selectPage(new Page<>(safePageNum, safePageSize), queryWrapper);
        List<HallOrderVO> records = page.getRecords().stream()
                .map(this::toHallOrderVO)
                .toList();

        PageResultVO<HallOrderVO> pageResultVO = new PageResultVO<>();
        pageResultVO.setRecords(records);
        pageResultVO.setTotal(page.getTotal());
        pageResultVO.setPageNum(safePageNum);
        pageResultVO.setPageSize(safePageSize);
        pageResultVO.setPages(page.getPages());
        return pageResultVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AcceptOrderVO acceptOrder(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        RiderInfoEntity riderInfo = getCurrentRider(userId);
        OrderEntity order = orderMapper.selectById(id);
        Integer oldStatus = getRiderAcceptOldStatus(order);
        LocalDateTime now = LocalDateTime.now();

        LambdaUpdateWrapper<OrderEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OrderEntity::getId, id)
                .eq(OrderEntity::getStatus, oldStatus)
                .eq(OrderEntity::getPayStatus, PaymentStatusEnum.PAID.getCode())
                .eq(OrderEntity::getDeleted, 0)
                .set(OrderEntity::getRiderId, riderInfo.getId())
                .set(OrderEntity::getStatus, OrderStatusEnum.ACCEPTED.getCode())
                .set(OrderEntity::getAcceptTime, now)
                .set(OrderEntity::getUpdateTime, now);

        int affectedRows = orderMapper.update(null, updateWrapper);
        if (affectedRows == 0) {
            handleAcceptFailed(id);
        }

        OrderLogEntity orderLog = new OrderLogEntity();
        orderLog.setOrderId(id);
        orderLog.setOldStatus(oldStatus);
        orderLog.setNewStatus(OrderStatusEnum.ACCEPTED.getCode());
        orderLog.setOperatorId(userId);
        orderLog.setOperatorType(OPERATOR_TYPE_RIDER);
        orderLog.setRemark("\u9a91\u624b\u63a5\u5355");
        orderLog.setCreateTime(now);
        int insertedRows = orderLogMapper.insert(orderLog);
        if (insertedRows != 1) {
            throw new BusinessException(ResultCode.FAIL, "order log save failed");
        }

        AcceptOrderVO acceptOrderVO = new AcceptOrderVO();
        acceptOrderVO.setOrderId(id);
        acceptOrderVO.setStatus(OrderStatusEnum.ACCEPTED.getCode());
        acceptOrderVO.setStatusText(OrderStatusEnum.ACCEPTED.getText());
        acceptOrderVO.setAcceptTime(now);
        return acceptOrderVO;
    }

    @Override
    public PageResultVO<RiderOrderVO> getMyOrders(Integer pageNum, Integer pageSize, Integer status) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        RiderInfoEntity riderInfo = getCurrentRider(userId);
        long safePageNum = normalizePageNum(pageNum);
        long safePageSize = normalizePageSize(pageSize);

        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getRiderId, riderInfo.getId())
                .eq(OrderEntity::getDeleted, 0)
                .eq(status != null, OrderEntity::getStatus, status)
                .orderByDesc(OrderEntity::getCreateTime);

        Page<OrderEntity> page = orderMapper.selectPage(new Page<>(safePageNum, safePageSize), queryWrapper);
        List<RiderOrderVO> records = page.getRecords().stream()
                .map(this::toRiderOrderVO)
                .toList();

        PageResultVO<RiderOrderVO> pageResultVO = new PageResultVO<>();
        pageResultVO.setRecords(records);
        pageResultVO.setTotal(page.getTotal());
        pageResultVO.setPageNum(safePageNum);
        pageResultVO.setPageSize(safePageSize);
        pageResultVO.setPages(page.getPages());
        return pageResultVO;
    }

    @Override
    public RiderOrderDetailVO getOrderDetail(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        RiderInfoEntity riderInfo = getCurrentRider(userId);
        OrderEntity order = getRiderOrder(id, riderInfo.getId());
        RiderOrderDetailVO result = new RiderOrderDetailVO();
        result.setId(order.getId());
        result.setOrderNo(order.getOrderNo());
        result.setOrderType(normalizeOrderType(order.getOrderType()));
        result.setOrderTypeText(OrderTypeEnum.getTextByCode(result.getOrderType()));
        result.setStoreId(order.getStoreId());
        result.setGoodsName(order.getGoodsName());
        result.setGoodsDescription(order.getGoodsDescription());
        result.setWeight(order.getWeight());
        result.setDistance(order.getDistance());
        result.setPrice(order.getPrice());
        result.setProductAmount(order.getProductAmount());
        result.setDeliveryFee(order.getDeliveryFee());
        result.setTotalAmount(order.getTotalAmount());
        result.setStatus(order.getStatus());
        result.setStatusText(OrderStatusEnum.getDescriptionByCode(
                order.getStatus(), order.getOrderType(), order.getPayStatus()));
        result.setPayStatus(order.getPayStatus());
        result.setPayStatusText(PaymentStatusEnum.getTextByCode(order.getPayStatus()));
        result.setRemark(order.getRemark());
        result.setCreateTime(order.getCreateTime());
        result.setPayTime(order.getPayTime());
        result.setAcceptTime(order.getAcceptTime());
        result.setFinishTime(order.getFinishTime());

        UserAddressEntity pickup = findAddress(order.getPickupAddressId());
        UserAddressEntity delivery = findAddress(order.getDeliveryAddressId());
        applyDeliveryAddress(result, delivery);
        applyPickup(result, order, pickup);

        List<OrderItemVO> items = orderItemMapper.selectByOrderId(order.getId()).stream()
                .map(this::toOrderItemVO)
                .toList();
        result.setItems(items);
        result.setGoodsSummary(buildGoodsSummary(items, order.getGoodsName()));
        result.setTimeline(selectTimeline(order));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FinishOrderVO finishOrder(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        RiderInfoEntity riderInfo = getCurrentRider(userId);
        OrderEntity orderEntity = getRiderOrder(id, riderInfo.getId());
        if (!OrderStatusEnum.ACCEPTED.getCode().equals(orderEntity.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, FINISH_STATUS_ERROR_MESSAGE);
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<OrderEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OrderEntity::getId, id)
                .eq(OrderEntity::getRiderId, riderInfo.getId())
                .eq(OrderEntity::getStatus, OrderStatusEnum.ACCEPTED.getCode())
                .eq(OrderEntity::getDeleted, 0)
                .set(OrderEntity::getStatus, OrderStatusEnum.WAITING_CONFIRM.getCode())
                .set(OrderEntity::getFinishTime, now)
                .set(OrderEntity::getUpdateTime, now);

        int affectedRows = orderMapper.update(null, updateWrapper);
        if (affectedRows != 1) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, FINISH_STATUS_ERROR_MESSAGE);
        }

        OrderLogEntity orderLog = new OrderLogEntity();
        orderLog.setOrderId(id);
        orderLog.setOldStatus(OrderStatusEnum.ACCEPTED.getCode());
        orderLog.setNewStatus(OrderStatusEnum.WAITING_CONFIRM.getCode());
        orderLog.setOperatorId(userId);
        orderLog.setOperatorType(OPERATOR_TYPE_RIDER);
        orderLog.setRemark("\u9a91\u624b\u5b8c\u6210\u914d\u9001");
        orderLog.setCreateTime(now);
        int insertedRows = orderLogMapper.insert(orderLog);
        if (insertedRows != 1) {
            throw new BusinessException(ResultCode.FAIL, "order log save failed");
        }

        FinishOrderVO finishOrderVO = new FinishOrderVO();
        finishOrderVO.setOrderId(id);
        finishOrderVO.setStatus(OrderStatusEnum.WAITING_CONFIRM.getCode());
        finishOrderVO.setStatusText(OrderStatusEnum.WAITING_CONFIRM.getText());
        finishOrderVO.setFinishTime(now);
        return finishOrderVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GiveUpOrderVO giveUpOrder(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        RiderInfoEntity riderInfo = getCurrentRider(userId);
        OrderEntity order = getRiderOrder(id, riderInfo.getId());
        if (!OrderStatusEnum.ACCEPTED.getCode().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_CANNOT_GIVE_UP);
        }
        OrderStatusEnum targetStatus = OrderTypeEnum.PRODUCT.getCode()
                .equals(order.getOrderType())
                ? OrderStatusEnum.WAITING_RIDER_ACCEPT
                : OrderStatusEnum.WAITING_ACCEPT;
        LocalDateTime now = LocalDateTime.now();

        LambdaUpdateWrapper<OrderEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OrderEntity::getId, id)
                .eq(OrderEntity::getRiderId, riderInfo.getId())
                .eq(OrderEntity::getStatus, OrderStatusEnum.ACCEPTED.getCode())
                .eq(OrderEntity::getDeleted, 0)
                .set(OrderEntity::getStatus, targetStatus.getCode())
                .set(OrderEntity::getRiderId, null)
                .set(OrderEntity::getAcceptTime, null)
                .set(OrderEntity::getUpdateTime, now);

        int affectedRows = orderMapper.update(null, updateWrapper);
        if (affectedRows != 1) {
            handleGiveUpFailed(id, riderInfo.getId(), userId, targetStatus);
        }

        OrderLogEntity orderLog = new OrderLogEntity();
        orderLog.setOrderId(id);
        orderLog.setOldStatus(OrderStatusEnum.ACCEPTED.getCode());
        orderLog.setNewStatus(targetStatus.getCode());
        orderLog.setOperatorId(userId);
        orderLog.setOperatorType(OPERATOR_TYPE_RIDER);
        orderLog.setRemark("骑手放弃订单");
        orderLog.setCreateTime(now);
        int insertedRows = orderLogMapper.insert(orderLog);
        if (insertedRows != 1) {
            throw new IllegalStateException("order log save failed");
        }

        GiveUpOrderVO giveUpOrderVO = new GiveUpOrderVO();
        giveUpOrderVO.setOrderId(id);
        giveUpOrderVO.setStatus(targetStatus.getCode());
        giveUpOrderVO.setStatusText(targetStatus.getText());
        giveUpOrderVO.setGiveUpTime(now);
        return giveUpOrderVO;
    }

    private HallOrderVO toHallOrderVO(OrderEntity orderEntity) {
        HallOrderVO hallOrderVO = new HallOrderVO();
        hallOrderVO.setId(orderEntity.getId());
        hallOrderVO.setOrderNo(orderEntity.getOrderNo());
        hallOrderVO.setGoodsName(orderEntity.getGoodsName());
        hallOrderVO.setGoodsDescription(orderEntity.getGoodsDescription());
        hallOrderVO.setWeight(orderEntity.getWeight());
        hallOrderVO.setDistance(orderEntity.getDistance());
        hallOrderVO.setPrice(orderEntity.getPrice());
        hallOrderVO.setPickupAddressId(orderEntity.getPickupAddressId());
        hallOrderVO.setDeliveryAddressId(orderEntity.getDeliveryAddressId());
        hallOrderVO.setOrderType(normalizeOrderType(orderEntity.getOrderType()));
        hallOrderVO.setOrderTypeText(OrderTypeEnum.getTextByCode(hallOrderVO.getOrderType()));
        hallOrderVO.setStoreId(orderEntity.getStoreId());
        enrichSummary(hallOrderVO, orderEntity);
        hallOrderVO.setStatus(orderEntity.getStatus());
        hallOrderVO.setStatusText(OrderStatusEnum.getDescriptionByCode(
                orderEntity.getStatus(),
                orderEntity.getOrderType(),
                orderEntity.getPayStatus()));
        hallOrderVO.setPayStatus(orderEntity.getPayStatus());
        hallOrderVO.setPayStatusText(PaymentStatusEnum.getTextByCode(orderEntity.getPayStatus()));
        hallOrderVO.setCreateTime(orderEntity.getCreateTime());
        return hallOrderVO;
    }

    private RiderOrderVO toRiderOrderVO(OrderEntity orderEntity) {
        RiderOrderVO riderOrderVO = new RiderOrderVO();
        riderOrderVO.setId(orderEntity.getId());
        riderOrderVO.setOrderNo(orderEntity.getOrderNo());
        riderOrderVO.setGoodsName(orderEntity.getGoodsName());
        riderOrderVO.setGoodsDescription(orderEntity.getGoodsDescription());
        riderOrderVO.setWeight(orderEntity.getWeight());
        riderOrderVO.setDistance(orderEntity.getDistance());
        riderOrderVO.setPrice(orderEntity.getPrice());
        riderOrderVO.setPickupAddressId(orderEntity.getPickupAddressId());
        riderOrderVO.setDeliveryAddressId(orderEntity.getDeliveryAddressId());
        riderOrderVO.setOrderType(normalizeOrderType(orderEntity.getOrderType()));
        riderOrderVO.setOrderTypeText(OrderTypeEnum.getTextByCode(riderOrderVO.getOrderType()));
        riderOrderVO.setStoreId(orderEntity.getStoreId());
        enrichSummary(riderOrderVO, orderEntity);
        riderOrderVO.setStatus(orderEntity.getStatus());
        riderOrderVO.setStatusText(OrderStatusEnum.getTextByCode(orderEntity.getStatus()));
        riderOrderVO.setPayStatus(orderEntity.getPayStatus());
        riderOrderVO.setPayStatusText(PaymentStatusEnum.getTextByCode(orderEntity.getPayStatus()));
        riderOrderVO.setAcceptTime(orderEntity.getAcceptTime());
        riderOrderVO.setFinishTime(orderEntity.getFinishTime());
        riderOrderVO.setCreateTime(orderEntity.getCreateTime());
        return riderOrderVO;
    }

    private void enrichSummary(HallOrderVO target, OrderEntity order) {
        UserAddressEntity pickup = findAddress(order.getPickupAddressId());
        UserAddressEntity delivery = findAddress(order.getDeliveryAddressId());
        MerchantStoreEntity store = findStore(order.getStoreId());
        target.setStoreName(store == null ? null : store.getStoreName());
        target.setPickupAddress(store != null && OrderTypeEnum.PRODUCT.getCode().equals(order.getOrderType())
                ? fullStoreAddress(store) : fullAddress(pickup));
        target.setDeliveryAddress(fullAddress(delivery));
        target.setGoodsSummary(buildGoodsSummary(order.getId(), order.getGoodsName()));
    }

    private void enrichSummary(RiderOrderVO target, OrderEntity order) {
        UserAddressEntity pickup = findAddress(order.getPickupAddressId());
        UserAddressEntity delivery = findAddress(order.getDeliveryAddressId());
        MerchantStoreEntity store = findStore(order.getStoreId());
        target.setStoreName(store == null ? null : store.getStoreName());
        target.setPickupAddress(store != null && OrderTypeEnum.PRODUCT.getCode().equals(order.getOrderType())
                ? fullStoreAddress(store) : fullAddress(pickup));
        target.setDeliveryAddress(fullAddress(delivery));
        target.setGoodsSummary(buildGoodsSummary(order.getId(), order.getGoodsName()));
    }

    private void applyPickup(RiderOrderDetailVO target, OrderEntity order, UserAddressEntity pickup) {
        MerchantStoreEntity store = findStore(order.getStoreId());
        if (store != null && OrderTypeEnum.PRODUCT.getCode().equals(order.getOrderType())) {
            target.setStoreName(store.getStoreName());
            target.setPickupName(store.getStoreName());
            target.setPickupPhone(store.getStorePhone());
            target.setPickupAddress(fullStoreAddress(store));
            target.setPickupLatitude(store.getLatitude());
            target.setPickupLongitude(store.getLongitude());
            return;
        }
        if (pickup != null) {
            target.setPickupName(pickup.getReceiverName());
            target.setPickupPhone(pickup.getReceiverPhone());
            target.setPickupAddress(fullAddress(pickup));
            target.setPickupLatitude(pickup.getLatitude());
            target.setPickupLongitude(pickup.getLongitude());
        }
    }

    private void applyDeliveryAddress(RiderOrderDetailVO target, UserAddressEntity delivery) {
        if (delivery == null) {
            return;
        }
        target.setDeliveryName(delivery.getReceiverName());
        target.setDeliveryPhone(delivery.getReceiverPhone());
        target.setDeliveryAddress(fullAddress(delivery));
        target.setDeliveryLatitude(delivery.getLatitude());
        target.setDeliveryLongitude(delivery.getLongitude());
    }

    private List<RiderOrderTimelineVO> selectTimeline(OrderEntity order) {
        LambdaQueryWrapper<OrderLogEntity> query = new LambdaQueryWrapper<>();
        query.eq(OrderLogEntity::getOrderId, order.getId())
                .orderByAsc(OrderLogEntity::getCreateTime)
                .orderByAsc(OrderLogEntity::getId);
        return orderLogMapper.selectList(query).stream().map(log -> {
            RiderOrderTimelineVO item = new RiderOrderTimelineVO();
            item.setOldStatus(log.getOldStatus());
            item.setOldStatusText(OrderStatusEnum.getDescriptionByCode(
                    log.getOldStatus(), order.getOrderType(), order.getPayStatus()));
            item.setNewStatus(log.getNewStatus());
            item.setNewStatusText(OrderStatusEnum.getDescriptionByCode(
                    log.getNewStatus(), order.getOrderType(), order.getPayStatus()));
            item.setOperatorType(log.getOperatorType());
            item.setRemark(log.getRemark());
            item.setCreateTime(log.getCreateTime());
            return item;
        }).toList();
    }

    private OrderItemVO toOrderItemVO(OrderItemEntity item) {
        OrderItemVO result = new OrderItemVO();
        result.setProductId(item.getProductId());
        result.setProductName(item.getProductName());
        result.setProductImage(item.getProductImage());
        result.setProductPrice(item.getProductPrice());
        result.setQuantity(item.getQuantity());
        result.setSubtotal(item.getSubtotal());
        return result;
    }

    private String buildGoodsSummary(Long orderId, String fallback) {
        return buildGoodsSummary(
                orderItemMapper.selectByOrderId(orderId).stream().map(this::toOrderItemVO).toList(),
                fallback);
    }

    private String buildGoodsSummary(List<OrderItemVO> items, String fallback) {
        if (items == null || items.isEmpty()) {
            return fallback;
        }
        return items.stream()
                .map(item -> item.getProductName() + " x" + item.getQuantity())
                .reduce((left, right) -> left + "、" + right)
                .orElse(fallback);
    }

    private UserAddressEntity findAddress(Long addressId) {
        return addressId == null ? null : userAddressMapper.selectById(addressId);
    }

    private MerchantStoreEntity findStore(Long storeId) {
        return storeId == null ? null : merchantStoreMapper.selectById(storeId);
    }

    private String fullAddress(UserAddressEntity address) {
        if (address == null) {
            return null;
        }
        return joinAddress(address.getProvince(), address.getCity(), address.getDistrict(), address.getDetailAddress());
    }

    private String fullStoreAddress(MerchantStoreEntity store) {
        return joinAddress(store.getProvince(), store.getCity(), store.getDistrict(), store.getDetailAddress());
    }

    private String joinAddress(String province, String city, String district, String detail) {
        return java.util.stream.Stream.of(province, city, district, detail)
                .filter(value -> value != null && !value.isBlank())
                .reduce("", String::concat);
    }

    private Integer normalizeOrderType(Integer orderType) {
        return orderType == null ? OrderTypeEnum.ERRAND.getCode() : orderType;
    }

    private OrderEntity getRiderOrder(Long id, Long riderId) {
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getId, id)
                .eq(OrderEntity::getRiderId, riderId)
                .eq(OrderEntity::getDeleted, 0)
                .last("LIMIT 1");

        OrderEntity orderEntity = orderMapper.selectOne(queryWrapper);
        if (orderEntity == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        return orderEntity;
    }

    private RiderInfoEntity getCurrentRider(Long userId) {
        LambdaQueryWrapper<RiderInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RiderInfoEntity::getUserId, userId)
                .last("LIMIT 1");

        RiderInfoEntity riderInfo = riderInfoMapper.selectOne(queryWrapper);
        if (riderInfo == null
                || !RIDER_AUDIT_APPROVED.equals(riderInfo.getAuditStatus())
                || !RIDER_ENABLED_STATUS.equals(riderInfo.getRiderStatus())) {
            throw new BusinessException(ResultCode.NOT_RIDER);
        }
        return riderInfo;
    }

    private void handleGiveUpFailed(
            Long id,
            Long riderId,
            Long userId,
            OrderStatusEnum targetStatus) {
        LambdaQueryWrapper<OrderEntity> orderQueryWrapper = new LambdaQueryWrapper<>();
        orderQueryWrapper.eq(OrderEntity::getId, id)
                .eq(OrderEntity::getRiderId, riderId)
                .eq(OrderEntity::getDeleted, 0)
                .last("LIMIT 1");

        OrderEntity orderEntity = orderMapper.selectOne(orderQueryWrapper);
        if (orderEntity != null) {
            throw new BusinessException(ResultCode.ORDER_STATUS_CANNOT_GIVE_UP);
        }

        LambdaQueryWrapper<OrderLogEntity> logQueryWrapper = new LambdaQueryWrapper<>();
        logQueryWrapper.eq(OrderLogEntity::getOrderId, id)
                .eq(OrderLogEntity::getOldStatus, OrderStatusEnum.ACCEPTED.getCode())
                .eq(OrderLogEntity::getNewStatus, targetStatus.getCode())
                .eq(OrderLogEntity::getOperatorId, userId)
                .eq(OrderLogEntity::getOperatorType, OPERATOR_TYPE_RIDER);

        if (orderLogMapper.selectCount(logQueryWrapper) > 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_CANNOT_GIVE_UP);
        }
        throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
    }

    private void handleAcceptFailed(Long id) {
        OrderEntity orderEntity = orderMapper.selectById(id);
        if (orderEntity == null || Integer.valueOf(1).equals(orderEntity.getDeleted())) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        if (!PaymentStatusEnum.PAID.getCode().equals(orderEntity.getPayStatus())) {
            throw new BusinessException(ResultCode.ORDER_NOT_PAID);
        }
        if (OrderTypeEnum.PRODUCT.getCode().equals(orderEntity.getOrderType())
                && !OrderStatusEnum.WAITING_RIDER_ACCEPT.getCode()
                .equals(orderEntity.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        if (OrderStatusEnum.CANCELLED.getCode().equals(orderEntity.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        throw new BusinessException(ResultCode.ORDER_ALREADY_ACCEPTED);
    }

    private Integer getRiderAcceptOldStatus(OrderEntity order) {
        if (order == null || Integer.valueOf(1).equals(order.getDeleted())) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        if (!PaymentStatusEnum.PAID.getCode().equals(order.getPayStatus())) {
            throw new BusinessException(ResultCode.ORDER_NOT_PAID);
        }
        if (OrderTypeEnum.PRODUCT.getCode().equals(order.getOrderType())) {
            if (!OrderStatusEnum.WAITING_RIDER_ACCEPT.getCode()
                    .equals(order.getStatus())) {
                throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
            }
            return OrderStatusEnum.WAITING_RIDER_ACCEPT.getCode();
        }
        if (!OrderStatusEnum.WAITING_ACCEPT.getCode().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_ALREADY_ACCEPTED);
        }
        return OrderStatusEnum.WAITING_ACCEPT.getCode();
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
}
