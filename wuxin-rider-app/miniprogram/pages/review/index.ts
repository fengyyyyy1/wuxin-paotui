import { ROUTES } from '../../constants/routes';
import { RIDER_AUDIT, RIDER_STATUS } from '../../constants/status';
import { logout, refreshRiderProfile } from '../../services/auth';
import { dateTime } from '../../utils/format';
import { errorMessage } from '../../utils/request';

Page({
  data: { loading: true, profile: null as Awaited<ReturnType<typeof refreshRiderProfile>> | null, applyTime: '', canReapply: false },
  onShow() { void this.load(); },
  async load() {
    this.setData({ loading: true });
    try {
      const profile = await refreshRiderProfile();
      if (profile.auditStatus === RIDER_AUDIT.approved && profile.riderStatus === RIDER_STATUS.enabled) { wx.reLaunch({ url: ROUTES.dashboard }); return; }
      this.setData({ profile, applyTime: dateTime(profile.applyTime), canReapply: profile.auditStatus === RIDER_AUDIT.rejected });
    } catch (error) { wx.showToast({ title: errorMessage(error), icon: 'none' }); }
    finally { this.setData({ loading: false }); }
  },
  onReapply() { wx.redirectTo({ url: ROUTES.apply }); },
  onLogout() { logout(); }
});
