import { updateUserProfile } from '../../../api/index';
import { getAuthState, refreshProfile } from '../../../services/auth';
import type { UserInfo } from '../../../types/user';
import { requireLogin } from '../../../utils/route-guard';

Page({
  data: {
    userInfo: null as UserInfo | null,
    nickname: '',
    avatar: '',
    gender: 0 as 0 | 1 | 2,
    saving: false,
    errorMessage: ''
  },

  async onShow() {
    const loggedIn = await requireLogin();
    if (!loggedIn) {
      return;
    }
    this.applyUserInfo(getAuthState().userInfo);
  },

  onNicknameChange(event: WechatMiniprogram.CustomEvent) {
    this.setData({ nickname: String(event.detail || '') });
  },

  onAvatarChange(event: WechatMiniprogram.CustomEvent) {
    this.setData({ avatar: String(event.detail || '') });
  },

  async submitProfile() {
    if (this.data.saving) {
      return;
    }

    this.setData({ saving: true, errorMessage: '' });
    try {
      await updateUserProfile({
        nickname: this.data.nickname.trim() || null,
        avatar: this.data.avatar.trim() || null,
        gender: this.data.gender
      });
      await refreshProfile();
      wx.showToast({ title: '资料已更新', icon: 'success' });
      wx.navigateBack();
    } catch (error) {
      const message = error instanceof Error ? error.message : '资料更新失败';
      this.setData({ errorMessage: message });
    } finally {
      this.setData({ saving: false });
    }
  },

  applyUserInfo(userInfo: UserInfo | null) {
    this.setData({
      userInfo,
      nickname: userInfo?.nickname || '',
      avatar: userInfo?.avatar || '',
      gender: userInfo?.gender ?? 0
    });
  }
});
