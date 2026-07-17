package com.wuxin.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PaymentAmountUtils {

    private PaymentAmountUtils() {
    }

    public static Integer yuanToFen(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new ArithmeticException("payment amount must be greater than zero");
        }
        return amount.movePointRight(2)
                .setScale(0, RoundingMode.UNNECESSARY)
                .intValueExact();
    }
}
