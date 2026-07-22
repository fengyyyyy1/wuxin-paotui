import { bindPhone } from '../../api/auth';
import { applyRider } from '../../api/rider';
import { MOCK_WECHAT_LOGIN_STORAGE_KEY } from '../../config/env';
import { ROUTES } from '../../constants/routes';
import { restoreSession } from '../../services/auth';
import { errorMessage } from '../../utils/request';
import { getUserInfo, saveAuth, getToken } from '../../utils/auth';

Page({
  data: { phone: '', realName: '', idCard: '', idCardFront: '', idCardBack: '', submitting: false, binding: false },
  onLoad() { this.setData({ phone: getUserInfo()?.phone || '' }); },
  onInput(event: WechatMiniprogram.Input) { const field = String(event.currentTarget.dataset.field); this.setData({ [field]: event.detail.value.trim() }); },
  async onBindPhone(event: any) {
    if (event.detail.errMsg?.includes('deny')) { wx.showToast({ title: '已取消手机号授权', icon: 'none' }); return; }
    const useMock = wx.getStorageSync<boolean>(MOCK_WECHAT_LOGIN_STORAGE_KEY) === true;
    const code = useMock ? 'mock-phone-code-13800000003' : event.detail.code;
    if (!code) { wx.showToast({ title: '未获取到手机号授权凭证', icon: 'none' }); return; }
    this.setData({ binding: true });
    try {
      const user = await bindPhone(code); const token = getToken(); if (token) saveAuth(token, user); restoreSession(); this.setData({ phone: user.phone || '' }); wx.showToast({ title: '手机号绑定成功', icon: 'success' });
    } catch (error) { wx.showToast({ title: errorMessage(error), icon: 'none' }); }
    finally { this.setData({ binding: false }); }
  },
  async onSubmit() {
    if (this.data.submitting) return;
    if (!this.data.phone) { wx.showToast({ title: '请先绑定手机号', icon: 'none' }); return; }
    if (!this.data.realName || !this.data.idCard || !this.data.idCardFront || !this.data.idCardBack) { wx.showToast({ title: '请完整填写实名资料', icon: 'none' }); return; }
    this.setData({ submitting: true });
    try {
      await applyRider({ realName: this.data.realName, idCard: this.data.idCard, idCardFront: this.data.idCardFront, idCardBack: this.data.idCardBack });
      wx.showToast({ title: '申请已提交', icon: 'success' }); setTimeout(() => wx.reLaunch({ url: ROUTES.review }), 700);
    } catch (error) { wx.showToast({ title: errorMessage(error), icon: 'none' }); }
    finally { this.setData({ submitting: false }); }
  }
});
