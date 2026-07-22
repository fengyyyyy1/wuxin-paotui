export interface OrderItem { productId: number; productName: string; productImage: string | null; productPrice: number; quantity: number; subtotal: number; }

export interface RiderOrder {
  id: number;
  orderNo: string;
  goodsName: string | null;
  goodsDescription: string | null;
  goodsSummary: string | null;
  weight: number | null;
  distance: number | null;
  price: number;
  pickupAddressId: number | null;
  deliveryAddressId: number;
  pickupAddress: string | null;
  deliveryAddress: string | null;
  orderType: number;
  orderTypeText: string;
  storeId: number | null;
  storeName: string | null;
  status: number;
  statusText: string;
  payStatus: number;
  payStatusText: string;
  acceptTime?: string | null;
  finishTime?: string | null;
  createTime: string;
}

export interface RiderOrderTimeline {
  oldStatus: number;
  oldStatusText: string;
  newStatus: number;
  newStatusText: string;
  operatorType: string;
  remark: string;
  createTime: string;
}

export interface RiderOrderDetail extends RiderOrder {
  items: OrderItem[];
  productAmount: number | null;
  deliveryFee: number | null;
  totalAmount: number | null;
  remark: string | null;
  pickupName: string | null;
  pickupPhone: string | null;
  pickupLatitude: number | null;
  pickupLongitude: number | null;
  deliveryName: string | null;
  deliveryPhone: string | null;
  deliveryLatitude: number | null;
  deliveryLongitude: number | null;
  payTime: string | null;
  timeline: RiderOrderTimeline[];
}
