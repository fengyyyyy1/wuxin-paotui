import { cancelOrder, confirmOrder, getAddressList, getOrderDetail, getOrderTimeline } from '../../../api/index';
import { ROUTES } from '../../../constants/routes';
import type { Address } from '../../../types/address';
import type { OrderDetail, OrderItem, OrderTimelineItem } from '../../../types/order';
import { buildFullAddress } from '../../../utils/address';
import { formatMoney } from '../../../utils/format';
import { DEFAULT_PRODUCT_IMAGE, normalizeImageUrl } from '../../../utils/image';
import { canCancelOrder, canConfirmOrder, canPayOrder, formatDateTime, getOrderStatusTone } from '../../../utils/order';
import { maskPhone } from '../../../utils/phone';
import { requireLogin } from '../../../utils/route-guard';

interface DetailItem extends OrderItem { imageUrl: string; priceText: string; subtotalText: string; }
interface DetailAddress extends Address { fullAddress: string; maskedPhone: string; }

Page({
  data: {
    orderId: 0,
    order: null as OrderDetail | null,
    items: [] as DetailItem[],
    timeline: [] as Array<OrderTimelineItem & { timeText: string }>,
    address: null as DetailAddress | null,
    amountText: '0.00', productAmountText: '0.00', deliveryFeeText: '0.00',
    createTimeText: '', statusTone: 'brand', canPay: false, canCancel: false, canConfirm: false,
    loading: true, errorMessage: '', operating: false
  },

  async onLoad(options: { id?: string }) {
    if (!await requireLogin()) return;
    const orderId = Number(options.id);
    if (!Number.isFinite(orderId) || orderId <= 0) { this.setData({ loading: false, errorMessage: '订单ID不合法' }); return; }
    this.setData({ orderId });
    await this.loadDetail();
  },

  async onPullDownRefresh() { await this.loadDetail(); wx.stopPullDownRefresh(); },

  async loadDetail() {
    this.setData({ loading: true, errorMessage: '' });
    try {
      const [order, timeline, addresses] = await Promise.all([getOrderDetail(this.data.orderId), getOrderTimeline(this.data.orderId), getAddressList()]);
      const address = addresses.find((item) => item.id === order.deliveryAddressId) || null;
      this.setData({
        order,
        items: (order.items || []).map(toDetailItem),
        timeline: timeline.timeline.map((item) => ({ ...item, timeText: formatDateTime(item.time) })),
        address: address ? { ...address, fullAddress: buildFullAddress(address), maskedPhone: maskPhone(address.receiverPhone) } : null,
        amountText: formatMoney(order.totalAmount ?? order.price),
        productAmountText: formatMoney(order.productAmount),
        deliveryFeeText: formatMoney(order.deliveryFee),
        createTimeText: formatDateTime(order.createTime),
        statusTone: getOrderStatusTone(order.status, order.payStatus),
        canPay: canPayOrder(order.status, order.payStatus),
        canCancel: canCancelOrder(order.status),
        canConfirm: canConfirmOrder(order.status)
      });
    } catch (error) {
      this.setData({ errorMessage: error instanceof Error ? error.message : '订单详情加载失败' });
    } finally { this.setData({ loading: false }); }
  },

  payOrder() { wx.navigateTo({ url: `${ROUTES.paymentProcessing}?orderId=${this.data.orderId}` }); },
  cancelOrder() { this.confirmAction('取消订单', '确认取消该订单？', () => cancelOrder(this.data.orderId)); },
  confirmReceipt() { this.confirmAction('确认收货', '确认已经收到商品？', () => confirmOrder(this.data.orderId)); },

  confirmAction(title: string, content: string, action: () => Promise<unknown>) {
    if (this.data.operating) return;
    wx.showModal({ title, content, confirmText: '确认', success: (result) => { if (result.confirm) void this.runAction(action); } });
  },

  async runAction(action: () => Promise<unknown>) {
    this.setData({ operating: true });
    try { await action(); wx.showToast({ title: '操作成功', icon: 'success' }); await this.loadDetail(); }
    catch (error) { wx.showToast({ title: error instanceof Error ? error.message : '操作失败', icon: 'none' }); }
    finally { this.setData({ operating: false }); }
  },

  handleImageError(event: WechatMiniprogram.BaseEvent) {
    const id = Number(event.currentTarget.dataset.id);
    this.setData({ items: this.data.items.map((item) => item.productId === id ? { ...item, imageUrl: DEFAULT_PRODUCT_IMAGE } : item) });
  },
  retry() { void this.loadDetail(); }
});

function toDetailItem(item: OrderItem): DetailItem {
  return { ...item, imageUrl: normalizeImageUrl(item.productImage, DEFAULT_PRODUCT_IMAGE), priceText: formatMoney(item.productPrice), subtotalText: formatMoney(item.subtotal) };
}
