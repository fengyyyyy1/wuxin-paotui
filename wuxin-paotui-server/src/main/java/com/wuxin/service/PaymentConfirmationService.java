package com.wuxin.service;

import com.wuxin.service.model.PaymentSuccessCommand;
import com.wuxin.vo.PaymentStatusVO;

public interface PaymentConfirmationService {

    PaymentStatusVO confirmPaymentSuccess(PaymentSuccessCommand command);
}
