import { ROUTES } from '../../constants/routes';
import { getCartCount, refreshCartSummary, restoreCartSummary } from '../../services/cart';
import { bindMockPhone, bindPhoneWithCode, getAuthState, logout as clearAndLogout, refreshProfile } from '../../services/auth';
import type { UserInfo } from '../../types/user';
import { DEFAULT_AVATAR, normalizeImageUrl } from '../../utils/image';
import { maskPhone } from '../../utils/phone';
import { requireLogin } from '../../utils/route-guard';
import { isMockWechatPhoneBindEnabled } from '../../utils/wechat-phone-mode';

Page({
  data: {
    userInfo: null as UserInfo | null,
    displayName: '微信用户',
    usernameText: '',
    phoneText: '未绑定',
    phoneButtonText: '立即绑定',
    canBindPhone: false,
    phoneBindOpenType: 'getPhoneNumber',
    bindingPhone: false,
    genderText: '未知',
    avatarUrl: DEFAULT_AVATAR,
    cartCount: 0
  },

  async onShow() {
    this.setData({ cartCount: restoreCartSummary() });
    const loggedIn = await requireLogin();
    if (!loggedIn) {
      return;
    }
    this.applyUserInfo(getAuthState().userInfo);
    void Promise.all([this.refreshUser(), this.refreshCartBadge()]);
  },

  goToLogin() {
    wx.navigateTo({ url: ROUTES.login });
  },

  goToEditProfile() {
    wx.navigateTo({ url: ROUTES.profileEdit });
  },

  goToAddressList() {
    wx.navigateTo({ url: ROUTES.addressList });
  },

  goToCart() {
    wx.navigateTo({ url: ROUTES.cart });
  },

  goToOrders() { wx.switchTab({ url: ROUTES.orderList }); },
  goToPublic() { wx.navigateTo({ url: ROUTES.publicService }); },
  goToAbout() { wx.navigateTo({ url: ROUTES.about }); },
  goToSettings() { wx.navigateTo({ url: ROUTES.settings }); },

  goToMerchantApply() {
    wx.navigateTo({ url: ROUTES.merchantApply });
  },

  goToRiderApply() {
    wx.navigateTo({ url: ROUTES.riderApply });
  },

  async handleBindPhoneClick() {
    if (!this.data.canBindPhone || this.data.bindingPhone || this.data.phoneBindOpenType) {
      return;
    }

    await this.bindPhoneByCode(() => bindMockPhone());
  },

  async handleGetPhoneNumber(event: WechatMiniprogram.CustomEvent) {
    if (!this.data.canBindPhone || this.data.bindingPhone) {
      return;
    }

    const detail = event.detail as { code?: string; errMsg?: string };
    if (!detail?.code) {
      wx.showToast({ title: '已取消手机号授权', icon: 'none' });
      return;
    }

    await this.bindPhoneByCode(() => bindPhoneWithCode(detail.code as string));
  },

  logout() {
    wx.showModal({
      title: '退出登录',
      content: '确认退出当前账号吗？',
      confirmText: '退出',
      success: (result) => {
        if (!result.confirm) {
          return;
        }
        this.applyUserInfo(null);
        clearAndLogout();
      }
    });
  },

  handleAvatarError() {
    this.setData({ avatarUrl: DEFAULT_AVATAR });
  },

  applyUserInfo(userInfo: UserInfo | null) {
    const hasPhone = Boolean(userInfo?.phone);
    const mockPhoneBind = isMockWechatPhoneBindEnabled();
    this.setData({
      userInfo,
      displayName: userInfo?.nickname || '微信用户',
      usernameText: userInfo?.username || '',
      phoneText: maskPhone(userInfo?.phone),
      phoneButtonText: hasPhone ? maskPhone(userInfo?.phone) : '立即绑定',
      canBindPhone: !hasPhone,
      phoneBindOpenType: !hasPhone && !mockPhoneBind ? 'getPhoneNumber' : '',
      genderText: getGenderText(userInfo?.gender),
      avatarUrl: normalizeImageUrl(userInfo?.avatar, DEFAULT_AVATAR)
    });
  },

  async bindPhoneByCode(action: () => Promise<UserInfo>) {
    this.setData({ bindingPhone: true });
    try {
      const userInfo = await action();
      this.applyUserInfo(userInfo);
      wx.showToast({ title: '手机号已绑定', icon: 'success' });
    } catch (error) {
      const message = error instanceof Error ? error.message : '手机号绑定失败';
      wx.showToast({ title: message, icon: 'none' });
    } finally {
      this.setData({ bindingPhone: false });
    }
  },

  async refreshCartBadge() {
    try {
      const cartCount = await refreshCartSummary();
      this.setData({ cartCount });
    } catch {
      this.setData({ cartCount: getCartCount() });
    }
  },

  async refreshUser() {
    try { this.applyUserInfo(await refreshProfile()); } catch { this.applyUserInfo(getAuthState().userInfo); }
  }
});

function getGenderText(gender?: 0 | 1 | 2): string {
  if (gender === 1) {
    return '男';
  }
  if (gender === 2) {
    return '女';
  }
  return '未知';
}
