package com.wuxin.controller;

import com.wuxin.annotation.AdminPermission;
import com.wuxin.common.Result;
import com.wuxin.dto.admin.AdminConsoleDTO;
import com.wuxin.service.AdminBusinessService;
import com.wuxin.vo.PageResultVO;
import com.wuxin.vo.admin.AdminConsoleVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@AdminPermission("order:view")
public class AdminOrderController {
    private final AdminBusinessService service;

    public AdminOrderController(AdminBusinessService service) {
        this.service = service;
    }

    @GetMapping
    public Result<PageResultVO<AdminConsoleVO.OrderRow>> page(@Valid AdminConsoleDTO.OrderQuery query) {
        return Result.success(service.pageOrders(query));
    }

    @GetMapping("/{id}")
    public Result<AdminConsoleVO.OrderDetail> detail(@PathVariable Long id) {
        return Result.success(service.orderDetail(id));
    }

    @PostMapping("/{id}/cancel")
    @AdminPermission("order:manage")
    public Result<AdminConsoleVO.OrderDetail> cancel(
            @PathVariable Long id, @Valid @RequestBody AdminConsoleDTO.OperationReason request) {
        return Result.success("订单已取消", service.cancelOrder(id, request));
    }

    @PostMapping("/{id}/complete")
    @AdminPermission("order:manage")
    public Result<AdminConsoleVO.OrderDetail> complete(
            @PathVariable Long id, @Valid @RequestBody AdminConsoleDTO.OperationReason request) {
        return Result.success("订单已人工完成", service.completeOrder(id, request));
    }
}
