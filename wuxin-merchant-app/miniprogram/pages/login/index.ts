import { loginWithPassword, loginWithWechat } from '../../services/auth';
import { errorMessage } from '../../utils/request';
Page({
  data: { username: '', password: '', submitting: false },
  onUsername(event: WechatMiniprogram.Input) { this.setData({ username: event.detail.value.trim() }); },
  onPassword(event: WechatMiniprogram.Input) { this.setData({ password: event.detail.value }); },
  async onPasswordLogin() { if (!this.data.username || !this.data.password || this.data.submitting) { wx.showToast({ title: '请输入账号和密码', icon: 'none' }); return; } this.setData({ submitting: true }); try { await loginWithPassword({ username: this.data.username, password: this.data.password }); } catch (error) { wx.showToast({ title: errorMessage(error), icon: 'none' }); } finally { this.setData({ submitting: false }); } },
  async onWechatLogin() { if (this.data.submitting) return; this.setData({ submitting: true }); try { await loginWithWechat(); } catch (error) { wx.showToast({ title: errorMessage(error), icon: 'none' }); } finally { this.setData({ submitting: false }); } }
});
