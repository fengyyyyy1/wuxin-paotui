import { createOrderFromCart, getAddressList, previewSettlement } from '../../api/index';
import { ROUTES } from '../../constants/routes';
import { STORAGE_KEYS } from '../../constants/storage';
import { refreshCartSummary } from '../../services/cart';
import type { Address } from '../../types/address';
import type { SettlementPreview, SettlementItem } from '../../types/order';
import { buildFullAddress, findDefaultAddress } from '../../utils/address';
import { formatMoney } from '../../utils/format';
import { DEFAULT_PRODUCT_IMAGE, normalizeImageUrl } from '../../utils/image';
import { maskPhone } from '../../utils/phone';
import { requireLogin } from '../../utils/route-guard';

interface CheckoutAddress extends Address { fullAddress: string; maskedPhone: string; }
interface CheckoutItem extends SettlementItem { imageUrl: string; priceText: string; subtotalText: string; }

Page({
  data: {
    address: null as CheckoutAddress | null,
    preview: null as SettlementPreview | null,
    items: [] as CheckoutItem[],
    productAmountText: '0.00',
    deliveryFeeText: '0.00',
    totalAmountText: '0.00',
    remark: '',
    loading: true,
    submitting: false,
    errorMessage: ''
  },

  async onShow() {
    if (!await requireLogin()) return;
    await this.loadCheckout();
  },

  async loadCheckout() {
    if (this.data.submitting) return;
    this.setData({ loading: true, errorMessage: '' });
    try {
      const addresses = await getAddressList();
      const selectedId = Number(wx.getStorageSync(STORAGE_KEYS.checkoutAddressId) || 0);
      const selected = addresses.find((item) => item.id === selectedId) || findDefaultAddress(addresses) || addresses[0] || null;
      if (!selected) {
        this.setData({ address: null, preview: null, items: [] });
        return;
      }
      const address = toCheckoutAddress(selected);
      wx.setStorageSync(STORAGE_KEYS.checkoutAddressId, selected.id);
      const preview = await previewSettlement({ deliveryAddressId: selected.id });
      this.setData({
        address,
        preview,
        items: preview.items.map(toCheckoutItem),
        productAmountText: formatMoney(preview.productAmount),
        deliveryFeeText: formatMoney(preview.deliveryFee),
        totalAmountText: formatMoney(preview.totalAmount)
      });
    } catch (error) {
      this.setData({ errorMessage: error instanceof Error ? error.message : '结算信息加载失败' });
    } finally {
      this.setData({ loading: false });
    }
  },

  chooseAddress() { wx.navigateTo({ url: `${ROUTES.addressList}?select=1` }); },
  onRemarkInput(event: WechatMiniprogram.Input) { this.setData({ remark: event.detail.value }); },

  async submitOrder() {
    if (this.data.submitting || !this.data.address || !this.data.preview) return;
    this.setData({ submitting: true, errorMessage: '' });
    try {
      const order = await createOrderFromCart({
        deliveryAddressId: this.data.address.id,
        remark: this.data.remark.trim() || undefined
      });
      await refreshCartSummary();
      wx.redirectTo({ url: `${ROUTES.paymentProcessing}?orderId=${order.orderId}` });
    } catch (error) {
      const message = error instanceof Error ? error.message : '订单提交失败';
      this.setData({ errorMessage: message });
      wx.showToast({ title: message, icon: 'none' });
      await this.loadCheckout();
    } finally {
      this.setData({ submitting: false });
    }
  },

  handleImageError(event: WechatMiniprogram.BaseEvent) {
    const id = Number(event.currentTarget.dataset.id);
    this.setData({ items: this.data.items.map((item) => item.productId === id ? { ...item, imageUrl: DEFAULT_PRODUCT_IMAGE } : item) });
  },

  retry() { void this.loadCheckout(); }
});

function toCheckoutAddress(address: Address): CheckoutAddress {
  return { ...address, fullAddress: buildFullAddress(address), maskedPhone: maskPhone(address.receiverPhone) };
}

function toCheckoutItem(item: SettlementItem): CheckoutItem {
  return { ...item, imageUrl: normalizeImageUrl(item.productImage, DEFAULT_PRODUCT_IMAGE), priceText: formatMoney(item.price), subtotalText: formatMoney(item.subtotal) };
}
