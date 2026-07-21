import { deleteAddress, getAddressList, setDefaultAddress } from '../../../api/index';
import { ROUTES } from '../../../constants/routes';
import { STORAGE_KEYS } from '../../../constants/storage';
import type { Address } from '../../../types/address';
import { buildFullAddress } from '../../../utils/address';
import { maskPhone } from '../../../utils/phone';
import { requireLogin } from '../../../utils/route-guard';

interface AddressCard extends Address {
  maskedPhone: string;
  fullAddress: string;
}

Page({
  data: {
    addresses: [] as AddressCard[],
    loading: false,
    errorMessage: '',
    deletingId: null as number | null,
    selectMode: false
  },

  onLoad(options: { select?: string }) {
    this.setData({ selectMode: options.select === '1' });
  },

  async onShow() {
    const loggedIn = await requireLogin();
    if (!loggedIn) {
      return;
    }
    await this.loadAddresses();
  },

  async onPullDownRefresh() {
    await this.loadAddresses();
    wx.stopPullDownRefresh();
  },

  async loadAddresses() {
    if (this.data.loading) {
      return;
    }

    this.setData({ loading: true, errorMessage: '' });
    try {
      const addresses = await getAddressList();
      this.setData({ addresses: normalizeAddresses(addresses) });
    } catch (error) {
      const message = error instanceof Error ? error.message : '地址加载失败';
      this.setData({ errorMessage: message });
    } finally {
      this.setData({ loading: false });
    }
  },

  goToCreateAddress() {
    wx.removeStorageSync(STORAGE_KEYS.editingAddress);
    wx.navigateTo({ url: `${ROUTES.addressEdit}?mode=create` });
  },

  editAddress(event: WechatMiniprogram.BaseEvent) {
    const address = this.findAddressFromEvent(event);
    if (!address) {
      wx.showToast({ title: '地址数据不存在', icon: 'none' });
      return;
    }

    wx.setStorageSync(STORAGE_KEYS.editingAddress, address);
    wx.navigateTo({ url: `${ROUTES.addressEdit}?mode=edit&id=${address.id}` });
  },

  selectAddress(event: WechatMiniprogram.BaseEvent) {
    if (!this.data.selectMode) return;
    const address = this.findAddressFromEvent(event);
    if (!address) return;
    wx.setStorageSync(STORAGE_KEYS.checkoutAddressId, address.id);
    wx.navigateBack();
  },

  async setDefaultAddress(event: WechatMiniprogram.BaseEvent) {
    const address = this.findAddressFromEvent(event);
    if (!address || address.isDefault === 1 || this.data.loading) {
      return;
    }

    this.setData({ loading: true, errorMessage: '' });
    try {
      await setDefaultAddress(address.id);
      wx.showToast({ title: '已设为默认', icon: 'success' });
      const addresses = await getAddressList();
      this.setData({ addresses: normalizeAddresses(addresses) });
    } catch (error) {
      const message = error instanceof Error ? error.message : '设置默认地址失败';
      wx.showToast({ title: message, icon: 'none' });
    } finally {
      this.setData({ loading: false });
    }
  },

  confirmDeleteAddress(event: WechatMiniprogram.BaseEvent) {
    const address = this.findAddressFromEvent(event);
    if (!address || this.data.deletingId) {
      return;
    }

    wx.showModal({
      title: '删除地址',
      content: '确认删除该地址？',
      confirmText: '删除',
      confirmColor: '#dc2626',
      success: async (result) => {
        if (!result.confirm) {
          return;
        }
        await this.removeAddress(address.id);
      }
    });
  },

  async removeAddress(id: number) {
    this.setData({ deletingId: id, errorMessage: '' });
    try {
      await deleteAddress(id);
      wx.showToast({ title: '地址已删除', icon: 'success' });
      await this.loadAddresses();
    } catch (error) {
      const message = error instanceof Error ? error.message : '地址删除失败';
      wx.showToast({ title: message, icon: 'none' });
    } finally {
      this.setData({ deletingId: null });
    }
  },

  findAddressFromEvent(event: WechatMiniprogram.BaseEvent): AddressCard | null {
    const id = Number(event.currentTarget.dataset.id);
    if (!Number.isFinite(id)) {
      return null;
    }
    return this.data.addresses.find((address) => address.id === id) || null;
  }
});

function normalizeAddresses(addresses: Address[]): AddressCard[] {
  return [...addresses]
    .sort((left, right) => {
      if (left.isDefault !== right.isDefault) {
        return right.isDefault - left.isDefault;
      }
      return (right.id || 0) - (left.id || 0);
    })
    .map((address) => ({
      ...address,
      maskedPhone: maskPhone(address.receiverPhone),
      fullAddress: buildFullAddress(address)
    }));
}
