import { addCart, clearCart, getAddressList, getStoreList, getStoreProducts } from '../../api/index';
import { HOME_BANNERS, PUBLIC_ENTRIES, SERVICE_ENTRIES } from '../../constants/home';
import { ROUTES } from '../../constants/routes';
import { saveRecentStore } from '../../services/discovery';
import { getAuthState, refreshProfile } from '../../services/auth';
import { refreshCartSummary } from '../../services/cart';
import type { Address } from '../../types/address';
import type { UserInfo } from '../../types/user';
import { buildAddressSummary, findDefaultAddress } from '../../utils/address';
import {
  replaceProductImage,
  replaceStoreImage,
  toProductCard,
  toStoreCard,
  type ProductCardView,
  type StoreCardView
} from '../../utils/catalog';
import { DEFAULT_AVATAR, normalizeImageUrl } from '../../utils/image';
import { requireLogin } from '../../utils/route-guard';

interface HomeProduct extends ProductCardView {
  storeId: number;
  storeName: string;
}

Page({
  data: {
    displayName: '微信用户',
    avatarUrl: DEFAULT_AVATAR,
    addressText: '请选择收货地址',
    banners: HOME_BANNERS,
    serviceEntries: SERVICE_ENTRIES,
    publicEntries: PUBLIC_ENTRIES,
    stores: [] as StoreCardView[],
    hotProducts: [] as HomeProduct[],
    recommendedProducts: [] as HomeProduct[],
    favoriteProducts: [] as HomeProduct[],
    loading: true,
    errorMessage: '',
    addingProductId: 0
  },

  async onShow() {
    if (!await requireLogin()) return;
    this.applyUser(getAuthState().userInfo);
    await this.loadHome();
  },

  async onPullDownRefresh() {
    await this.loadHome();
    wx.stopPullDownRefresh();
  },

  async loadHome() {
    this.setData({ loading: true, errorMessage: '' });
    const [profileResult, addressResult, storeResult] = await Promise.allSettled([
      refreshProfile(),
      getAddressList(),
      getStoreList({ pageNum: 1, pageSize: 8 })
    ]);

    if (profileResult.status === 'fulfilled') this.applyUser(profileResult.value);
    if (addressResult.status === 'fulfilled') this.applyAddress(findDefaultAddress(addressResult.value));

    if (storeResult.status === 'fulfilled') {
      const stores = storeResult.value.records.map(toStoreCard);
      this.setData({ stores });
      await this.loadHomeProducts(stores);
    } else {
      this.setData({ errorMessage: storeResult.reason instanceof Error ? storeResult.reason.message : '首页数据加载失败' });
    }
    this.setData({ loading: false });
  },

  async loadHomeProducts(stores: StoreCardView[]) {
    const candidates = stores.filter((store) => store.businessStatus === 1).slice(0, 3);
    const results = await Promise.allSettled(
      candidates.map((store) => getStoreProducts(store.storeId, { pageNum: 1, pageSize: 6 }))
    );
    const products: HomeProduct[] = [];
    results.forEach((result, index) => {
      if (result.status !== 'fulfilled') return;
      const store = candidates[index];
      result.value.records.forEach((product) => {
        products.push({ ...toProductCard(product, { storeId: store.storeId, storeName: store.storeName }), storeId: store.storeId, storeName: store.storeName });
      });
    });
    const bySales = [...products].sort((left, right) => Number(right.sales || 0) - Number(left.sales || 0));
    this.setData({
      hotProducts: bySales.slice(0, 4),
      recommendedProducts: products.slice(4, 8),
      favoriteProducts: products.slice(8, 12)
    });
  },

  goToAddress() { wx.navigateTo({ url: ROUTES.addressList }); },
  goToSearch() { wx.navigateTo({ url: ROUTES.search }); },
  goToStoreList() { wx.navigateTo({ url: ROUTES.storeList }); },

  handleBannerTap(event: WechatMiniprogram.BaseEvent) {
    const banner = this.data.banners.find((item) => item.id === event.currentTarget.dataset.id);
    if (banner?.target) wx.navigateTo({ url: banner.target });
  },

  handleServiceTap(event: WechatMiniprogram.BaseEvent) {
    const entry = this.data.serviceEntries.find((item) => item.id === event.currentTarget.dataset.id);
    if (entry) wx.navigateTo({ url: entry.target });
  },

  goToPublicPage(event: WechatMiniprogram.BaseEvent) {
    const type = String(event.currentTarget.dataset.id || 'missing');
    wx.navigateTo({ url: `${ROUTES.publicService}?type=${type}` });
  },

  goToStoreDetail(event: WechatMiniprogram.CustomEvent<{ id: number }>) {
    const store = this.data.stores.find((item) => item.storeId === Number(event.detail.id));
    if (!store) return;
    saveRecentStore(store);
    wx.navigateTo({ url: `${ROUTES.storeDetail}?id=${store.storeId}` });
  },

  goToProductDetail(event: WechatMiniprogram.CustomEvent<{ id: number }>) {
    const product = this.findProduct(Number(event.detail.id));
    if (!product) return;
    wx.navigateTo({ url: `${ROUTES.productDetail}?id=${product.productId}&storeId=${product.storeId}&storeName=${encodeURIComponent(product.storeName)}&storeCanSell=1` });
  },

  async addProduct(event: WechatMiniprogram.CustomEvent<{ id: number }>) {
    const productId = Number(event.detail.id);
    if (!productId || this.data.addingProductId) return;
    this.setData({ addingProductId: productId });
    try {
      await addCart({ productId, quantity: 1 });
      await refreshCartSummary();
      wx.showToast({ title: '已加入购物车', icon: 'success' });
    } catch (error) {
      const message = error instanceof Error ? error.message : '加入购物车失败';
      if (message.includes('购物车') && message.includes('其他')) {
        this.confirmReplaceCart(productId);
      } else {
        wx.showToast({ title: message, icon: 'none' });
      }
    } finally {
      this.setData({ addingProductId: 0 });
    }
  },

  confirmReplaceCart(productId: number) {
    wx.showModal({
      title: '更换门店商品',
      content: '购物车中已有其他门店商品，是否清空后加入当前商品？',
      confirmText: '清空并加入',
      success: (result) => {
        if (!result.confirm) return;
        void clearCart().then(() => addCart({ productId, quantity: 1 })).then(() => refreshCartSummary()).then(() => {
          wx.showToast({ title: '已加入购物车', icon: 'success' });
        }).catch((error: Error) => wx.showToast({ title: error.message || '加入失败', icon: 'none' }));
      }
    });
  },

  handleStoreImageError(event: WechatMiniprogram.CustomEvent<{ id: number }>) {
    this.setData({ stores: replaceStoreImage(this.data.stores, Number(event.detail.id)) });
  },

  handleProductImageError(event: WechatMiniprogram.CustomEvent<{ id: number }>) {
    const id = Number(event.detail.id);
    this.setData({
      hotProducts: replaceProductImage(this.data.hotProducts, id) as HomeProduct[],
      recommendedProducts: replaceProductImage(this.data.recommendedProducts, id) as HomeProduct[],
      favoriteProducts: replaceProductImage(this.data.favoriteProducts, id) as HomeProduct[]
    });
  },

  handleAvatarError() { this.setData({ avatarUrl: DEFAULT_AVATAR }); },
  retry() { void this.loadHome(); },

  applyUser(userInfo: UserInfo | null) {
    this.setData({ displayName: userInfo?.nickname || '微信用户', avatarUrl: normalizeImageUrl(userInfo?.avatar, DEFAULT_AVATAR) });
  },

  applyAddress(address: Address | null) { this.setData({ addressText: buildAddressSummary(address) }); },

  findProduct(productId: number): HomeProduct | undefined {
    return [...this.data.hotProducts, ...this.data.recommendedProducts, ...this.data.favoriteProducts]
      .find((item) => item.productId === productId);
  }
});
