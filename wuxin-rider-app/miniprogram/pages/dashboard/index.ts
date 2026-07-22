import { getMyOrders, getStatistics } from '../../api/rider';
import { ROUTES } from '../../constants/routes';
import { requireApprovedRider, restoreSession } from '../../services/auth';
import { displayName } from '../../utils/format';
import { errorMessage } from '../../utils/request';

const AVAILABILITY_KEY = 'WUXIN_RIDER_LOCAL_AVAILABILITY';

Page({
  data: { loading: true, name: '五鑫骑手', available: true, today: 0, week: 0, month: 0, total: 0, active: 0, waitingConfirm: 0 },
  onShow() { void this.load(); },
  onPullDownRefresh() { void this.load().finally(() => wx.stopPullDownRefresh()); },
  async load() {
    this.setData({ loading: true });
    try {
      if (!await requireApprovedRider()) return;
      const profile = restoreSession().riderProfile;
      if (!profile) return;
      const [stats, active, waiting] = await Promise.all([getStatistics(profile.riderId), getMyOrders(1, 1, 1), getMyOrders(1, 1, 3)]);
      const stored = wx.getStorageSync<boolean>(AVAILABILITY_KEY);
      this.setData({ name: displayName(profile.realName), available: typeof stored === 'boolean' ? stored : true, today: stats.todayCompletedCount, week: stats.weekCompletedCount, month: stats.monthCompletedCount, total: stats.totalCompletedCount, active: active.total, waitingConfirm: waiting.total });
    } catch (error) { wx.showToast({ title: errorMessage(error), icon: 'none' }); }
    finally { this.setData({ loading: false }); }
  },
  onAvailability(event: WechatMiniprogram.SwitchChange) {
    const value = event.detail.value; wx.setStorageSync(AVAILABILITY_KEY, value); this.setData({ available: value });
    wx.showToast({ title: value ? '接单界面已恢复' : '接单界面已暂停', icon: 'none' });
  },
  toHall() { wx.switchTab({ url: ROUTES.hall }); },
  toOrders() { wx.navigateTo({ url: ROUTES.orders }); },
  toStatistics() { wx.navigateTo({ url: ROUTES.statistics }); },
  toRanking() { wx.navigateTo({ url: ROUTES.ranking }); }
});
