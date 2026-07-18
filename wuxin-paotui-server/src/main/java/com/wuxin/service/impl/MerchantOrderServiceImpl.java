package com.wuxin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuxin.common.ResultCode;
import com.wuxin.dto.merchant.MerchantOrderPageQueryDTO;
import com.wuxin.dto.merchant.RejectMerchantOrderDTO;
import com.wuxin.entity.OrderEntity;
import com.wuxin.entity.OrderItemEntity;
import com.wuxin.entity.OrderLogEntity;
import com.wuxin.enums.OrderStatusEnum;
import com.wuxin.enums.OrderTypeEnum;
import com.wuxin.enums.PaymentStatusEnum;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.MerchantOrderMapper;
import com.wuxin.mapper.OrderItemMapper;
import com.wuxin.mapper.OrderLogMapper;
import com.wuxin.mapper.OrderMapper;
import com.wuxin.service.MerchantOrderService;
import com.wuxin.service.MerchantService;
import com.wuxin.utils.UserContext;
import com.wuxin.vo.MerchantOrderDetailVO;
import com.wuxin.vo.MerchantOrderPageVO;
import com.wuxin.vo.MerchantOrderStatusVO;
import com.wuxin.vo.MerchantOrderTimelineVO;
import com.wuxin.vo.OrderItemVO;
import com.wuxin.vo.PageResultVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MerchantOrderServiceImpl implements MerchantOrderService {

    private static final int NOT_DELETED = 0;

    private static final String OPERATOR_TYPE_MERCHANT = "MERCHANT";

    private static final int REJECT_REASON_MIN_LENGTH = 2;

    private static final int REJECT_REASON_MAX_LENGTH = 200;

    private final MerchantService merchantService;

    private final MerchantOrderMapper merchantOrderMapper;

    private final OrderMapper orderMapper;

    private final OrderItemMapper orderItemMapper;

    private final OrderLogMapper orderLogMapper;

    public MerchantOrderServiceImpl(
            MerchantService merchantService,
            MerchantOrderMapper merchantOrderMapper,
            OrderMapper orderMapper,
            OrderItemMapper orderItemMapper,
            OrderLogMapper orderLogMapper) {
        this.merchantService = merchantService;
        this.merchantOrderMapper = merchantOrderMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderLogMapper = orderLogMapper;
    }

    @Override
    public PageResultVO<MerchantOrderPageVO> pageOrders(
            MerchantOrderPageQueryDTO query) {
        MerchantOrderPageQueryDTO safeQuery = validatePageQuery(query);
        Long storeId = merchantService.getCurrentApprovedStoreId();
        Page<MerchantOrderPageVO> page = merchantOrderMapper.selectOrderPage(
                new Page<>(safeQuery.getPageNum(), safeQuery.getPageSize()),
                storeId,
                OrderTypeEnum.PRODUCT.getCode(),
                safeQuery.getStatus(),
                trimToNull(safeQuery.getKeyword()),
                safeQuery.getStartTime(),
                safeQuery.getEndTime());

        page.getRecords().forEach(this::enrichOrder);
        return toPageResult(page);
    }

    @Override
    public MerchantOrderDetailVO getOrderDetail(Long orderId) {
        validateOrderId(orderId);
        Long storeId = merchantService.getCurrentApprovedStoreId();
        MerchantOrderDetailVO detail = merchantOrderMapper.selectOrderDetail(
                orderId, storeId, OrderTypeEnum.PRODUCT.getCode());
        if (detail == null) {
            throw new BusinessException(ResultCode.MERCHANT_ORDER_NOT_EXIST);
        }

        enrichOrder(detail);
        detail.setItems(orderItemMapper.selectByOrderId(orderId).stream()
                .map(this::toOrderItemVO)
                .toList());
        detail.setTimeline(selectTimeline(orderId, detail.getPayStatus()));
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantOrderStatusVO acceptOrder(Long orderId) {
        validateOrderId(orderId);
        Long userId = getCurrentUserId();
        Long storeId = merchantService.getCurrentApprovedStoreId();
        LocalDateTime now = LocalDateTime.now();

        LambdaUpdateWrapper<OrderEntity> update = baseTransitionUpdate(
                orderId, storeId, OrderStatusEnum.WAITING_ACCEPT);
        update.set(OrderEntity::getStatus, OrderStatusEnum.MERCHANT_PREPARING.getCode())
                .set(OrderEntity::getMerchantAcceptTime, now)
                .set(OrderEntity::getUpdateTime, now);
        if (orderMapper.update(null, update) != 1) {
            handleTransitionFailure(orderId, storeId);
        }

        insertLog(
                orderId,
                OrderStatusEnum.WAITING_ACCEPT,
                OrderStatusEnum.MERCHANT_PREPARING,
                userId,
                "商家接单",
                now);
        return toStatusVO(
                orderId,
                OrderStatusEnum.MERCHANT_PREPARING,
                now,
                null,
                null,
                null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantOrderStatusVO rejectOrder(
            Long orderId,
            RejectMerchantOrderDTO request) {
        validateOrderId(orderId);
        String reason = validateRejectReason(request);
        Long userId = getCurrentUserId();
        Long storeId = merchantService.getCurrentApprovedStoreId();
        LocalDateTime now = LocalDateTime.now();

        LambdaUpdateWrapper<OrderEntity> update = baseTransitionUpdate(
                orderId, storeId, OrderStatusEnum.WAITING_ACCEPT);
        update.set(OrderEntity::getStatus, OrderStatusEnum.WAITING_REFUND.getCode())
                .set(OrderEntity::getMerchantRejectTime, now)
                .set(OrderEntity::getMerchantRejectReason, reason)
                .set(OrderEntity::getUpdateTime, now);
        if (orderMapper.update(null, update) != 1) {
            handleTransitionFailure(orderId, storeId);
        }

        insertLog(
                orderId,
                OrderStatusEnum.WAITING_ACCEPT,
                OrderStatusEnum.WAITING_REFUND,
                userId,
                "商家拒单：" + reason,
                now);
        return toStatusVO(
                orderId,
                OrderStatusEnum.WAITING_REFUND,
                null,
                null,
                now,
                reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantOrderStatusVO readyOrder(Long orderId) {
        validateOrderId(orderId);
        Long userId = getCurrentUserId();
        Long storeId = merchantService.getCurrentApprovedStoreId();
        LocalDateTime now = LocalDateTime.now();

        LambdaUpdateWrapper<OrderEntity> update = baseTransitionUpdate(
                orderId, storeId, OrderStatusEnum.MERCHANT_PREPARING);
        update.set(OrderEntity::getStatus, OrderStatusEnum.WAITING_RIDER_ACCEPT.getCode())
                .set(OrderEntity::getMerchantReadyTime, now)
                .set(OrderEntity::getUpdateTime, now);
        if (orderMapper.update(null, update) != 1) {
            handleTransitionFailure(orderId, storeId);
        }

        insertLog(
                orderId,
                OrderStatusEnum.MERCHANT_PREPARING,
                OrderStatusEnum.WAITING_RIDER_ACCEPT,
                userId,
                "商家出餐",
                now);
        OrderEntity updatedOrder = findOwnedProductOrder(orderId, storeId);
        if (updatedOrder == null) {
            throw new BusinessException(ResultCode.MERCHANT_ORDER_NOT_EXIST);
        }
        return toStatusVO(updatedOrder);
    }

    private MerchantOrderPageQueryDTO validatePageQuery(
            MerchantOrderPageQueryDTO query) {
        MerchantOrderPageQueryDTO safeQuery =
                query == null ? new MerchantOrderPageQueryDTO() : query;
        if (safeQuery.getPageNum() == null
                || safeQuery.getPageNum() < 1
                || safeQuery.getPageSize() == null
                || safeQuery.getPageSize() < 1
                || safeQuery.getPageSize() > 100
                || (safeQuery.getStatus() != null
                    && OrderStatusEnum.of(safeQuery.getStatus()) == null)
                || (safeQuery.getKeyword() != null
                    && safeQuery.getKeyword().length() > 100)
                || (safeQuery.getStartTime() != null
                    && safeQuery.getEndTime() != null
                    && !safeQuery.getStartTime().isBefore(safeQuery.getEndTime()))) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return safeQuery;
    }

    private LambdaUpdateWrapper<OrderEntity> baseTransitionUpdate(
            Long orderId,
            Long storeId,
            OrderStatusEnum oldStatus) {
        LambdaUpdateWrapper<OrderEntity> update = new LambdaUpdateWrapper<>();
        update.eq(OrderEntity::getId, orderId)
                .eq(OrderEntity::getStoreId, storeId)
                .eq(OrderEntity::getOrderType, OrderTypeEnum.PRODUCT.getCode())
                .eq(OrderEntity::getPayStatus, PaymentStatusEnum.PAID.getCode())
                .eq(OrderEntity::getStatus, oldStatus.getCode())
                .eq(OrderEntity::getDeleted, NOT_DELETED);
        return update;
    }

    private void handleTransitionFailure(Long orderId, Long storeId) {
        OrderEntity order = findOwnedProductOrder(orderId, storeId);
        if (order == null) {
            throw new BusinessException(ResultCode.MERCHANT_ORDER_NOT_EXIST);
        }
        if (!PaymentStatusEnum.PAID.getCode().equals(order.getPayStatus())) {
            throw new BusinessException(ResultCode.MERCHANT_ORDER_UNPAID);
        }
        throw new BusinessException(ResultCode.MERCHANT_ORDER_STATUS_ERROR);
    }

    private OrderEntity findOwnedProductOrder(Long orderId, Long storeId) {
        LambdaQueryWrapper<OrderEntity> query = new LambdaQueryWrapper<>();
        query.eq(OrderEntity::getId, orderId)
                .eq(OrderEntity::getStoreId, storeId)
                .eq(OrderEntity::getOrderType, OrderTypeEnum.PRODUCT.getCode())
                .eq(OrderEntity::getDeleted, NOT_DELETED)
                .last("LIMIT 1");
        return orderMapper.selectOne(query);
    }

    private void insertLog(
            Long orderId,
            OrderStatusEnum oldStatus,
            OrderStatusEnum newStatus,
            Long operatorId,
            String remark,
            LocalDateTime now) {
        OrderLogEntity log = new OrderLogEntity();
        log.setOrderId(orderId);
        log.setOldStatus(oldStatus.getCode());
        log.setNewStatus(newStatus.getCode());
        log.setOperatorId(operatorId);
        log.setOperatorType(OPERATOR_TYPE_MERCHANT);
        log.setRemark(remark);
        log.setCreateTime(now);
        if (orderLogMapper.insert(log) != 1) {
            throw new IllegalStateException("order log save failed");
        }
    }

    private void enrichOrder(MerchantOrderPageVO order) {
        order.setStatusName(OrderStatusEnum.getDescriptionByCode(
                order.getStatus(),
                OrderTypeEnum.PRODUCT.getCode(),
                order.getPayStatus()));
        order.setPayStatusName(
                PaymentStatusEnum.getTextByCode(order.getPayStatus()));
        order.setReceiverPhone(maskPhone(order.getReceiverPhone()));
    }

    private List<MerchantOrderTimelineVO> selectTimeline(
            Long orderId,
            Integer payStatus) {
        LambdaQueryWrapper<OrderLogEntity> query = new LambdaQueryWrapper<>();
        query.eq(OrderLogEntity::getOrderId, orderId)
                .orderByAsc(OrderLogEntity::getCreateTime)
                .orderByAsc(OrderLogEntity::getId);
        return orderLogMapper.selectList(query).stream()
                .map(log -> toTimelineVO(log, payStatus))
                .toList();
    }

    private MerchantOrderTimelineVO toTimelineVO(
            OrderLogEntity log,
            Integer payStatus) {
        MerchantOrderTimelineVO result = new MerchantOrderTimelineVO();
        result.setOldStatus(log.getOldStatus());
        result.setOldStatusName(OrderStatusEnum.getDescriptionByCode(
                log.getOldStatus(), OrderTypeEnum.PRODUCT.getCode(), payStatus));
        result.setNewStatus(log.getNewStatus());
        result.setNewStatusName(OrderStatusEnum.getDescriptionByCode(
                log.getNewStatus(), OrderTypeEnum.PRODUCT.getCode(), payStatus));
        result.setOperatorType(log.getOperatorType());
        result.setRemark(log.getRemark());
        result.setCreateTime(log.getCreateTime());
        return result;
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

    private MerchantOrderStatusVO toStatusVO(
            Long orderId,
            OrderStatusEnum status,
            LocalDateTime merchantAcceptTime,
            LocalDateTime readyTime,
            LocalDateTime rejectTime,
            String rejectReason) {
        MerchantOrderStatusVO result = new MerchantOrderStatusVO();
        result.setOrderId(orderId);
        result.setStatus(status.getCode());
        result.setStatusName(status.getText());
        result.setMerchantAcceptTime(merchantAcceptTime);
        result.setReadyTime(readyTime);
        result.setRejectTime(rejectTime);
        result.setRejectReason(rejectReason);
        return result;
    }

    private MerchantOrderStatusVO toStatusVO(OrderEntity order) {
        OrderStatusEnum status = OrderStatusEnum.of(order.getStatus());
        return toStatusVO(
                order.getId(),
                status,
                order.getMerchantAcceptTime(),
                order.getMerchantReadyTime(),
                order.getMerchantRejectTime(),
                order.getMerchantRejectReason());
    }

    private PageResultVO<MerchantOrderPageVO> toPageResult(
            Page<MerchantOrderPageVO> page) {
        PageResultVO<MerchantOrderPageVO> result = new PageResultVO<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setPages(page.getPages());
        return result;
    }

    private String validateRejectReason(RejectMerchantOrderDTO request) {
        String reason = request == null ? null : trimToNull(request.getReason());
        if (reason == null
                || reason.length() < REJECT_REASON_MIN_LENGTH
                || reason.length() > REJECT_REASON_MAX_LENGTH) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return reason;
    }

    private Long getCurrentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return userId;
    }

    private void validateOrderId(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }
        if (phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 3)
                + "****"
                + phone.substring(phone.length() - 4);
    }
}
