import type {
  CreateCartOrderRequest,
  CreateCartOrderResponse,
  OrderDetail,
  OrderPage,
  OrderPageQuery,
  OrderTimeline,
  SettlementPreview,
  SettlementPreviewRequest
} from '../types/order';
import { request } from '../utils/request';

export function previewSettlement(data: SettlementPreviewRequest): Promise<SettlementPreview> {
  return request({ url: '/api/order/settlement/preview', method: 'POST', data });
}

export function createOrderFromCart(data: CreateCartOrderRequest): Promise<CreateCartOrderResponse> {
  return request({ url: '/api/order/create-from-cart', method: 'POST', data });
}

export function getMyOrders(data: OrderPageQuery): Promise<OrderPage> {
  return request({ url: '/api/order/my', data });
}

export function getOrderDetail(orderId: number): Promise<OrderDetail> {
  return request({ url: `/api/order/${orderId}` });
}

export function getOrderTimeline(orderId: number): Promise<OrderTimeline> {
  return request({ url: `/api/order/timeline/${orderId}` });
}

export function cancelOrder(orderId: number): Promise<unknown> {
  return request({ url: `/api/order/cancel/${orderId}`, method: 'POST' });
}

export function confirmOrder(orderId: number): Promise<unknown> {
  return request({ url: `/api/order/confirm/${orderId}`, method: 'POST' });
}
