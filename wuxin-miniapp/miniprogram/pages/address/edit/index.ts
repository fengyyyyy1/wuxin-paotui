import { createAddress, getAddressList, updateAddress } from '../../../api/index';
import { STORAGE_KEYS } from '../../../constants/storage';
import type { Address, AddressRequest } from '../../../types/address';
import { requireLogin } from '../../../utils/route-guard';

type AddressFormMode = 'create' | 'edit';

Page({
  data: {
    mode: 'create' as AddressFormMode,
    addressId: null as number | null,
    receiverName: '',
    receiverPhone: '',
    province: '',
    city: '',
    district: '',
    detailAddress: '',
    isDefault: false,
    saving: false,
    errorMessage: ''
  },

  async onLoad(options: Record<string, string | undefined>) {
    const loggedIn = await requireLogin();
    if (!loggedIn) {
      return;
    }

    const mode: AddressFormMode = options.mode === 'edit' ? 'edit' : 'create';
    const addressId = parseAddressId(options.id);
    this.setData({ mode, addressId });

    if (mode === 'edit') {
      if (!addressId) {
        this.setData({ errorMessage: '地址ID不合法，请返回列表重试' });
        return;
      }
      await this.applyEditingAddress(addressId);
    }
  },

  onReceiverNameChange(event: WechatMiniprogram.CustomEvent) {
    this.setData({ receiverName: String(event.detail || '') });
  },

  onReceiverPhoneChange(event: WechatMiniprogram.CustomEvent) {
    this.setData({ receiverPhone: String(event.detail || '') });
  },

  onProvinceChange(event: WechatMiniprogram.CustomEvent) {
    this.setData({ province: String(event.detail || '') });
  },

  onCityChange(event: WechatMiniprogram.CustomEvent) {
    this.setData({ city: String(event.detail || '') });
  },

  onDistrictChange(event: WechatMiniprogram.CustomEvent) {
    this.setData({ district: String(event.detail || '') });
  },

  onDetailAddressChange(event: WechatMiniprogram.CustomEvent) {
    this.setData({ detailAddress: String(event.detail || '') });
  },

  onDefaultChange(event: WechatMiniprogram.CustomEvent) {
    this.setData({ isDefault: Boolean(event.detail) });
  },

  async submitAddress() {
    if (this.data.saving) {
      return;
    }

    const payload = this.buildPayload();
    const errorMessage = validateAddress(payload);
    if (errorMessage) {
      this.setData({ errorMessage });
      return;
    }

    this.setData({ saving: true, errorMessage: '' });
    try {
      if (this.data.mode === 'edit') {
        if (!this.data.addressId) {
          throw new Error('地址ID不合法');
        }
        await updateAddress(this.data.addressId, payload);
      } else {
        await createAddress(payload);
      }
      wx.showToast({ title: '地址已保存', icon: 'success' });
      wx.navigateBack();
    } catch (error) {
      const message = error instanceof Error ? error.message : '地址保存失败';
      this.setData({ errorMessage: message });
    } finally {
      this.setData({ saving: false });
    }
  },

  async applyEditingAddress(addressId: number) {
    const address = await this.resolveEditingAddress(addressId);
    if (!address) {
      this.setData({ errorMessage: '地址数据不存在，请返回列表重试' });
      return;
    }

    this.setData({
      receiverName: address.receiverName || '',
      receiverPhone: address.receiverPhone || '',
      province: address.province || '',
      city: address.city || '',
      district: address.district || '',
      detailAddress: address.detailAddress || '',
      isDefault: address.isDefault === 1
    });
  },

  async resolveEditingAddress(addressId: number): Promise<Address | null> {
    const cachedAddress = wx.getStorageSync(STORAGE_KEYS.editingAddress) as Address | '';
    if (cachedAddress && typeof cachedAddress === 'object' && cachedAddress.id === addressId) {
      return cachedAddress;
    }

    try {
      const addresses = await getAddressList();
      return addresses.find((address) => address.id === addressId) || null;
    } catch {
      return null;
    }
  },

  buildPayload(): AddressRequest {
    return {
      receiverName: this.data.receiverName.trim(),
      receiverPhone: this.data.receiverPhone.trim(),
      province: normalizeOptionalText(this.data.province),
      city: normalizeOptionalText(this.data.city),
      district: normalizeOptionalText(this.data.district),
      detailAddress: this.data.detailAddress.trim(),
      latitude: null,
      longitude: null,
      isDefault: this.data.isDefault ? 1 : 0
    };
  }
});

function normalizeOptionalText(value: string): string | null {
  return value.trim() || null;
}

function parseAddressId(value?: string): number | null {
  const addressId = Number(value);
  return Number.isFinite(addressId) && addressId > 0 ? addressId : null;
}

function validateAddress(address: AddressRequest): string {
  if (!address.receiverName) {
    return '收件人不能为空';
  }
  if (!/^1\d{10}$/.test(address.receiverPhone)) {
    return '请输入11位手机号';
  }
  if (!address.detailAddress) {
    return '详细地址不能为空';
  }
  return '';
}
