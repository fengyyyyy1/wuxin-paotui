package com.wuxin.controller;

import com.wuxin.common.Result;
import com.wuxin.dto.order.CreateOrderDTO;
import com.wuxin.service.OrderService;
import com.wuxin.vo.OrderDetailVO;
import com.wuxin.vo.OrderListVO;
import com.wuxin.vo.PageResultVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody CreateOrderDTO createOrderDTO) {
        return Result.success(orderService.createOrder(createOrderDTO));
    }

    @GetMapping("/my")
    public Result<PageResultVO<OrderListVO>> myOrders(
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "status", required = false) Integer status) {
        return Result.success(orderService.getMyOrders(pageNum, pageSize, status));
    }

    @GetMapping("/{id}")
    public Result<OrderDetailVO> detail(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id));
    }
}
