import { restoreSession } from './services/auth';

App<IAppOption>({
  globalData: { token: null, userInfo: null, merchantProfile: null },
  onLaunch() {
    restoreSession();
    wx.onNetworkStatusChange(({ isConnected }) => { if (!isConnected) wx.showToast({ title: '网络已断开', icon: 'none' }); });
  }
});
