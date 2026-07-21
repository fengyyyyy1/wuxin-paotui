import {
  addCart,
  clearCart,
  deleteCartItem,
  getStoreCategories,
  getStoreDetail,
  getStoreProducts,
  updateCartQuantity
} from '../../../api/index';
import { ROUTES } from '../../../constants/routes';
import { refreshCartDetail as fetchCartDetail, restoreCartSummary } from '../../../services/cart';
import type { CartItem, CartList } from '../../../types/cart';
import type { ProductCategory, ProductItem } from '../../../types/product';
import type { StoreDetail } from '../../../types/store';
import { formatMoney } from '../../../utils/format';
import { DEFAULT_PRODUCT_IMAGE, DEFAULT_STORE_IMAGE, normalizeImageUrl } from '../../../utils/image';
import { requireLogin } from '../../../utils/route-guard';

interface CategoryTab {
  categoryId: number | null;
  categoryName: string;
}

interface ProductCard extends ProductItem {
  imageUrl: string;
  priceText: string;
  stockText: string;
  cartId: number | null;
  cartQuantity: number;
  cartInvalidReason: string;
  cartOperating: boolean;
  canQuickAdd: boolean;
}

interface StoreDetailDisplay extends StoreDetail {
  imageUrl: string;
  statusText: string;
  statusClass: string;
  timeText: string;
  addressText: string;
  canSell: boolean;
}

Page({
  data: {
    storeId: 0,
    store: null as StoreDetailDisplay | null,
    categories: [{ categoryId: null, categoryName: '全部商品' }] as CategoryTab[],
    activeCategoryId: null as number | null,
    products: [] as ProductCard[],
    keyword: '',
    pageNum: 1,
    pageSize: 10,
    total: 0,
    hasMore: false,
    loadingStore: false,
    loadingProducts: false,
    errorMessage: '',
    productErrorMessage: '',
    productEmptyText: '暂无可售商品',
    cartCount: 0,
    cartStoreName: '',
    cartSelectedTotalAmountText: '0.00',
    cartSelectedProductCount: 0,
    cartItems: [] as ProductCard[],
    cartValidItems: [] as ProductCard[],
    cartInvalidItems: [] as ProductCard[],
    cartEmpty: true,
    cartPanelVisible: false,
    cartLoading: false,
    cartOperationKey: ''
  },

  onLoad(options: { id?: string }) {
    const storeId = Number(options.id);
    if (!Number.isFinite(storeId) || storeId <= 0) {
      this.setData({ errorMessage: '门店ID不合法' });
      return;
    }
    this.setData({ storeId, cartCount: restoreCartSummary() });
    void this.loadPage();
    void this.refreshCartDetail();
  },

  onShow() {
    this.setData({ cartCount: restoreCartSummary() });
    void this.refreshCartDetail();
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loadingProducts) {
      void this.loadProducts(this.data.pageNum + 1, true);
    }
  },

  async loadPage() {
    await Promise.all([this.loadStoreDetail(), this.loadCategories()]);
    await this.loadProducts(1, false);
  },

  async loadStoreDetail() {
    if (!this.data.storeId) {
      return;
    }
    this.setData({ loadingStore: true, errorMessage: '' });
    try {
      const store = await getStoreDetail(this.data.storeId);
      this.setData({ store: toStoreDetailDisplay(store) });
    } catch (error) {
      const message = error instanceof Error ? error.message : '门店详情加载失败';
      this.setData({ errorMessage: message, store: null });
    } finally {
      this.setData({ loadingStore: false });
    }
  },

  async loadCategories() {
    if (!this.data.storeId) {
      return;
    }
    try {
      const categories = await getStoreCategories(this.data.storeId);
      this.setData({ categories: buildCategoryTabs(categories) });
    } catch {
      this.setData({ categories: [{ categoryId: null, categoryName: '全部商品' }] });
    }
  },

  async loadProducts(pageNum: number, append: boolean) {
    if (!this.data.storeId || this.data.loadingProducts) {
      return;
    }

    this.setData({ loadingProducts: true, productErrorMessage: '' });
    try {
      const page = await getStoreProducts(this.data.storeId, {
        pageNum,
        pageSize: this.data.pageSize,
        categoryId: this.data.activeCategoryId || undefined,
        keyword: this.data.keyword.trim() || undefined
      });
      const nextProducts = page.records.map((product) => toProductCard(product, this.data.store?.canSell ?? true));
      const products = append ? [...this.data.products, ...nextProducts] : nextProducts;
      this.setData({
        products: mergeProductsWithCart(products, this.data.cartItems),
        pageNum: Number(page.pageNum || pageNum),
        total: Number(page.total || 0),
        hasMore: Number(page.pageNum || pageNum) < Number(page.pages || 0),
        productEmptyText: this.data.keyword.trim() ? '没有找到相关商品' : '暂无可售商品'
      });
    } catch (error) {
      const message = error instanceof Error ? error.message : '商品加载失败';
      this.setData({ productErrorMessage: message });
    } finally {
      this.setData({ loadingProducts: false });
    }
  },

  refreshAll() {
    void this.loadPage();
  },

  refreshProducts() {
    void this.loadProducts(1, false);
  },

  selectCategory(event: WechatMiniprogram.BaseEvent) {
    const value = event.currentTarget.dataset.id;
    const categoryId = value === 'all' ? null : Number(value);
    if (categoryId === this.data.activeCategoryId || (categoryId !== null && !Number.isFinite(categoryId))) {
      return;
    }
    this.setData({ activeCategoryId: categoryId, products: [], pageNum: 1 });
    void this.loadProducts(1, false);
  },

  onKeywordInput(event: WechatMiniprogram.Input) {
    this.setData({ keyword: event.detail.value });
  },

  searchProducts() {
    if (this.data.loadingProducts) {
      return;
    }
    this.setData({ products: [], pageNum: 1 });
    void this.loadProducts(1, false);
  },

  clearKeyword() {
    if (!this.data.keyword) {
      return;
    }
    this.setData({ keyword: '', products: [], pageNum: 1 });
    void this.loadProducts(1, false);
  },

  goToProductDetail(event: WechatMiniprogram.BaseEvent) {
    const productId = Number(event.currentTarget.dataset.id);
    if (!Number.isFinite(productId)) {
      return;
    }
    const storeName = encodeURIComponent(this.data.store?.storeName || '');
    const storeCanSell = this.data.store?.canSell ? 1 : 0;
    wx.navigateTo({
      url: `${ROUTES.productDetail}?id=${productId}&storeId=${this.data.storeId}&storeName=${storeName}&storeCanSell=${storeCanSell}`
    });
  },

  goToCart() {
    wx.navigateTo({ url: ROUTES.cart });
  },

  async quickAddCart(event: WechatMiniprogram.BaseEvent) {
    const productId = Number(event.currentTarget.dataset.id);
    const product = this.findProduct(productId);
    if (!product || !product.canQuickAdd || product.cartOperating || this.data.cartOperationKey) {
      return;
    }
    const loggedIn = await requireLogin();
    if (!loggedIn) {
      return;
    }
    await this.runCartOperation(`product-${productId}`, async () => {
      try {
        await addCart({ productId, quantity: 1 });
        wx.showToast({ title: '已加入购物车', icon: 'success' });
        await this.refreshCartDetail();
      } catch (error) {
        const message = error instanceof Error ? error.message : '加入购物车失败';
        if (isStoreConflictMessage(message)) {
          this.showStoreConflictConfirm(productId);
          return;
        }
        wx.showToast({ title: message, icon: 'none' });
        await this.refreshCartDetail();
      }
    });
  },

  async increaseCartItem(event: WechatMiniprogram.BaseEvent) {
    const productId = Number(event.currentTarget.dataset.id);
    const product = this.findProduct(productId);
    if (!product || !product.cartId || product.cartOperating || this.data.cartOperationKey) {
      return;
    }
    if (product.stock > 0 && product.cartQuantity >= product.stock) {
      wx.showToast({ title: '不能超过当前库存', icon: 'none' });
      return;
    }
    await this.runCartOperation(`product-${productId}`, async () => {
      await updateCartQuantity({ cartId: product.cartId as number, quantity: product.cartQuantity + 1 });
      await this.refreshCartDetail();
    });
  },

  async decreaseCartItem(event: WechatMiniprogram.BaseEvent) {
    const productId = Number(event.currentTarget.dataset.id);
    const product = this.findProduct(productId);
    if (!product || !product.cartId || product.cartOperating || this.data.cartOperationKey) {
      return;
    }
    await this.runCartOperation(`product-${productId}`, async () => {
      if (product.cartQuantity <= 1) {
        await deleteCartItem(product.cartId as number);
      } else {
        await updateCartQuantity({ cartId: product.cartId as number, quantity: product.cartQuantity - 1 });
      }
      await this.refreshCartDetail();
    });
  },

  async deleteCartProduct(event: WechatMiniprogram.BaseEvent) {
    const productId = Number(event.currentTarget.dataset.id);
    const product = this.findProduct(productId);
    if (!product || !product.cartId || product.cartOperating || this.data.cartOperationKey) {
      return;
    }
    await this.runCartOperation(`product-${productId}`, async () => {
      await deleteCartItem(product.cartId as number);
      await this.refreshCartDetail();
    });
  },

  openCartPanel() {
    this.setData({ cartPanelVisible: true });
  },

  closeCartPanel() {
    this.setData({ cartPanelVisible: false });
  },

  preventPanelMove() {
    return;
  },

  clearCurrentCart() {
    if (this.data.cartEmpty || this.data.cartOperationKey) {
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
        void this.runCartOperation('clear-all', async () => {
          await clearCart();
          await this.refreshCartDetail();
        });
      }
    });
  },

  checkout() {
    if (!this.data.cartSelectedProductCount) {
      wx.showToast({ title: '请先选择商品', icon: 'none' });
      return;
    }
    wx.showToast({ title: '提交订单功能将在 V1.7-9 开放', icon: 'none' });
  },

  handleStoreImageError() {
    if (!this.data.store || this.data.store.imageUrl === DEFAULT_STORE_IMAGE) {
      return;
    }
    console.warn('[store-detail] store image load failed:', this.data.store.imageUrl);
    this.setData({ store: { ...this.data.store, imageUrl: DEFAULT_STORE_IMAGE } });
  },

  handleProductImageError(event: WechatMiniprogram.BaseEvent) {
    const productId = Number(event.currentTarget.dataset.id);
    if (!Number.isFinite(productId)) {
      return;
    }
    const failedProduct = this.findProduct(productId);
    if (!failedProduct || failedProduct.imageUrl === DEFAULT_PRODUCT_IMAGE) {
      return;
    }
    console.warn('[store-detail] product image load failed:', failedProduct.imageUrl);
    this.setData({
      products: this.data.products.map((product) =>
        product.productId === productId ? { ...product, imageUrl: DEFAULT_PRODUCT_IMAGE } : product
      ),
      cartItems: this.data.cartItems.map((item) =>
        item.productId === productId ? { ...item, imageUrl: DEFAULT_PRODUCT_IMAGE } : item
      ),
      cartValidItems: this.data.cartValidItems.map((item) =>
        item.productId === productId ? { ...item, imageUrl: DEFAULT_PRODUCT_IMAGE } : item
      ),
      cartInvalidItems: this.data.cartInvalidItems.map((item) =>
        item.productId === productId ? { ...item, imageUrl: DEFAULT_PRODUCT_IMAGE } : item
      )
    });
  },

  async refreshCartDetail() {
    this.setData({ cartLoading: true });
    try {
      const cartList = await fetchCartDetail();
      this.applyCartList(cartList);
    } catch (error) {
      const message = error instanceof Error ? error.message : '购物车加载失败';
      wx.showToast({ title: message, icon: 'none' });
    } finally {
      this.setData({ cartLoading: false });
    }
  },

  applyCartList(cartList: CartList) {
    const cartItems = cartList.items.map((item) => toCartProductCard(item, this.data.store?.canSell ?? true));
    const validItems = cartItems.filter((item) => !item.cartInvalidReason);
    const invalidItems = cartItems.filter((item) => item.cartInvalidReason);
    const cartCount = cartItems.reduce((total, item) => total + item.cartQuantity, 0);
    const products = mergeProductsWithCart(this.data.products, cartItems);
    this.setData({
      cartCount,
      cartStoreName: cartList.storeName || '',
      cartSelectedTotalAmountText: formatMoney(cartList.selectedTotalAmount),
      cartSelectedProductCount: Number(cartList.selectedProductCount || 0),
      cartItems,
      cartValidItems: validItems,
      cartInvalidItems: invalidItems,
      cartEmpty: cartItems.length === 0,
      cartPanelVisible: cartItems.length === 0 ? false : this.data.cartPanelVisible,
      products
    });
  },

  findProduct(productId: number): ProductCard | undefined {
    return (
      this.data.products.find((product) => product.productId === productId) ||
      this.data.cartItems.find((product) => product.productId === productId)
    );
  },

  async runCartOperation(operationKey: string, action: () => Promise<void>) {
    this.setData({
      cartOperationKey: operationKey,
      products: this.data.products.map((product) =>
        operationKey === `product-${product.productId}` ? { ...product, cartOperating: true } : product
      ),
      cartItems: this.data.cartItems.map((item) =>
        operationKey === `product-${item.productId}` ? { ...item, cartOperating: true } : item
      ),
      cartValidItems: this.data.cartValidItems.map((item) =>
        operationKey === `product-${item.productId}` ? { ...item, cartOperating: true } : item
      ),
      cartInvalidItems: this.data.cartInvalidItems.map((item) =>
        operationKey === `product-${item.productId}` ? { ...item, cartOperating: true } : item
      )
    });
    try {
      await action();
    } catch (error) {
      const message = error instanceof Error ? error.message : '购物车操作失败';
      wx.showToast({ title: message, icon: 'none' });
      await this.refreshCartDetail();
    } finally {
      this.setData({ cartOperationKey: '' });
    }
  },

  showStoreConflictConfirm(productId: number) {
    wx.showModal({
      title: '清空原购物车',
      content: '购物车中已有其他门店商品，是否清空并加入当前商品？',
      confirmText: '清空并加入',
      cancelText: '取消',
      success: (result) => {
        if (!result.confirm) {
          return;
        }
        void this.runCartOperation(`product-${productId}`, async () => {
          await clearCart();
          await addCart({ productId, quantity: 1 });
          wx.showToast({ title: '已清空并加入购物车', icon: 'success' });
          await this.refreshCartDetail();
        });
      }
    });
  }
});

function buildCategoryTabs(categories: ProductCategory[]): CategoryTab[] {
  return [
    { categoryId: null, categoryName: '全部商品' },
    ...categories.map((category) => ({
      categoryId: category.categoryId,
      categoryName: category.categoryName
    }))
  ];
}

function toStoreDetailDisplay(store: StoreDetail): StoreDetailDisplay {
  const canSell = store.businessStatus === 1;
  return {
    ...store,
    imageUrl: normalizeImageUrl(store.storeLogo, DEFAULT_STORE_IMAGE),
    statusText: store.businessStatusText || (canSell ? '营业中' : '休息中'),
    statusClass: canSell ? 'status-open' : 'status-rest',
    timeText: buildStoreTimeText(store),
    addressText: [store.province, store.city, store.district, store.detailAddress].filter(Boolean).join(''),
    canSell
  };
}

function toProductCard(product: ProductItem, storeCanSell: boolean): ProductCard {
  const canQuickAdd = storeCanSell && product.stock > 0;
  return {
    ...product,
    imageUrl: normalizeImageUrl(product.productImage, DEFAULT_PRODUCT_IMAGE),
    priceText: formatMoney(product.price),
    stockText: product.stock > 0 ? `库存 ${product.stock}` : '已售罄',
    cartId: null,
    cartQuantity: 0,
    cartInvalidReason: '',
    cartOperating: false,
    canQuickAdd
  };
}

function toCartProductCard(item: CartItem, storeCanSell: boolean): ProductCard {
  const stock = Number(item.stock || 0);
  return {
    productId: item.productId,
    categoryId: 0,
    categoryName: null,
    productName: item.productName,
    productImage: item.productImage,
    productDescription: item.invalidReason || null,
    price: Number(item.price || 0),
    originalPrice: null,
    stock,
    imageUrl: normalizeImageUrl(item.productImage, DEFAULT_PRODUCT_IMAGE),
    priceText: formatMoney(item.price),
    stockText: stock > 0 ? `库存 ${stock}` : '已售罄',
    cartId: item.cartId,
    cartQuantity: Number(item.quantity || 0),
    cartInvalidReason: item.invalidReason || '',
    cartOperating: false,
    canQuickAdd: storeCanSell && stock > 0 && !item.invalidReason
  };
}

function mergeProductsWithCart(products: ProductCard[], cartItems: ProductCard[]): ProductCard[] {
  return products.map((product) => {
    const cartItem = cartItems.find((item) => item.productId === product.productId);
    if (!cartItem) {
      return { ...product, cartId: null, cartQuantity: 0, cartInvalidReason: '', cartOperating: false };
    }
    return {
      ...product,
      cartId: cartItem.cartId,
      cartQuantity: cartItem.cartQuantity,
      cartInvalidReason: cartItem.cartInvalidReason,
      cartOperating: cartItem.cartOperating
    };
  });
}

function isStoreConflictMessage(message: string): boolean {
  return message.includes('购物车') && message.includes('其他');
}

function buildStoreTimeText(store: StoreDetail): string {
  if (!store.openTime || !store.closeTime) {
    return '营业时间待补充';
  }
  return `${store.openTime.slice(0, 5)}-${store.closeTime.slice(0, 5)}`;
}
