import { getAddressList } from '../../api/index';
import { ERRAND_SERVICES, findErrandService, type ErrandServiceType } from '../../constants/errand';
import { ROUTES } from '../../constants/routes';
import { STORAGE_KEYS } from '../../constants/storage';
import type { Address } from '../../types/address';
import { buildFullAddress } from '../../utils/address';
import { maskPhone } from '../../utils/phone';
import { requireLogin } from '../../utils/route-guard';

interface AddressOption extends Address {
  summary: string;
  contact: string;
}

type AddressTarget = 'pickup' | 'delivery' | '';

Page({
  data: {
    services: ERRAND_SERVICES,
    serviceType: 'send' as ErrandServiceType,
    serviceLabel: '帮我送',
    itemTypes: ['文件资料', '日用小件', '食品饮品', '鲜花礼品', '其他物品'],
    itemTypeIndex: 0,
    weight: 1,
    remark: '',
    imagePaths: [] as string[],
    addresses: [] as AddressOption[],
    pickupAddress: null as AddressOption | null,
    deliveryAddress: null as AddressOption | null,
    selectingAddress: '' as AddressTarget,
    loadingAddresses: false
  },

  onLoad(options: { type?: string }) {
    this.applyService(options.type);
  },

  async onShow() {
    if (!(await requireLogin())) {
      return;
    }
    await this.loadAddresses();
  },

  applyService(type?: string) {
    const service = findErrandService(type);
    this.setData({ serviceType: service.id, serviceLabel: service.title });
  },

  selectService(event: WechatMiniprogram.BaseEvent) {
    this.applyService(String(event.currentTarget.dataset.id || 'send'));
  },

  selectItemType(event: WechatMiniprogram.PickerChange) {
    this.setData({ itemTypeIndex: Number(event.detail.value) || 0 });
  },

  changeWeight(event: WechatMiniprogram.SliderChange) {
    this.setData({ weight: Number(event.detail.value) || 1 });
  },

  updateRemark(event: WechatMiniprogram.TextareaInput) {
    this.setData({ remark: event.detail.value || '' });
  },

  chooseImages() {
    const remaining = 3 - this.data.imagePaths.length;
    if (remaining <= 0) {
      return;
    }
    wx.chooseMedia({
      count: remaining,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (result) => {
        const paths = result.tempFiles.map((file) => file.tempFilePath);
        this.setData({ imagePaths: [...this.data.imagePaths, ...paths].slice(0, 3) });
      }
    });
  },

  removeImage(event: WechatMiniprogram.BaseEvent) {
    const index = Number(event.currentTarget.dataset.index);
    if (!Number.isInteger(index)) {
      return;
    }
    this.setData({ imagePaths: this.data.imagePaths.filter((_, current) => current !== index) });
  },

  chooseAddress(event: WechatMiniprogram.BaseEvent) {
    const target = String(event.currentTarget.dataset.target) as AddressTarget;
    if (target !== 'pickup' && target !== 'delivery') {
      return;
    }
    this.setData({ selectingAddress: target });
    wx.removeStorageSync(STORAGE_KEYS.checkoutAddressId);
    wx.navigateTo({ url: `${ROUTES.addressList}?select=1` });
  },

  async loadAddresses() {
    if (this.data.loadingAddresses) {
      return;
    }
    this.setData({ loadingAddresses: true });
    try {
      const addresses = (await getAddressList()).map(toAddressOption);
      const selectedId = this.data.selectingAddress
        ? Number(wx.getStorageSync(STORAGE_KEYS.checkoutAddressId))
        : 0;
      const selected = addresses.find((address) => address.id === selectedId) || null;
      const updates: {
        addresses: AddressOption[];
        pickupAddress?: AddressOption;
        deliveryAddress?: AddressOption;
        selectingAddress: AddressTarget;
      } = { addresses, selectingAddress: '' };
      if (selected && this.data.selectingAddress === 'pickup') {
        updates.pickupAddress = selected;
      }
      if (selected && this.data.selectingAddress === 'delivery') {
        updates.deliveryAddress = selected;
      }
      this.setData(updates);
      if (selectedId) {
        wx.removeStorageSync(STORAGE_KEYS.checkoutAddressId);
      }
    } catch (error) {
      wx.showToast({
        title: error instanceof Error ? error.message : '地址加载失败',
        icon: 'none'
      });
    } finally {
      this.setData({ loadingAddresses: false });
    }
  },

  submitOrder() {
    if (!this.data.pickupAddress || !this.data.deliveryAddress) {
      wx.showToast({ title: '请选择取货和送达地址', icon: 'none' });
      return;
    }
    wx.showModal({
      title: '暂不能提交',
      content: '跑腿计价服务正在准备中，请稍后再试。',
      showCancel: false,
      confirmText: '知道了'
    });
  }
});

function toAddressOption(address: Address): AddressOption {
  return {
    ...address,
    summary: buildFullAddress(address) || '地址信息待完善',
    contact: `${address.receiverName} ${maskPhone(address.receiverPhone)}`
  };
}
