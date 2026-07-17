package com.wuxin.controller;

import com.wuxin.common.Result;
import com.wuxin.service.PaymentService;
import com.wuxin.vo.PaymentStatusVO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment/mock")
@ConditionalOnProperty(
        prefix = "wuxin.mock-payment",
        name = "enabled",
        havingValue = "true")
public class MockPaymentController {

    private final PaymentService paymentService;

    public MockPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{paymentNo}/success")
    public Result<PaymentStatusVO> confirmSuccess(@PathVariable String paymentNo) {
        return Result.success(
                "模拟支付成功",
                paymentService.confirmMockPaymentSuccess(paymentNo));
    }
}
