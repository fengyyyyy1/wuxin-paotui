import { acceptOrder, getHall } from '../../api/rider';
import { ROUTES } from '../../constants/routes';
import { requireApprovedRider } from '../../services/auth';
import type { RiderOrder } from '../../types/order';
import { dateTime, money } from '../../utils/format';
import { errorMessage } from '../../utils/request';

interface HallView extends RiderOrder { amountText: string; createText: string; distanceText: string; accepting: boolean; }

Page({
  data: { items: [] as HallView[], loading: true, loadingMore: false, error: '', pageNum: 1, pages: 1, available: true },
  onShow() { this.setData({ available: wx.getStorageSync<boolean>('WUXIN_RIDER_LOCAL_AVAILABILITY') !== false }); void this.reload(); },
  onPullDownRefresh() { void this.reload().finally(() => wx.stopPullDownRefresh()); },
  onReachBottom() { if (this.data.pageNum < this.data.pages && !this.data.loadingMore) void this.loadPage(this.data.pageNum + 1, true); },
  async reload() { this.setData({ pageNum: 1, pages: 1, items: [], error: '' }); await this.loadPage(1, false); },
  async loadPage(pageNum: number, append: boolean) {
    if (!await requireApprovedRider()) return;
    this.setData(append ? { loadingMore: true } : { loading: true });
    try {
      const result = await getHall(pageNum);
      const items = result.records.map(item => ({ ...item, amountText: money(item.price), createText: dateTime(item.createTime), distanceText: item.distance == null ? '距离待确认' : `${item.distance} km`, accepting: false }));
      this.setData({ items: append ? [...this.data.items, ...items] : items, pageNum: result.pageNum, pages: result.pages, error: '' });
    } catch (error) { this.setData({ error: errorMessage(error) }); }
    finally { this.setData({ loading: false, loadingMore: false }); }
  },
  async onAccept(event: WechatMiniprogram.TouchEvent) {
    if (!this.data.available) { wx.showToast({ title: '请先开启接单界面', icon: 'none' }); return; }
    const id = Number(event.currentTarget.dataset.id);
    const index = this.data.items.findIndex(item => item.id === id);
    if (index < 0 || this.data.items[index].accepting) return;
    this.setData({ [`items[${index}].accepting`]: true });
    try { await acceptOrder(id); this.setData({ items: this.data.items.filter(item => item.id !== id) }); wx.showToast({ title: '接单成功', icon: 'success' }); setTimeout(() => wx.navigateTo({ url: `${ROUTES.detail}?id=${id}` }), 500); }
    catch (error) { wx.showToast({ title: errorMessage(error), icon: 'none' }); await this.reload(); }
    finally { const next = this.data.items.findIndex(item => item.id === id); if (next >= 0) this.setData({ [`items[${next}].accepting`]: false }); }
  }
});
