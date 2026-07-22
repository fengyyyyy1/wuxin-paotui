import { createErrandOrder, getAddressList, getPlatformHome } from '../../api/index';
import { ERRAND_SERVICES, findErrandService, type ErrandServiceType } from '../../constants/errand';
import { ROUTES } from '../../constants/routes';
import { STORAGE_KEYS } from '../../constants/storage';
import type { Address } from '../../types/address';
import type { PlatformConfig } from '../../types/platform';
import { buildFullAddress } from '../../utils/address';
import { formatMoney } from '../../utils/format';
import { maskPhone } from '../../utils/phone';
import { requireLogin } from '../../utils/route-guard';

interface AddressOption extends Address {
  summary: string;
  contact: string;
}

type AddressTarget = 'pickup' | 'delivery' | '';

interface ErrandPricing {
  baseDeliveryFee: number;
  perKmFee: number;
  perKgFee: number;
  nightSurcharge: number;
  minimumOrderAmount: number;
}

const DEFAULT_DISTANCE_KM = 1;
const DEFAULT_PRICING: ErrandPricing = {
  baseDeliveryFee: 5,
  perKmFee: 1.5,
  perKgFee: 1,
  nightSurcharge: 0,
  minimumOrderAmount: 0
};

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
    loadingAddresses: false,
    pricing: DEFAULT_PRICING,
    estimatedDistance: DEFAULT_DISTANCE_KM,
    estimatedFeeText: formatMoney(DEFAULT_PRICING.baseDeliveryFee),
    submitting: false
  },

  onLoad(options: { type?: string }) {
    this.applyService(options.type);
  },

  async onShow() {
    if (!(await requireLogin())) {
      return;
    }
    await Promise.all([this.loadAddresses(), this.loadPlatformPricing()]);
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
    this.updateEstimatedFee();
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

  async loadPlatformPricing() {
    try {
      const platform = await getPlatformHome();
      const app = getApp<IAppOption>();
      if (app?.globalData) app.globalData.platformHome = platform;
      this.setData({ pricing: toErrandPricing(platform.configs) });
      this.updateEstimatedFee();
    } catch {
      this.updateEstimatedFee();
    }
  },

  updateEstimatedFee() {
    const pricing = this.data.pricing;
    const distance = Math.max(DEFAULT_DISTANCE_KM, Number(this.data.estimatedDistance) || DEFAULT_DISTANCE_KM);
    const weight = Math.max(1, Number(this.data.weight) || 1);
    const hour = new Date().getHours();
    const nightFee = hour >= 22 || hour < 6 ? pricing.nightSurcharge : 0;
    const amount =
      pricing.baseDeliveryFee +
      pricing.perKmFee * distance +
      pricing.perKgFee * Math.max(0, weight - 1) +
      nightFee;
    const finalAmount = Math.max(amount, pricing.minimumOrderAmount, 0.01);
    this.setData({ estimatedFeeText: formatMoney(finalAmount) });
  },

  async submitOrder() {
    if (this.data.submitting) {
      return;
    }
    if (!this.data.pickupAddress || !this.data.deliveryAddress) {
      wx.showToast({ title: '请选择取货和送达地址', icon: 'none' });
      return;
    }
    this.setData({ submitting: true });
    try {
      const itemType = this.data.itemTypes[this.data.itemTypeIndex] || '其他物品';
      const orderId = await createErrandOrder({
        pickupAddressId: this.data.pickupAddress.id,
        deliveryAddressId: this.data.deliveryAddress.id,
        goodsName: `${this.data.serviceLabel}-${itemType}`,
        goodsDescription: this.data.remark.trim() || undefined,
        weight: Number(this.data.weight),
        distance: Number(this.data.estimatedDistance),
        price: Number(this.data.estimatedFeeText),
        remark: this.data.remark.trim() || undefined
      });
      wx.redirectTo({ url: `${ROUTES.paymentProcessing}?orderId=${orderId}` });
    } catch (error) {
      wx.showToast({
        title: error instanceof Error ? error.message : '跑腿订单提交失败',
        icon: 'none'
      });
    } finally {
      this.setData({ submitting: false });
    }
  }
});

function toAddressOption(address: Address): AddressOption {
  return {
    ...address,
    summary: buildFullAddress(address) || '地址信息待完善',
    contact: `${address.receiverName} ${maskPhone(address.receiverPhone)}`
  };
}

function toErrandPricing(configs: PlatformConfig[]): ErrandPricing {
  return {
    baseDeliveryFee: readDecimal(configs, 'errand.base_delivery_fee', DEFAULT_PRICING.baseDeliveryFee),
    perKmFee: readDecimal(configs, 'errand.per_km_fee', DEFAULT_PRICING.perKmFee),
    perKgFee: readDecimal(configs, 'errand.per_kg_fee', DEFAULT_PRICING.perKgFee),
    nightSurcharge: readDecimal(configs, 'errand.night_surcharge', DEFAULT_PRICING.nightSurcharge),
    minimumOrderAmount: readDecimal(configs, 'errand.minimum_order_amount', DEFAULT_PRICING.minimumOrderAmount)
  };
}

function readDecimal(configs: PlatformConfig[], key: string, fallback: number): number {
  const value = Number(configs.find((item) => item.configKey === key)?.configValue);
  return Number.isFinite(value) ? value : fallback;
}
