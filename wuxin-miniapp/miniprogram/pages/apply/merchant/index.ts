import { applyMerchant } from '../../../api/index';
import type { MerchantApplyRequest, MerchantApplyResponse } from '../../../types/merchant';
import { requireLogin } from '../../../utils/route-guard';

Page({
  data: {
    merchantName: '', contactName: '', contactPhone: '', businessLicense: '', idCardFront: '', idCardBack: '',
    storeName: '', storeLogo: '', storeDescription: '', storePhone: '', province: '', city: '', district: '', detailAddress: '',
    latitude: null as number | null, longitude: null as number | null, openTime: '', closeTime: '',
    submitting: false, errorMessage: '', result: null as MerchantApplyResponse | null
  },
  async onLoad() { await requireLogin(); },
  updateField(event: WechatMiniprogram.Input) {
    const field = String(event.currentTarget.dataset.field);
    this.setData({ [field]: event.detail.value });
  },
  chooseLocation() {
    wx.chooseLocation({ success: (location) => this.setData({ detailAddress: location.address || location.name, latitude: location.latitude, longitude: location.longitude }) });
  },
  async submit() {
    if (this.data.submitting) return;
    const request = this.buildRequest();
    const validation = validateRequest(request);
    if (validation) { this.setData({ errorMessage: validation }); return; }
    this.setData({ submitting: true, errorMessage: '' });
    try {
      const result = await applyMerchant(request);
      this.setData({ result });
      wx.showToast({ title: '申请已提交', icon: 'success' });
    } catch (error) { this.setData({ errorMessage: error instanceof Error ? error.message : '申请提交失败' }); }
    finally { this.setData({ submitting: false }); }
  },
  buildRequest(): MerchantApplyRequest {
    return {
      merchantName: this.data.merchantName.trim(), contactName: this.data.contactName.trim(), contactPhone: this.data.contactPhone.trim(),
      businessLicense: optional(this.data.businessLicense), idCardFront: optional(this.data.idCardFront), idCardBack: optional(this.data.idCardBack),
      storeName: this.data.storeName.trim(), storeLogo: optional(this.data.storeLogo), storeDescription: optional(this.data.storeDescription),
      storePhone: this.data.storePhone.trim(), province: optional(this.data.province), city: optional(this.data.city), district: optional(this.data.district),
      detailAddress: this.data.detailAddress.trim(), latitude: this.data.latitude, longitude: this.data.longitude,
      openTime: optional(this.data.openTime), closeTime: optional(this.data.closeTime)
    };
  },
  backProfile() { wx.switchTab({ url: '/pages/profile/index' }); }
});

function optional(value: string): string | null { return value.trim() || null; }
function validateRequest(value: MerchantApplyRequest): string {
  if (!value.merchantName) return '请填写商家主体名称';
  if (!value.contactName) return '请填写联系人姓名';
  if (!/^1\d{10}$/.test(value.contactPhone)) return '请填写正确的联系人手机号';
  if (!value.storeName) return '请填写店铺名称';
  if (!/^1\d{10}$/.test(value.storePhone)) return '请填写正确的店铺联系电话';
  if (!value.detailAddress) return '请填写店铺详细地址';
  return '';
}
