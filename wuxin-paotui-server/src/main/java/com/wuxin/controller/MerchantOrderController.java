package com.wuxin.controller;

import com.wuxin.common.Result;
import com.wuxin.dto.merchant.MerchantOrderPageQueryDTO;
import com.wuxin.dto.merchant.RejectMerchantOrderDTO;
import com.wuxin.service.MerchantOrderService;
import com.wuxin.vo.MerchantOrderDetailVO;
import com.wuxin.vo.MerchantOrderPageVO;
import com.wuxin.vo.MerchantOrderStatusVO;
import com.wuxin.vo.PageResultVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchant/order")
public class MerchantOrderController {

    private final MerchantOrderService merchantOrderService;

    public MerchantOrderController(MerchantOrderService merchantOrderService) {
        this.merchantOrderService = merchantOrderService;
    }

    @GetMapping("/page")
    public Result<PageResultVO<MerchantOrderPageVO>> page(
            @Valid @ModelAttribute MerchantOrderPageQueryDTO query) {
        return Result.success(merchantOrderService.pageOrders(query));
    }

    @GetMapping("/{id}")
    public Result<MerchantOrderDetailVO> detail(@PathVariable Long id) {
        return Result.success(merchantOrderService.getOrderDetail(id));
    }

    @PostMapping("/{id}/accept")
    public Result<MerchantOrderStatusVO> accept(@PathVariable Long id) {
        return Result.success(
                "商家接单成功",
                merchantOrderService.acceptOrder(id));
    }

    @PostMapping("/{id}/reject")
    public Result<MerchantOrderStatusVO> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectMerchantOrderDTO request) {
        return Result.success(
                "商家拒单已受理，等待退款处理",
                merchantOrderService.rejectOrder(id, request));
    }

    @PostMapping("/{id}/ready")
    public Result<MerchantOrderStatusVO> ready(@PathVariable Long id) {
        return Result.success(
                "商家出餐成功",
                merchantOrderService.readyOrder(id));
    }
}
