import { createJsapiPayment } from '../../../api/index';
import { ROUTES } from '../../../constants/routes';
import { requireLogin } from '../../../utils/route-guard';

Page({
  data: { orderId: 0, loading: true, errorMessage: '' },
  async onLoad(options: { orderId?: string }) {
    if (!await requireLogin()) return;
    const orderId = Number(options.orderId);
    if (!Number.isFinite(orderId) || orderId <= 0) { this.setData({ loading: false, errorMessage: '订单ID不合法' }); return; }
    this.setData({ orderId });
    await this.startPayment();
  },
  async startPayment() {
    if (!this.data.orderId) return;
    this.setData({ loading: true, errorMessage: '' });
    try {
      const payment = await createJsapiPayment({ orderId: this.data.orderId });
      await new Promise<void>((resolve, reject) => {
        wx.requestPayment({
          timeStamp: payment.timeStamp,
          nonceStr: payment.nonceStr,
          package: payment.packageValue,
          signType: payment.signType as 'RSA' | 'MD5' | 'HMAC-SHA256',
          paySign: payment.paySign,
          success: () => resolve(),
          fail: (error) => reject(new Error(error.errMsg.includes('cancel') ? '支付已取消' : '支付未完成'))
        });
      });
      wx.redirectTo({ url: `${ROUTES.paymentSuccess}?orderId=${this.data.orderId}` });
    } catch (error) {
      const message = error instanceof Error ? error.message : '支付未完成';
      wx.redirectTo({ url: `${ROUTES.paymentFailure}?orderId=${this.data.orderId}&message=${encodeURIComponent(message)}` });
    } finally {
      this.setData({ loading: false });
    }
  }
});
