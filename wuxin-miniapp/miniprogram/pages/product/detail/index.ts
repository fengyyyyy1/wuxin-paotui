import { addCart, clearCart, getProductDetail } from '../../../api/index';
import { ROUTES } from '../../../constants/routes';
import type { ProductItem } from '../../../types/product';
import { formatMoney } from '../../../utils/format';
import { DEFAULT_PRODUCT_IMAGE, normalizeImageUrl } from '../../../utils/image';
import { requireLogin } from '../../../utils/route-guard';
import { getCartCount, refreshCartSummary, restoreCartSummary } from '../../../services/cart';

interface ProductDetailDisplay extends ProductItem {
  imageUrl: string;
  priceText: string;
  stockText: string;
  statusText: string;
  cartButtonText: string;
  canAddToCart: boolean;
}

Page({
  data: {
    productId: 0,
    storeId: 0,
    storeName: '',
    storeCanSell: true,
    product: null as ProductDetailDisplay | null,
    loading: false,
    errorMessage: '',
    addingCart: false,
    cartCount: 0
  },

  onLoad(options: { id?: string; storeId?: string; storeName?: string; storeCanSell?: string }) {
    const productId = Number(options.id);
    if (!Number.isFinite(productId) || productId <= 0) {
      this.setData({ errorMessage: '商品ID不合法' });
      return;
    }

    this.setData({
      productId,
      storeId: Number(options.storeId) || 0,
      storeName: decodeURIComponent(options.storeName || ''),
      storeCanSell: options.storeCanSell !== '0',
      cartCount: restoreCartSummary()
    });
    void this.loadProductDetail();
    void this.refreshCartBadge();
  },

  onShow() {
    this.setData({ cartCount: getCartCount() });
  },

  async loadProductDetail() {
    if (!this.data.productId || this.data.loading) {
      return;
    }

    this.setData({ loading: true, errorMessage: '' });
    try {
      const product = await getProductDetail(this.data.productId);
      this.setData({ product: toProductDetailDisplay(product, this.data.storeCanSell) });
    } catch (error) {
      const message = error instanceof Error ? error.message : '商品详情加载失败';
      this.setData({ errorMessage: message, product: null });
    } finally {
      this.setData({ loading: false });
    }
  },

  refresh() {
    void this.loadProductDetail();
  },

  handleImageError() {
    if (!this.data.product || this.data.product.imageUrl === DEFAULT_PRODUCT_IMAGE) {
      return;
    }
    this.setData({ product: { ...this.data.product, imageUrl: DEFAULT_PRODUCT_IMAGE } });
  },

  goToCart() {
    wx.navigateTo({ url: ROUTES.cart });
  },

  async handleCartClick() {
    if (this.data.addingCart || !this.data.product) {
      return;
    }

    const loggedIn = await requireLogin();
    if (!loggedIn) {
      return;
    }

    if (!this.data.product.canAddToCart) {
      wx.showToast({ title: this.data.product.cartButtonText, icon: 'none' });
      return;
    }

    await this.addCurrentProduct(false);
  },

  async addCurrentProduct(retryAfterClear: boolean) {
    if (!this.data.product) {
      return;
    }

    this.setData({ addingCart: true });
    try {
      await addCart({ productId: this.data.product.productId, quantity: 1 });
      const cartCount = await refreshCartSummary();
      this.setData({ cartCount });
      wx.showToast({ title: retryAfterClear ? '已清空并加入购物车' : '已加入购物车', icon: 'success' });
    } catch (error) {
      const message = error instanceof Error ? error.message : '加入购物车失败';
      if (isStoreConflictMessage(message) && !retryAfterClear) {
        this.showStoreConflictConfirm();
        return;
      }
      wx.showToast({ title: message, icon: 'none' });
    } finally {
      this.setData({ addingCart: false });
    }
  },

  showStoreConflictConfirm() {
    wx.showModal({
      title: '清空原购物车',
      content: '购物车中已有其他门店商品，是否清空并加入当前商品？',
      confirmText: '清空并加入',
      cancelText: '取消',
      success: (result) => {
        if (!result.confirm) {
          return;
        }
        void this.clearCartThenAdd();
      }
    });
  },

  async clearCartThenAdd() {
    this.setData({ addingCart: true });
    try {
      await clearCart();
      await this.addCurrentProduct(true);
    } catch (error) {
      const message = error instanceof Error ? error.message : '清空购物车失败';
      wx.showToast({ title: message, icon: 'none' });
    } finally {
      this.setData({ addingCart: false });
    }
  },

  async refreshCartBadge() {
    try {
      const cartCount = await refreshCartSummary();
      this.setData({ cartCount });
    } catch {
      this.setData({ cartCount: getCartCount() });
    }
  }
});

function toProductDetailDisplay(product: ProductItem, storeCanSell: boolean): ProductDetailDisplay {
  const stock = Number(product.stock || 0);
  const canAddToCart = storeCanSell && stock > 0;
  return {
    ...product,
    imageUrl: normalizeImageUrl(product.productImage, DEFAULT_PRODUCT_IMAGE),
    priceText: formatMoney(product.price),
    stockText: stock > 0 ? `库存 ${stock}` : '已售罄',
    statusText: canAddToCart ? '可售' : storeCanSell ? '暂不可售' : '门店休息中',
    cartButtonText: canAddToCart ? '加入购物车' : storeCanSell ? '暂不可售' : '门店休息中',
    canAddToCart
  };
}

function isStoreConflictMessage(message: string): boolean {
  return message.includes('购物车') && message.includes('其他');
}
