package com.wuxin.controller;

import com.wuxin.common.Result;
import com.wuxin.dto.payment.CreateJsapiPaymentDTO;
import com.wuxin.service.PaymentService;
import com.wuxin.vo.JsapiPaymentVO;
import com.wuxin.vo.PaymentStatusVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/wechat/jsapi")
    public Result<JsapiPaymentVO> createJsapiPayment(
            @Valid @RequestBody CreateJsapiPaymentDTO request) {
        return Result.success("支付单创建成功", paymentService.createJsapiPayment(request));
    }

    @GetMapping("/order/{orderId}/status")
    public Result<PaymentStatusVO> getOrderPaymentStatus(@PathVariable Long orderId) {
        return Result.success(paymentService.getOrderPaymentStatus(orderId));
    }
}
