import type { CreatePaymentRequest, JsapiPayment, PaymentStatus } from '../types/order';
import { request } from '../utils/request';

export function createJsapiPayment(data: CreatePaymentRequest): Promise<JsapiPayment> {
  return request({ url: '/api/payment/wechat/jsapi', method: 'POST', data });
}

export function getPaymentStatus(orderId: number): Promise<PaymentStatus> {
  return request({ url: `/api/payment/order/${orderId}/status` });
}

export function confirmMockPayment(paymentNo: string): Promise<PaymentStatus> {
  return request({ url: `/api/payment/mock/${paymentNo}/success`, method: 'POST' });
}
