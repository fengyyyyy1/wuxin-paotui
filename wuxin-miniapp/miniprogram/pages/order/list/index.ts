import { cancelOrder, getMyOrders } from '../../../api/index';
import { ROUTES } from '../../../constants/routes';
import { STORAGE_KEYS } from '../../../constants/storage';
import type { OrderListItem, OrderStatus } from '../../../types/order';
import { formatMoney } from '../../../utils/format';
import {
  canCancelOrder,
  canPayOrder,
  formatDateTime,
  getOrderStatusTone
} from '../../../utils/order';
import { requireLogin } from '../../../utils/route-guard';

type OrderFilter = 'all' | 'pay' | 'waiting' | 'delivery' | 'completed' | 'cancelled';

interface OrderDisplay extends OrderListItem {
  amountText: string;
  createTimeText: string;
  statusTone: string;
  canPay: boolean;
  canCancel: boolean;
  summaryText: string;
}

const FILTERS: Array<{ key: OrderFilter; label: string }> = [
  { key: 'all', label: '全部' },
  { key: 'pay', label: '待付款' },
  { key: 'waiting', label: '待接单' },
  { key: 'delivery', label: '配送中' },
  { key: 'completed', label: '已完成' },
  { key: 'cancelled', label: '已取消' }
];

Page({
  data: {
    filters: FILTERS,
    activeFilter: 'all' as OrderFilter,
    orders: [] as OrderDisplay[],
    loading: true,
    errorMessage: '',
    operatingId: 0
  },
  onLoad(options: { tab?: string; status?: string }) {
    this.setData({ activeFilter: normalizeOrderFilter(options.tab || options.status) });
  },
  async onShow() {
    const pendingFilter = wx.getStorageSync(STORAGE_KEYS.orderFilter);
    if (pendingFilter) {
      this.setData({ activeFilter: normalizeOrderFilter(String(pendingFilter)) });
      wx.removeStorageSync(STORAGE_KEYS.orderFilter);
    }
    if (await requireLogin()) await this.loadOrders();
  },
  async onPullDownRefresh() {
    await this.loadOrders();
    wx.stopPullDownRefresh();
  },

  async loadOrders() {
    if (this.data.operatingId) return;
    this.setData({ loading: true, errorMessage: '' });
    try {
      const statuses = getFilterStatuses(this.data.activeFilter);
      const pages = await Promise.all(
        statuses.map((status) => {
          const query =
            status === null ? { pageNum: 1, pageSize: 100 } : { pageNum: 1, pageSize: 100, status };
          return getMyOrders(query);
        })
      );
      const unique = new Map<number, OrderListItem>();
      pages
        .flatMap((page) => page.records)
        .filter((order) => matchesFilter(order, this.data.activeFilter))
        .forEach((order) => unique.set(order.id, order));
      const orders = [...unique.values()]
        .sort((left, right) => right.createTime.localeCompare(left.createTime))
        .map(toOrderDisplay);
      this.setData({ orders });
    } catch (error) {
      this.setData({ errorMessage: error instanceof Error ? error.message : '订单加载失败' });
    } finally {
      this.setData({ loading: false });
    }
  },

  selectFilter(event: WechatMiniprogram.BaseEvent) {
    const filter = normalizeOrderFilter(String(event.currentTarget.dataset.key || ''));
    if (filter === this.data.activeFilter) return;
    this.setData({ activeFilter: filter, orders: [] });
    void this.loadOrders();
  },

  viewOrder(event: WechatMiniprogram.BaseEvent) {
    const id = Number(event.currentTarget.dataset.id);
    if (id) wx.navigateTo({ url: `${ROUTES.orderDetail}?id=${id}` });
  },

  payOrder(event: WechatMiniprogram.BaseEvent) {
    const id = Number(event.currentTarget.dataset.id);
    if (id) wx.navigateTo({ url: `${ROUTES.paymentProcessing}?orderId=${id}` });
  },

  cancelOrder(event: WechatMiniprogram.BaseEvent) {
    const id = Number(event.currentTarget.dataset.id);
    if (!id || this.data.operatingId) return;
    wx.showModal({
      title: '取消订单',
      content: '确认取消该订单？',
      confirmText: '取消订单',
      confirmColor: '#FF4D4F',
      success: (result) => {
        if (result.confirm) void this.performCancel(id);
      }
    });
  },

  async performCancel(id: number) {
    this.setData({ operatingId: id });
    try {
      await cancelOrder(id);
      wx.showToast({ title: '订单已取消', icon: 'success' });
    } catch (error) {
      wx.showToast({ title: error instanceof Error ? error.message : '取消失败', icon: 'none' });
    } finally {
      this.setData({ operatingId: 0 });
    }
    await this.loadOrders();
  },

  retry() {
    void this.loadOrders();
  },
  goShopping() {
    wx.switchTab({ url: ROUTES.home });
  }
});

function normalizeOrderFilter(value?: string): OrderFilter {
  return FILTERS.some((filter) => filter.key === value) ? (value as OrderFilter) : 'all';
}

function getFilterStatuses(filter: OrderFilter): Array<OrderStatus | null> {
  if (filter === 'all') return [null];
  if (filter === 'pay') return [0];
  if (filter === 'waiting') return [0, 6, 7];
  if (filter === 'delivery') return [1, 2, 3];
  if (filter === 'completed') return [4];
  return [5, 8];
}

function matchesFilter(order: OrderListItem, filter: OrderFilter): boolean {
  if (filter === 'pay') return order.status === 0 && order.payStatus === 0;
  if (filter === 'waiting') return order.payStatus === 1 && [0, 6, 7].includes(order.status);
  return true;
}

function toOrderDisplay(order: OrderListItem): OrderDisplay {
  return {
    ...order,
    amountText: formatMoney(order.price),
    createTimeText: formatDateTime(order.createTime),
    statusTone: getOrderStatusTone(order.status, order.payStatus),
    canPay: canPayOrder(order.status, order.payStatus),
    canCancel: canCancelOrder(order.status),
    summaryText:
      order.goodsName || (order.goodsDescription ? order.goodsDescription : '商品配送订单')
  };
}
