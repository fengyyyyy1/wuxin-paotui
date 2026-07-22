package com.wuxin.service;

import com.wuxin.dto.admin.AdminConsoleDTO;
import com.wuxin.vo.PageResultVO;
import com.wuxin.vo.admin.AdminConsoleVO;

import java.util.List;

public interface AdminBusinessService {
    PageResultVO<AdminConsoleVO.OrderRow> pageOrders(AdminConsoleDTO.OrderQuery query);
    AdminConsoleVO.OrderDetail orderDetail(Long orderId);
    AdminConsoleVO.OrderDetail cancelOrder(Long orderId, AdminConsoleDTO.OperationReason request);
    AdminConsoleVO.OrderDetail completeOrder(Long orderId, AdminConsoleDTO.OperationReason request);
    PageResultVO<AdminConsoleVO.UserRow> pageUsers(AdminConsoleDTO.UserQuery query);
    AdminConsoleVO.UserRow updateUserStatus(Long userId, AdminConsoleDTO.StatusUpdate request);
    PageResultVO<AdminConsoleVO.RiderRow> pageRiders(AdminConsoleDTO.RiderQuery query);
    AdminConsoleVO.RiderRow riderDetail(Long riderId);
    PageResultVO<AdminConsoleVO.ProductRow> pageProducts(AdminConsoleDTO.ProductQuery query);
    List<AdminConsoleVO.CategoryRow> categories();
    AdminConsoleVO.ProductRow updateProductStatus(Long productId, AdminConsoleDTO.StatusUpdate request);
    AdminConsoleVO.ProductRow updateProductFlags(Long productId, AdminConsoleDTO.ProductFlags request);
}
