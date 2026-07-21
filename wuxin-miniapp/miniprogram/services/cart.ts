import { getCartList } from '../api/index';
import { STORAGE_KEYS } from '../constants/storage';
import type { CartList } from '../types/cart';
import { getToken } from '../utils/auth';

let cartCount = 0;

function getRuntimeApp(): IAppOption | null {
  try {
    const app = getApp<IAppOption>();
    return app && app.globalData ? app : null;
  } catch {
    return null;
  }
}

function persistCartCount(count: number): void {
  cartCount = Math.max(0, count);
  wx.setStorageSync(STORAGE_KEYS.cartCount, cartCount);
  const app = getRuntimeApp();
  if (app) {
    app.globalData.cartCount = cartCount;
  }
}

export function restoreCartSummary(): number {
  const value = Number(wx.getStorageSync(STORAGE_KEYS.cartCount) || 0);
  persistCartCount(Number.isFinite(value) ? value : 0);
  return cartCount;
}

export function getCartCount(): number {
  return cartCount;
}

export function applyCartListSummary(cartList: CartList | null): number {
  const count = cartList?.items.reduce((total, item) => total + Number(item.quantity || 0), 0) || 0;
  persistCartCount(count);
  return cartCount;
}

export function clearCartSummary(): void {
  wx.removeStorageSync(STORAGE_KEYS.cartCount);
  persistCartCount(0);
}

export async function refreshCartSummary(): Promise<number> {
  if (!getToken()) {
    clearCartSummary();
    return cartCount;
  }

  const cartList = await getCartList();
  return applyCartListSummary(cartList);
}

export async function refreshCartDetail(): Promise<CartList> {
  if (!getToken()) {
    clearCartSummary();
    return {
      storeId: null,
      storeName: null,
      items: [],
      selectedTotalAmount: 0,
      selectedProductCount: 0
    };
  }

  const cartList = await getCartList();
  applyCartListSummary(cartList);
  return cartList;
}
