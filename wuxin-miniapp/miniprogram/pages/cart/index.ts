import {
  clearCart,
  clearInvalidCart,
  deleteCartItem,
  updateCartAllSelected,
  updateCartQuantity,
  updateCartSelected
} from '../../api/index';
import { ROUTES } from '../../constants/routes';
import { applyCartListSummary, refreshCartDetail } from '../../services/cart';
import type { CartItem, CartList, CartSelectedStatus } from '../../types/cart';
import { formatMoney } from '../../utils/format';
import { DEFAULT_PRODUCT_IMAGE, normalizeImageUrl } from '../../utils/image';
import { requireLogin } from '../../utils/route-guard';

interface CartItemDisplay extends CartItem {
  imageUrl: string;
  priceText: string;
  subtotalText: string;
  stockText: string;
  selectedChecked: boolean;
}

let requestSeq = 0;

Page({
  data: {
    storeName: '',
    items: [] as CartItemDisplay[],
    selectedTotalAmountText: '0.00',
    selectedProductCount: 0,
    allSelected: false,
    hasItems: false,
    hasValidItems: false,
    hasInvalidItems: false,
    loading: false,
    errorMessage: '',
    operationKey: ''
  },

  async onShow() {
    const loggedIn = await requireLogin();
    if (!loggedIn) {
      return;
    }
    void this.loadCart();
  },

  async loadCart() {
    const seq = ++requestSeq;
    this.setData({ loading: true, errorMessage: '' });
    try {
      const cartList = await refreshCartDetail();
      if (seq !== requestSeq) {
        return;
      }
      this.applyCartList(cartList);
    } catch (error) {
      const message = error instanceof Error ? error.message : '购物车加载失败';
      this.setData({ errorMessage: message });
    } finally {
      if (seq === requestSeq) {
        this.setData({ loading: false });
      }
    }
  },

  refresh() {
    void this.loadCart();
  },

  goShopping() {
    wx.switchTab({ url: ROUTES.home });
  },

  async toggleSelected(event: WechatMiniprogram.BaseEvent) {
    const cartId = Number(event.currentTarget.dataset.id);
    const item = this.findItem(cartId);
    if (!item || item.invalidReason || this.data.operationKey) {
      return;
    }
    const selected: CartSelectedStatus = item.selected === 1 ? 0 : 1;
    await this.runOperation(`selected-${cartId}`, () =>
      updateCartSelected({ cartId, selected }).then(() => this.loadCart())
    );
  },

  async toggleAllSelected() {
    if (!this.data.hasValidItems || this.data.operationKey) {
      return;
    }
    const selected: CartSelectedStatus = this.data.allSelected ? 0 : 1;
    await this.runOperation('selected-all', async () => {
      const cartList = await updateCartAllSelected({ selected });
      this.applyCartList(cartList);
    });
  },

  async changeQuantity(event: WechatMiniprogram.BaseEvent) {
    const cartId = Number(event.currentTarget.dataset.id);
    const delta = Number(event.currentTarget.dataset.delta);
    const item = this.findItem(cartId);
    if (!item || item.invalidReason || this.data.operationKey) {
      return;
    }

    const nextQuantity = item.quantity + delta;
    if (nextQuantity < 1) {
      this.confirmRemoveItem(cartId);
      return;
    }
    if (typeof item.stock === 'number' && nextQuantity > item.stock) {
      wx.showToast({ title: '不能超过当前库存', icon: 'none' });
      return;
    }

    await this.runOperation(`quantity-${cartId}`, () =>
      updateCartQuantity({ cartId, quantity: nextQuantity }).then(() => this.loadCart())
    );
  },

  confirmRemoveItem(cartId: number) {
    if (!Number.isFinite(cartId) || this.data.operationKey) {
      return;
    }
    wx.showModal({
      title: '移除商品',
      content: '是否移除商品？',
      confirmText: '移除',
      confirmColor: '#ff4d4f',
      success: (result) => {
        if (!result.confirm) {
          return;
        }
        void this.runOperation(`delete-${cartId}`, () =>
          deleteCartItem(cartId).then(() => this.loadCart())
        );
      }
    });
  },

  clearInvalid() {
    if (!this.data.hasInvalidItems || this.data.operationKey) {
      return;
    }
    wx.showModal({
      title: '清理失效商品',
      content: '确认清理购物车中的失效商品？',
      confirmText: '清理',
      success: (result) => {
        if (!result.confirm) {
          return;
        }
        void this.runOperation('clear-invalid', () =>
          clearInvalidCart().then(() => this.loadCart())
        );
      }
    });
  },

  clearAll() {
    if (!this.data.hasItems || this.data.operationKey) {
      return;
    }
    wx.showModal({
      title: '清空购物车',
      content: '确认清空当前购物车？',
      confirmText: '清空',
      success: (result) => {
        if (!result.confirm) {
          return;
        }
        void this.runOperation('clear-all', () => clearCart().then(() => this.loadCart()));
      }
    });
  },

  checkout() {
    if (!this.data.selectedProductCount) {
      wx.showToast({ title: '请先选择商品', icon: 'none' });
      return;
    }
    wx.navigateTo({ url: ROUTES.checkout });
  },

  handleImageError(event: WechatMiniprogram.BaseEvent) {
    const cartId = Number(event.currentTarget.dataset.id);
    if (!Number.isFinite(cartId)) {
      return;
    }
    const item = this.findItem(cartId);
    if (!item || item.imageUrl === DEFAULT_PRODUCT_IMAGE) {
      return;
    }
    this.setData({
      items: this.data.items.map((cartItem) =>
        cartItem.cartId === cartId ? { ...cartItem, imageUrl: DEFAULT_PRODUCT_IMAGE } : cartItem
      )
    });
  },

  findItem(cartId: number): CartItemDisplay | undefined {
    return this.data.items.find((item) => item.cartId === cartId);
  },

  async runOperation(operationKey: string, action: () => Promise<void>) {
    this.setData({ operationKey });
    try {
      await action();
    } catch (error) {
      const message = error instanceof Error ? error.message : '购物车操作失败';
      wx.showToast({ title: message, icon: 'none' });
      await this.loadCart();
    } finally {
      this.setData({ operationKey: '' });
    }
  },

  applyCartList(cartList: CartList) {
    const items = cartList.items.map(toCartItemDisplay);
    const validItems = items.filter((item) => !item.invalidReason);
    applyCartListSummary(cartList);
    this.setData({
      storeName: cartList.storeName || '',
      items,
      selectedTotalAmountText: formatMoney(cartList.selectedTotalAmount),
      selectedProductCount: Number(cartList.selectedProductCount || 0),
      allSelected: validItems.length > 0 && validItems.every((item) => item.selected === 1),
      hasItems: items.length > 0,
      hasValidItems: validItems.length > 0,
      hasInvalidItems: items.some((item) => Boolean(item.invalidReason))
    });
  }
});

function toCartItemDisplay(item: CartItem): CartItemDisplay {
  return {
    ...item,
    imageUrl: normalizeImageUrl(item.productImage, DEFAULT_PRODUCT_IMAGE),
    priceText: formatMoney(item.price),
    subtotalText: formatMoney(item.subtotal),
    stockText: typeof item.stock === 'number' ? `库存 ${item.stock}` : '库存待确认',
    selectedChecked: item.selected === 1
  };
}
