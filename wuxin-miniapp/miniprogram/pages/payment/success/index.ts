import { getPaymentStatus } from '../../../api/index';
import { ROUTES } from '../../../constants/routes';
import type { PaymentStatus } from '../../../types/order';
import { formatMoney } from '../../../utils/format';
import { formatDateTime } from '../../../utils/order';

Page({
  data: { orderId: 0, payment: null as PaymentStatus | null, amountText: '0.00', successTimeText: '', loading: true },
  async onLoad(options: { orderId?: string }) {
    const orderId = Number(options.orderId);
    this.setData({ orderId });
    if (orderId > 0) await this.loadStatus(); else this.setData({ loading: false });
  },
  async loadStatus() {
    try {
      const payment = await getPaymentStatus(this.data.orderId);
      this.setData({ payment, amountText: formatMoney((payment.amountTotal || 0) / 100), successTimeText: formatDateTime(payment.successTime) });
    } catch {
      this.setData({ payment: null });
    } finally {
      this.setData({ loading: false });
    }
  },
  viewOrder() { wx.redirectTo({ url: `${ROUTES.orderDetail}?id=${this.data.orderId}` }); },
  backHome() { wx.switchTab({ url: ROUTES.home }); }
});
