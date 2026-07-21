import { logout } from '../../services/auth';
import { STORAGE_KEYS } from '../../constants/storage';

Page({
  clearCache() {
    wx.showModal({ title: '清理浏览记录', content: '将清理搜索历史和最近浏览，不影响登录、地址和订单。', confirmText: '清理', success: (result) => {
      if (!result.confirm) return;
      wx.removeStorageSync(STORAGE_KEYS.searchHistory);
      wx.removeStorageSync(STORAGE_KEYS.recentStores);
      wx.showToast({ title: '已清理', icon: 'success' });
    } });
  },
  signOut() {
    wx.showModal({ title: '退出登录', content: '确认退出当前账号？', confirmText: '退出', confirmColor: '#FF4D4F', success: (result) => { if (result.confirm) logout(); } });
  }
});
