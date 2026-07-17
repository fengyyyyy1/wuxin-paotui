package com.wuxin.service;

import com.wuxin.dto.payment.CreateJsapiPaymentDTO;
import com.wuxin.vo.JsapiPaymentVO;
import com.wuxin.vo.PaymentStatusVO;

public interface PaymentService {

    JsapiPaymentVO createJsapiPayment(CreateJsapiPaymentDTO request);

    PaymentStatusVO getOrderPaymentStatus(Long orderId);

    PaymentStatusVO confirmMockPaymentSuccess(String paymentNo);
}
