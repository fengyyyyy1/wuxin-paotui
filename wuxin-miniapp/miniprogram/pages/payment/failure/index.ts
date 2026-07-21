import { ROUTES } from '../../../constants/routes';

Page({
  data: { orderId: 0, message: '支付未完成' },
  onLoad(options: { orderId?: string; message?: string }) {
    this.setData({ orderId: Number(options.orderId) || 0, message: decodeURIComponent(options.message || '支付未完成') });
  },
  retryPayment() { if (this.data.orderId) wx.redirectTo({ url: `${ROUTES.paymentProcessing}?orderId=${this.data.orderId}` }); },
  viewOrder() { if (this.data.orderId) wx.redirectTo({ url: `${ROUTES.orderDetail}?id=${this.data.orderId}` }); },
  backHome() { wx.switchTab({ url: ROUTES.home }); }
});
