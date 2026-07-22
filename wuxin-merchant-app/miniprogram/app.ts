import { restoreSession } from './services/auth';
import { getPlatformHome } from './api/platform';

App<IAppOption>({
  globalData: { token: null, userInfo: null, merchantProfile: null, platformHome: null },
  onLaunch() {
    restoreSession();
    void getPlatformHome().then((value) => {
      const app = getApp<IAppOption>();
      if (app?.globalData) app.globalData.platformHome = value;
    }).catch(() => undefined);
    wx.onNetworkStatusChange(({ isConnected }) => { if (!isConnected) wx.showToast({ title: '网络已断开', icon: 'none' }); });
  }
});
