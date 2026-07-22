import { getRanking } from '../../api/rider';
import { requireApprovedRider, restoreSession } from '../../services/auth';
import type { RiderRanking } from '../../types/rider';
import { errorMessage } from '../../utils/request';

const tabs = [{ label: '今日', type: 'today' }, { label: '本周', type: 'week' }, { label: '本月', type: 'month' }, { label: '累计', type: 'total' }] as const;

Page({
  data: { tabs, active: 0, items: [] as RiderRanking[], loading: true, error: '', currentRiderId: 0, currentRank: 0 },
  onShow() { void this.load(); },
  onPullDownRefresh() { void this.load().finally(() => wx.stopPullDownRefresh()); },
  onTab(event: WechatMiniprogram.TouchEvent) { this.setData({ active: Number(event.currentTarget.dataset.index) }); void this.load(); },
  async load() {
    this.setData({ loading: true, error: '' });
    try { if (!await requireApprovedRider()) return; const profile = restoreSession().riderProfile; if (!profile) return; const items = await getRanking(tabs[this.data.active].type); const current = items.find(item => item.riderId === profile.riderId); this.setData({ items, currentRiderId: profile.riderId, currentRank: current?.rank || 0 }); }
    catch (error) { this.setData({ error: errorMessage(error) }); }
    finally { this.setData({ loading: false }); }
  }
});
