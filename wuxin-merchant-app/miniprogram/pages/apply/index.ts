import { applyMerchant } from '../../api/merchant';
import { ROUTES } from '../../constants/routes';
import { errorMessage } from '../../utils/request';

Page({
  data: { merchantName: '', contactName: '', contactPhone: '', businessLicense: '', idCardFront: '', idCardBack: '', storeName: '', storeLogo: '', storeDescription: '', storePhone: '', province: '重庆市', city: '重庆市', district: '潼南区', detailAddress: '', latitude: '', longitude: '', openTime: '08:00', closeTime: '22:00', submitting: false },
  onInput(event: WechatMiniprogram.Input) { const field = String(event.currentTarget.dataset.field); this.setData({ [field]: event.detail.value.trim() }); },
  onOpenTime(event: WechatMiniprogram.PickerChange) { this.setData({ openTime: String(event.detail.value) }); },
  onCloseTime(event: WechatMiniprogram.PickerChange) { this.setData({ closeTime: String(event.detail.value) }); },
  async onSubmit() {
    if (this.data.submitting) return;
    const required = ['merchantName', 'contactName', 'contactPhone', 'storeName', 'storePhone', 'detailAddress'] as const;
    if (required.some(field => !this.data[field])) { wx.showToast({ title: '请完整填写必填资料', icon: 'none' }); return; }
    this.setData({ submitting: true });
    try {
      await applyMerchant({ merchantName: this.data.merchantName, contactName: this.data.contactName, contactPhone: this.data.contactPhone, businessLicense: this.data.businessLicense || undefined, idCardFront: this.data.idCardFront || undefined, idCardBack: this.data.idCardBack || undefined, storeName: this.data.storeName, storeLogo: this.data.storeLogo || undefined, storeDescription: this.data.storeDescription || undefined, storePhone: this.data.storePhone, province: this.data.province || undefined, city: this.data.city || undefined, district: this.data.district || undefined, detailAddress: this.data.detailAddress, latitude: this.data.latitude ? Number(this.data.latitude) : undefined, longitude: this.data.longitude ? Number(this.data.longitude) : undefined, openTime: `${this.data.openTime}:00`, closeTime: `${this.data.closeTime}:00` });
      wx.showToast({ title: '申请已提交', icon: 'success' }); setTimeout(() => wx.reLaunch({ url: ROUTES.review }), 700);
    } catch (error) { wx.showToast({ title: errorMessage(error), icon: 'none' }); }
    finally { this.setData({ submitting: false }); }
  }
});
