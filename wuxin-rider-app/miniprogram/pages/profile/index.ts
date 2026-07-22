import { ROUTES } from '../../constants/routes';
import { logout, requireApprovedRider, restoreSession } from '../../services/auth';
import { displayName, phone } from '../../utils/format';

Page({
  data: { name: '五鑫骑手', phone: '未绑定', auditText: '', riderText: '' },
  async onShow() { if (!await requireApprovedRider()) return; const profile = restoreSession().riderProfile; if (profile) this.setData({ name: displayName(profile.realName), phone: phone(profile.phone), auditText: profile.auditStatusText, riderText: profile.riderStatusText }); },
  toOrders() { wx.navigateTo({ url: ROUTES.orders }); },
  toStatistics() { wx.navigateTo({ url: ROUTES.statistics }); },
  toRanking() { wx.navigateTo({ url: ROUTES.ranking }); },
  about() { wx.showModal({ title: '关于五鑫骑手', content: '五鑫跑腿骑手工作端，为潼南本地配送服务提供接单与履约工具。', showCancel: false }); },
  onLogout() { wx.showModal({ title: '退出登录', content: '确定退出当前骑手账号？', confirmColor: '#e5484d', success: result => { if (result.confirm) logout(); } }); }
});
