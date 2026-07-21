import type { OrderStatus } from '../types/order';

export const ORDER_FILTERS: Array<{ label: string; value: OrderStatus | null }> = [
  { label: '全部', value: null },
  { label: '待付款', value: 0 },
  { label: '待接单', value: 6 },
  { label: '配送中', value: 1 },
  { label: '已完成', value: 4 },
  { label: '已取消', value: 5 }
];

export function formatDateTime(value: string | null | undefined): string {
  if (!value) return '';
  return value.replace('T', ' ').slice(0, 16);
}

export function getOrderStatusTone(status: OrderStatus, payStatus: 0 | 1): string {
  if (payStatus === 0 && status === 0) return 'warning';
  if (status === 4) return 'success';
  if (status === 5 || status === 8) return 'muted';
  return 'brand';
}

export function canCancelOrder(status: OrderStatus): boolean {
  return status === 0;
}

export function canConfirmOrder(status: OrderStatus): boolean {
  return status === 3;
}

export function canPayOrder(status: OrderStatus, payStatus: 0 | 1): boolean {
  return status === 0 && payStatus === 0;
}
