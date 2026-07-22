import { getMyOrders } from '../../api/rider';
import { ROUTES } from '../../constants/routes';
import { requireApprovedRider } from '../../services/auth';
import type { RiderOrder } from '../../types/order';
import { dateTime, money } from '../../utils/format';
import { errorMessage } from '../../utils/request';

interface OrderView extends RiderOrder { amountText: string; createText: string; }
const tabs: Array<{ label: string; status?: number }> = [{ label: '全部' }, { label: '配送任务', status: 1 }, { label: '待确认', status: 3 }, { label: '已完成', status: 4 }, { label: '异常/取消', status: 5 }];

Page({
  data: { tabs, active: 0, items: [] as OrderView[], loading: true, loadingMore: false, error: '', pageNum: 1, pages: 1 },
  onLoad(options: Record<string, string>) { const index = Number(options.tab || 0); this.setData({ active: Number.isInteger(index) && index >= 0 && index < tabs.length ? index : 0 }); },
  onShow() { void this.reload(); },
  onPullDownRefresh() { void this.reload().finally(() => wx.stopPullDownRefresh()); },
  onReachBottom() { if (this.data.pageNum < this.data.pages && !this.data.loadingMore) void this.loadPage(this.data.pageNum + 1, true); },
  onTab(event: WechatMiniprogram.TouchEvent) { this.setData({ active: Number(event.currentTarget.dataset.index) }); void this.reload(); },
  async reload() { this.setData({ items: [], pageNum: 1, pages: 1, error: '' }); await this.loadPage(1, false); },
  async loadPage(pageNum: number, append: boolean) {
    if (!await requireApprovedRider()) return;
    this.setData(append ? { loadingMore: true } : { loading: true });
    try {
      const result = await getMyOrders(pageNum, 10, tabs[this.data.active].status);
      const items = result.records.map(item => ({ ...item, amountText: money(item.price), createText: dateTime(item.createTime) }));
      this.setData({ items: append ? [...this.data.items, ...items] : items, pageNum: result.pageNum, pages: result.pages, error: '' });
    } catch (error) { this.setData({ error: errorMessage(error) }); }
    finally { this.setData({ loading: false, loadingMore: false }); }
  },
  toDetail(event: WechatMiniprogram.TouchEvent) { wx.navigateTo({ url: `${ROUTES.detail}?id=${Number(event.currentTarget.dataset.id)}` }); }
});
