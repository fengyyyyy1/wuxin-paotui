import type { PageResult } from './common';

export type OrderStatus = 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8;
export type PayStatus = 0 | 1;

export interface SettlementPreviewRequest {
  deliveryAddressId: number;
}

export interface SettlementItem {
  productId: number;
  productName: string;
  productImage: string | null;
  price: number | string;
  quantity: number;
  subtotal: number | string;
  stock: number;
}

export interface SettlementPreview {
  storeId: number;
  storeName: string;
  deliveryAddressId: number;
  items: SettlementItem[];
  productAmount: number | string;
  deliveryFee: number | string;
  totalAmount: number | string;
  selectedProductCount: number;
}

export interface CreateCartOrderRequest {
  deliveryAddressId: number;
  remark?: string;
}

export interface CreateErrandOrderRequest {
  pickupAddressId: number;
  deliveryAddressId: number;
  goodsName: string;
  goodsDescription?: string;
  weight: number;
  distance: number;
  price: number;
  remark?: string;
}

export interface CreateCartOrderResponse {
  orderId: number;
  orderNo: string;
  orderType: number;
  storeId: number;
  productAmount: number | string;
  deliveryFee: number | string;
  totalAmount: number | string;
  payStatus: PayStatus;
  status: OrderStatus;
  itemCount: number;
}

export interface OrderListItem {
  id: number;
  orderNo: string;
  pickupAddressId: number | null;
  deliveryAddressId: number | null;
  goodsName: string | null;
  goodsDescription: string | null;
  weight: number | string | null;
  distance: number | string | null;
  price: number | string;
  status: OrderStatus;
  statusText: string;
  payStatus: PayStatus;
  payStatusText: string;
  payTime: string | null;
  paymentNo: string | null;
  remark: string | null;
  createTime: string;
  updateTime: string;
}

export interface OrderItem {
  productId: number;
  productName: string;
  productImage: string | null;
  productPrice: number | string;
  quantity: number;
  subtotal: number | string;
}

export interface OrderDetail extends OrderListItem {
  orderType: number;
  orderTypeText: string;
  storeId: number | null;
  storeName: string | null;
  productAmount: number | string | null;
  deliveryFee: number | string | null;
  totalAmount: number | string | null;
  items: OrderItem[];
}

export interface OrderTimelineItem {
  type: string;
  title: string;
  description: string | null;
  time: string;
  sort: number;
}

export interface OrderTimeline {
  orderId: number;
  orderNo: string;
  status: OrderStatus;
  statusText: string;
  payStatus: PayStatus;
  payStatusText: string;
  timeline: OrderTimelineItem[];
}

export interface OrderPageQuery {
  pageNum?: number;
  pageSize?: number;
  status?: OrderStatus;
}

export type OrderPage = PageResult<OrderListItem>;

export interface CreatePaymentRequest {
  orderId: number;
}

export interface JsapiPayment {
  paymentNo: string;
  timeStamp: string;
  nonceStr: string;
  packageValue: string;
  signType: 'RSA' | 'MD5' | 'HMAC-SHA256' | string;
  paySign: string;
}

export interface PaymentStatus {
  orderId: number;
  orderNo: string;
  payStatus: PayStatus;
  paymentNo: string | null;
  paymentStatus: number | null;
  paymentStatusText: string | null;
  transactionId: string | null;
  amountTotal: number | null;
  successTime: string | null;
}
