import { ROUTES } from '../../constants/routes';
import { logout, requireApprovedMerchant, restoreSession } from '../../services/auth';
import { displayName } from '../../utils/format';
Page({
  data: { name: '五鑫商家', contact: '', auditText: '', businessText: '' },
  async onShow() { if (!await requireApprovedMerchant()) return; const p = restoreSession().merchantProfile; if (p) this.setData({ name: displayName(p.storeName), contact: `${p.contactName} · ${p.contactPhone}`, auditText: p.auditStatusText, businessText: p.businessStatusText }); },
  toOrders() { wx.switchTab({ url: ROUTES.orders }); }, toStore() { wx.navigateTo({ url: ROUTES.store }); }, toProducts() { wx.navigateTo({ url: ROUTES.products }); }, toCategories() { wx.navigateTo({ url: ROUTES.categories }); }, toAnalytics() { wx.navigateTo({ url: ROUTES.analytics }); },
  about() { wx.showModal({ title: '关于五鑫商家', content: '五鑫跑腿商家经营端，为潼南本地商家提供订单和商品管理能力。', showCancel: false }); },
  onLogout() { wx.showModal({ title: '退出登录', content: '确定退出当前商家账号？', confirmColor: '#e5484d', success: result => { if (result.confirm) logout(); } }); }
});
