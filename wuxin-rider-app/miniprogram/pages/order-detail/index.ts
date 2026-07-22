import { finishOrder, getRiderOrderDetail, giveUpOrder } from '../../api/rider';
import { ORDER_STATUS } from '../../constants/status';
import type { RiderOrderDetail } from '../../types/order';
import { dateTime, money } from '../../utils/format';
import { callPhone, copyText, openLocation } from '../../utils/navigation';
import { errorMessage } from '../../utils/request';

Page({
  data: { id: 0, loading: true, error: '', order: null as RiderOrderDetail | null, amountText: '0.00', createText: '', payText: '', acceptText: '', finishText: '', submitting: false, canOperate: false },
  onLoad(options: Record<string, string>) { const id = Number(options.id); if (!Number.isInteger(id) || id <= 0) { this.setData({ loading: false, error: '订单参数错误' }); return; } this.setData({ id }); },
  onShow() { if (this.data.id > 0) void this.load(); },
  async load() {
    this.setData({ loading: true, error: '' });
    try {
      const order = await getRiderOrderDetail(this.data.id);
      this.setData({ order, amountText: money(order.totalAmount ?? order.price), createText: dateTime(order.createTime), payText: dateTime(order.payTime), acceptText: dateTime(order.acceptTime), finishText: dateTime(order.finishTime), canOperate: order.status === ORDER_STATUS.accepted });
    } catch (error) { this.setData({ error: errorMessage(error) }); }
    finally { this.setData({ loading: false }); }
  },
  callPickup() { callPhone(this.data.order?.pickupPhone); },
  callDelivery() { callPhone(this.data.order?.deliveryPhone); },
  copyPickup() { copyText(this.data.order?.pickupAddress); },
  copyDelivery() { copyText(this.data.order?.deliveryAddress); },
  mapPickup() { const o = this.data.order; if (o) openLocation(o.pickupLatitude, o.pickupLongitude, o.pickupName || '取货地址', o.pickupAddress || ''); },
  mapDelivery() { const o = this.data.order; if (o) openLocation(o.deliveryLatitude, o.deliveryLongitude, o.deliveryName || '送达地址', o.deliveryAddress || ''); },
  async onFinish() {
    if (!this.data.canOperate || this.data.submitting) return;
    const confirm = await wx.showModal({ title: '确认完成配送', content: '确认货物已送达并通知收件人？', confirmColor: '#18a660' }); if (!confirm.confirm) return;
    this.setData({ submitting: true }); try { await finishOrder(this.data.id); wx.showToast({ title: '已提交送达', icon: 'success' }); await this.load(); } catch (error) { wx.showToast({ title: errorMessage(error), icon: 'none' }); } finally { this.setData({ submitting: false }); }
  },
  async onGiveUp() {
    if (!this.data.canOperate || this.data.submitting) return;
    const confirm = await wx.showModal({ title: '放弃配送任务', content: '订单将退回接单大厅，确定继续？', confirmColor: '#e5484d' }); if (!confirm.confirm) return;
    this.setData({ submitting: true }); try { await giveUpOrder(this.data.id); wx.showToast({ title: '订单已退回大厅', icon: 'success' }); setTimeout(() => wx.navigateBack(), 600); } catch (error) { wx.showToast({ title: errorMessage(error), icon: 'none' }); } finally { this.setData({ submitting: false }); }
  }
});
