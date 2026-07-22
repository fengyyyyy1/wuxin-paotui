import { getMerchantOrders } from '../../api/orders';
import { ROUTES } from '../../constants/routes';
import { requireApprovedMerchant } from '../../services/auth';
import type { MerchantOrder } from '../../types/order';
import { dateTime, money } from '../../utils/format';
import { errorMessage } from '../../utils/request';

interface OrderView extends MerchantOrder { amountText: string; createText: string; }
const tabs: Array<{ label: string; status?: number }> = [{ label: '全部' }, { label: '待接单', status: 0 }, { label: '制作中', status: 6 }, { label: '待骑手', status: 7 }, { label: '配送中', status: 1 }, { label: '待确认', status: 3 }, { label: '已完成', status: 4 }, { label: '待退款', status: 8 }, { label: '已取消', status: 5 }];

Page({
  data: { tabs, active: 0, keyword: '', items: [] as OrderView[], loading: true, loadingMore: false, error: '', pageNum: 1, pages: 1 },
  onShow() { void this.reload(); }, onPullDownRefresh() { void this.reload().finally(() => wx.stopPullDownRefresh()); }, onReachBottom() { if (this.data.pageNum < this.data.pages && !this.data.loadingMore) void this.loadPage(this.data.pageNum + 1, true); },
  onKeyword(event: WechatMiniprogram.Input) { this.setData({ keyword: event.detail.value }); }, onSearch() { void this.reload(); }, onClear() { this.setData({ keyword: '' }); void this.reload(); }, onTab(event: WechatMiniprogram.TouchEvent) { this.setData({ active: Number(event.currentTarget.dataset.index) }); void this.reload(); },
  async reload() { this.setData({ items: [], pageNum: 1, pages: 1, error: '' }); await this.loadPage(1, false); },
  async loadPage(pageNum: number, append: boolean) { if (!await requireApprovedMerchant()) return; this.setData(append ? { loadingMore: true } : { loading: true }); try { const result = await getMerchantOrders({ pageNum, pageSize: 10, status: tabs[this.data.active].status, keyword: this.data.keyword.trim() || undefined }); const items = result.records.map(item => ({ ...item, amountText: money(item.totalAmount), createText: dateTime(item.createTime) })); this.setData({ items: append ? [...this.data.items, ...items] : items, pageNum: result.pageNum, pages: result.pages, error: '' }); } catch (error) { this.setData({ error: errorMessage(error) }); } finally { this.setData({ loading: false, loadingMore: false }); } },
  toDetail(event: WechatMiniprogram.TouchEvent) { wx.navigateTo({ url: `${ROUTES.orderDetail}?id=${Number(event.currentTarget.dataset.id)}` }); }
});
