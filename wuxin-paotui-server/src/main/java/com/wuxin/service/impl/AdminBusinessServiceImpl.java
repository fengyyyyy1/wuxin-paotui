package com.wuxin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuxin.common.ResultCode;
import com.wuxin.dto.admin.AdminConsoleDTO;
import com.wuxin.entity.HomeRecommendationEntity;
import com.wuxin.entity.MerchantProductEntity;
import com.wuxin.entity.OrderEntity;
import com.wuxin.entity.OrderLogEntity;
import com.wuxin.entity.UserEntity;
import com.wuxin.enums.OrderStatusEnum;
import com.wuxin.enums.ProductStatusEnum;
import com.wuxin.exception.BusinessException;
import com.wuxin.mapper.AdminConsoleMapper;
import com.wuxin.mapper.HomeRecommendationMapper;
import com.wuxin.mapper.MerchantProductMapper;
import com.wuxin.mapper.OrderLogMapper;
import com.wuxin.mapper.OrderMapper;
import com.wuxin.mapper.UserMapper;
import com.wuxin.service.AdminAuditLogService;
import com.wuxin.service.AdminBusinessService;
import com.wuxin.utils.UserContext;
import com.wuxin.vo.PageResultVO;
import com.wuxin.vo.admin.AdminConsoleVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class AdminBusinessServiceImpl implements AdminBusinessService {

    private static final Set<Integer> MANUAL_COMPLETE_STATUSES = Set.of(2, 3);

    private final AdminConsoleMapper mapper;
    private final OrderMapper orderMapper;
    private final OrderLogMapper orderLogMapper;
    private final UserMapper userMapper;
    private final MerchantProductMapper productMapper;
    private final HomeRecommendationMapper recommendationMapper;
    private final AdminAuditLogService auditLogService;

    public AdminBusinessServiceImpl(
            AdminConsoleMapper mapper,
            OrderMapper orderMapper,
            OrderLogMapper orderLogMapper,
            UserMapper userMapper,
            MerchantProductMapper productMapper,
            HomeRecommendationMapper recommendationMapper,
            AdminAuditLogService auditLogService) {
        this.mapper = mapper;
        this.orderMapper = orderMapper;
        this.orderLogMapper = orderLogMapper;
        this.userMapper = userMapper;
        this.productMapper = productMapper;
        this.recommendationMapper = recommendationMapper;
        this.auditLogService = auditLogService;
    }

    @Override
    public PageResultVO<AdminConsoleVO.OrderRow> pageOrders(AdminConsoleDTO.OrderQuery query) {
        Page<AdminConsoleVO.OrderRow> page = mapper.selectOrderPage(
                new Page<>(query.getPageNum(), query.getPageSize()), query.getOrderType(), query.getStatus(),
                query.getUserId(), query.getRiderId(), query.getMerchantId(), query.getStartTime(),
                query.getEndTime(), trim(query.getKeyword()), query.getAbnormalOnly());
        page.getRecords().forEach(this::enrichOrder);
        return pageResult(page);
    }

    @Override
    public AdminConsoleVO.OrderDetail orderDetail(Long orderId) {
        AdminConsoleVO.OrderDetail detail = requireOrderDetail(orderId);
        detail.setItems(mapper.selectOrderItems(orderId));
        detail.setLogs(mapper.selectOrderLogs(orderId));
        enrichOrder(detail);
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminConsoleVO.OrderDetail cancelOrder(
            Long orderId, AdminConsoleDTO.OperationReason request) {
        OrderEntity order = requireOrder(orderId);
        if (OrderStatusEnum.COMPLETED.getCode().equals(order.getStatus())
                || OrderStatusEnum.CANCELLED.getCode().equals(order.getStatus())
                || OrderStatusEnum.WAITING_REFUND.getCode().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_CANNOT_CANCEL);
        }
        int nextStatus = Integer.valueOf(1).equals(order.getPayStatus())
                ? OrderStatusEnum.WAITING_REFUND.getCode() : OrderStatusEnum.CANCELLED.getCode();
        updateOrderStatus(order, nextStatus, request.getReason().trim());
        AdminConsoleVO.OrderDetail result = orderDetail(orderId);
        auditLogService.record("order", "order:cancel", "管理员取消订单", "ORDER", orderId, order, result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminConsoleVO.OrderDetail completeOrder(
            Long orderId, AdminConsoleDTO.OperationReason request) {
        OrderEntity order = requireOrder(orderId);
        if (!MANUAL_COMPLETE_STATUSES.contains(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "当前订单状态不可人工完成");
        }
        updateOrderStatus(order, OrderStatusEnum.COMPLETED.getCode(), request.getReason().trim());
        AdminConsoleVO.OrderDetail result = orderDetail(orderId);
        auditLogService.record("order", "order:complete", "管理员人工完成订单", "ORDER", orderId, order, result);
        return result;
    }

    @Override
    public PageResultVO<AdminConsoleVO.UserRow> pageUsers(AdminConsoleDTO.UserQuery query) {
        Page<AdminConsoleVO.UserRow> page = mapper.selectUserPage(
                new Page<>(query.getPageNum(), query.getPageSize()), query.getStatus(),
                query.getStartTime(), query.getEndTime(), trim(query.getKeyword()));
        return pageResult(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminConsoleVO.UserRow updateUserStatus(Long userId, AdminConsoleDTO.StatusUpdate request) {
        UserEntity user = userMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getIsDeleted())) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        if (userId.equals(UserContext.getUserId()) && Integer.valueOf(0).equals(request.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "不能禁用当前管理员账号");
        }
        Integer before = user.getStatus();
        user.setStatus(request.getStatus());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        AdminConsoleDTO.UserQuery query = new AdminConsoleDTO.UserQuery();
        query.setKeyword(user.getUsername());
        AdminConsoleVO.UserRow result = pageUsers(query).getRecords().stream()
                .filter(item -> userId.equals(item.getUserId())).findFirst().orElseThrow();
        auditLogService.record("user", "user:status", "修改用户状态", "USER", userId, before, request.getStatus());
        return result;
    }

    @Override
    public PageResultVO<AdminConsoleVO.RiderRow> pageRiders(AdminConsoleDTO.RiderQuery query) {
        Page<AdminConsoleVO.RiderRow> page = mapper.selectRiderPage(
                new Page<>(query.getPageNum(), query.getPageSize()), query.getAuditStatus(),
                query.getRiderStatus(), trim(query.getKeyword()));
        page.getRecords().forEach(this::enrichRider);
        return pageResult(page);
    }

    @Override
    public AdminConsoleVO.RiderRow riderDetail(Long riderId) {
        AdminConsoleVO.RiderRow result = mapper.selectRiderDetail(riderId);
        if (result == null) throw new BusinessException(ResultCode.RIDER_NOT_EXIST);
        enrichRider(result);
        return result;
    }

    @Override
    public PageResultVO<AdminConsoleVO.ProductRow> pageProducts(AdminConsoleDTO.ProductQuery query) {
        Page<AdminConsoleVO.ProductRow> page = mapper.selectProductPage(
                new Page<>(query.getPageNum(), query.getPageSize()), query.getStoreId(), query.getCategoryId(),
                query.getProductStatus(), trim(query.getKeyword()), query.getRecommended(), query.getHot());
        return pageResult(page);
    }

    @Override
    public List<AdminConsoleVO.CategoryRow> categories() {
        return mapper.selectCategories();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminConsoleVO.ProductRow updateProductStatus(
            Long productId, AdminConsoleDTO.StatusUpdate request) {
        MerchantProductEntity product = requireProduct(productId);
        if (Integer.valueOf(ProductStatusEnum.ON_SHELF.getCode()).equals(request.getStatus())
                && (product.getStock() == null || product.getStock() <= 0)) {
            throw new BusinessException(ResultCode.PRODUCT_STOCK_NOT_ENOUGH);
        }
        Integer before = product.getProductStatus();
        product.setProductStatus(request.getStatus());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);
        AdminConsoleVO.ProductRow result = findProduct(productId);
        auditLogService.record("product", "product:status", "修改商品上下架状态",
                "PRODUCT", productId, before, request.getStatus());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminConsoleVO.ProductRow updateProductFlags(
            Long productId, AdminConsoleDTO.ProductFlags request) {
        requireProduct(productId);
        updateRecommendationFlag("PRODUCT", productId, request.getRecommended());
        updateRecommendationFlag("HOT_PRODUCT", productId, request.getHot());
        AdminConsoleVO.ProductRow result = findProduct(productId);
        auditLogService.record("product", "product:flags", "修改商品推荐标记",
                "PRODUCT", productId, null, request);
        return result;
    }

    private void updateOrderStatus(OrderEntity order, int nextStatus, String reason) {
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<OrderEntity> update = new LambdaUpdateWrapper<>();
        update.eq(OrderEntity::getId, order.getId())
                .eq(OrderEntity::getStatus, order.getStatus())
                .eq(OrderEntity::getDeleted, 0)
                .set(OrderEntity::getStatus, nextStatus)
                .set(OrderEntity::getUpdateTime, now);
        if (nextStatus == OrderStatusEnum.COMPLETED.getCode()) {
            update.set(OrderEntity::getFinishTime, now);
        }
        if (orderMapper.update(null, update) != 1) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        OrderLogEntity log = new OrderLogEntity();
        log.setOrderId(order.getId());
        log.setOldStatus(order.getStatus());
        log.setNewStatus(nextStatus);
        log.setOperatorId(UserContext.getUserId());
        log.setOperatorType("ADMIN");
        log.setRemark(reason);
        log.setCreateTime(now);
        orderLogMapper.insert(log);
    }

    private void updateRecommendationFlag(String type, Long productId, boolean enabled) {
        LambdaQueryWrapper<HomeRecommendationEntity> query = new LambdaQueryWrapper<>();
        query.eq(HomeRecommendationEntity::getRecommendationType, type)
                .eq(HomeRecommendationEntity::getTargetId, productId);
        HomeRecommendationEntity existing = recommendationMapper.selectOne(query);
        if (!enabled) {
            if (existing != null) recommendationMapper.deleteById(existing.getId());
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            existing = new HomeRecommendationEntity();
            existing.setRecommendationType(type);
            existing.setTargetId(productId);
            existing.setSort(0);
            existing.setStatus(1);
            existing.setUpdateAdminId(UserContext.getUserId());
            existing.setCreateTime(now);
            existing.setUpdateTime(now);
            recommendationMapper.insert(existing);
        } else {
            existing.setStatus(1);
            existing.setUpdateAdminId(UserContext.getUserId());
            existing.setUpdateTime(now);
            recommendationMapper.updateById(existing);
        }
    }

    private AdminConsoleVO.ProductRow findProduct(Long productId) {
        AdminConsoleVO.ProductRow result = mapper.selectProductById(productId);
        if (result == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        }
        return result;
    }

    private OrderEntity requireOrder(Long id) {
        OrderEntity order = id == null ? null : orderMapper.selectById(id);
        if (order == null || Integer.valueOf(1).equals(order.getDeleted())) {
            throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        }
        return order;
    }

    private AdminConsoleVO.OrderDetail requireOrderDetail(Long id) {
        AdminConsoleVO.OrderDetail result = id == null ? null : mapper.selectOrderDetail(id);
        if (result == null) throw new BusinessException(ResultCode.ORDER_NOT_EXIST);
        return result;
    }

    private MerchantProductEntity requireProduct(Long id) {
        MerchantProductEntity product = id == null ? null : productMapper.selectById(id);
        if (product == null) throw new BusinessException(ResultCode.PRODUCT_NOT_EXIST);
        return product;
    }

    private void enrichOrder(AdminConsoleVO.OrderRow order) {
        order.setOrderTypeText(Integer.valueOf(1).equals(order.getOrderType()) ? "商品订单" : "跑腿订单");
        order.setStatusText(OrderStatusEnum.getDescriptionByCode(
                order.getStatus(), order.getOrderType(), order.getPayStatus()));
    }

    private void enrichRider(AdminConsoleVO.RiderRow rider) {
        rider.setAuditStatusText(com.wuxin.enums.RiderAuditStatusEnum.getTextByCode(rider.getAuditStatus()));
        rider.setRiderStatusText(com.wuxin.enums.RiderStatusEnum.getTextByCode(rider.getRiderStatus()));
    }

    private String trim(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private <T> PageResultVO<T> pageResult(Page<T> page) {
        PageResultVO<T> result = new PageResultVO<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        return result;
    }
}
