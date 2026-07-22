import { ROUTES } from '../../constants/routes';
import { MERCHANT_AUDIT, MERCHANT_STATUS } from '../../constants/status';
import { logout, refreshMerchantProfile } from '../../services/auth';
import { dateTime } from '../../utils/format';
import { errorMessage } from '../../utils/request';
Page({
  data: { loading: true, profile: null as Awaited<ReturnType<typeof refreshMerchantProfile>> | null, applyTime: '' },
  onShow() { void this.load(); },
  async load() { this.setData({ loading: true }); try { const profile = await refreshMerchantProfile(); if (profile.auditStatus === MERCHANT_AUDIT.approved && profile.merchantStatus === MERCHANT_STATUS.enabled && profile.storeStatus === MERCHANT_STATUS.enabled) { wx.reLaunch({ url: ROUTES.dashboard }); return; } this.setData({ profile, applyTime: dateTime(profile.createTime) }); } catch (error) { wx.showToast({ title: errorMessage(error), icon: 'none' }); } finally { this.setData({ loading: false }); } },
  onLogout() { logout(); }
});
