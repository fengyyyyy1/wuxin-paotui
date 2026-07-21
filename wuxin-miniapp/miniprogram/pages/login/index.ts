import { ROUTES } from '../../constants/routes';
import { login, verifySession } from '../../services/auth';

Page({
  data: {
    loading: false,
    errorMessage: '',
    checkingSession: false
  },

  async onShow() {
    if (this.data.checkingSession) {
      return;
    }

    this.setData({ checkingSession: true });
    try {
      const loggedIn = await verifySession();
      if (loggedIn) {
        wx.switchTab({ url: ROUTES.home });
      }
    } finally {
      this.setData({ checkingSession: false });
    }
  },

  async handleWechatLogin() {
    if (this.data.loading) {
      return;
    }

    this.setData({ loading: true, errorMessage: '' });
    try {
      await login();
      wx.switchTab({ url: ROUTES.home });
    } catch (error) {
      const message = error instanceof Error ? error.message : '登录失败，请稍后重试';
      this.setData({ errorMessage: message });
    } finally {
      this.setData({ loading: false });
    }
  }
});
