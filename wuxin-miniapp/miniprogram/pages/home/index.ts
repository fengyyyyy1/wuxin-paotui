import { getAddressList, getStoreList } from '../../api/index';
import { HOME_BANNERS, PUBLIC_ENTRIES, SERVICE_ENTRIES } from '../../constants/home';
import { ROUTES } from '../../constants/routes';
import { getAuthState, refreshProfile } from '../../services/auth';
import type { Address } from '../../types/address';
import type { StoreListItem } from '../../types/store';
import type { UserInfo } from '../../types/user';
import { buildAddressSummary, findDefaultAddress } from '../../utils/address';
import { DEFAULT_AVATAR, DEFAULT_STORE_IMAGE, normalizeImageUrl } from '../../utils/image';
import { maskPhone } from '../../utils/phone';
import { requireLogin } from '../../utils/route-guard';

interface HomeStoreCard extends StoreListItem {
  imageUrl: string;
  statusText: string;
  statusClass: string;
  timeText: string;
  addressText: string;
}

Page({
  data: {
    userInfo: null as UserInfo | null,
    displayName: '微信用户',
    usernameText: '',
    phoneText: '未绑定',
    avatarUrl: DEFAULT_AVATAR,
    addressText: '请选择收货地址',
    hasDefaultAddress: false,
    banners: HOME_BANNERS,
    serviceEntries: SERVICE_ENTRIES,
    publicEntries: PUBLIC_ENTRIES,
    stores: [] as HomeStoreCard[],
    profileLoading: false,
    addressLoading: false,
    storeLoading: false,
    storeErrorMessage: '',
    storeEmptyText: '暂无推荐门店'
  },

  async onShow() {
    const loggedIn = await requireLogin();
    if (!loggedIn) {
      return;
    }
    this.applyUserInfo(getAuthState().userInfo);
    await Promise.all([this.refreshUserProfile(), this.loadDefaultAddress(), this.loadStores()]);
  },

  async refreshUserProfile() {
    this.setData({ profileLoading: true });
    try {
      const userInfo = await refreshProfile();
      this.applyUserInfo(userInfo);
    } catch {
      this.applyUserInfo(getAuthState().userInfo);
    } finally {
      this.setData({ profileLoading: false });
    }
  },

  async loadDefaultAddress() {
    this.setData({ addressLoading: true });
    try {
      const addresses = await getAddressList();
      const defaultAddress = findDefaultAddress(addresses);
      this.applyAddress(defaultAddress);
    } catch {
      this.setData({
        addressText: '请选择收货地址',
        hasDefaultAddress: false
      });
    } finally {
      this.setData({ addressLoading: false });
    }
  },

  async loadStores() {
    this.setData({ storeLoading: true, storeErrorMessage: '' });
    try {
      const pageResult = await getStoreList({ pageNum: 1, pageSize: 6 });
      this.setData({
        stores: pageResult.records.map(toStoreCard),
        storeEmptyText: '暂无推荐门店'
      });
    } catch (error) {
      const message = error instanceof Error ? error.message : '门店加载失败';
      this.setData({ storeErrorMessage: message });
    } finally {
      this.setData({ storeLoading: false });
    }
  },

  goToAddress() {
    wx.navigateTo({ url: ROUTES.addressList });
  },

  goToSearch() {
    wx.navigateTo({ url: ROUTES.search });
  },

  handleBannerTap(event: WechatMiniprogram.BaseEvent) {
    const banner = this.data.banners.find((item) => item.id === event.currentTarget.dataset.id);
    if (!banner) {
      return;
    }
    if (banner.actionType === 'page' && banner.target) {
      wx.navigateTo({ url: banner.target });
      return;
    }
    wx.showToast({ title: '功能建设中', icon: 'none' });
  },

  handleServiceTap(event: WechatMiniprogram.BaseEvent) {
    const service = this.data.serviceEntries.find((item) => item.id === event.currentTarget.dataset.id);
    if (!service) {
      return;
    }
    if (service.actionType === 'store') {
      wx.pageScrollTo({ selector: '#store-section', duration: 250 });
      return;
    }
    wx.showToast({ title: '功能将在后续版本开放', icon: 'none' });
  },

  goToPublicPage(event: WechatMiniprogram.BaseEvent) {
    const entry = this.data.publicEntries.find((item) => item.id === event.currentTarget.dataset.id);
    if (!entry) {
      return;
    }
    wx.navigateTo({ url: entry.route });
  },

  goToStoreDetail(event: WechatMiniprogram.BaseEvent) {
    const storeId = Number(event.currentTarget.dataset.id);
    if (!Number.isFinite(storeId)) {
      return;
    }
    wx.navigateTo({ url: `${ROUTES.storeDetail}?id=${storeId}` });
  },

  handleImageError(event: WechatMiniprogram.BaseEvent) {
    const storeId = Number(event.currentTarget.dataset.id);
    if (!Number.isFinite(storeId)) {
      return;
    }
    const failedStore = this.data.stores.find((store) => store.storeId === storeId);
    if (!failedStore || failedStore.imageUrl === DEFAULT_STORE_IMAGE) {
      return;
    }
    console.warn('[home] store image load failed:', failedStore.imageUrl);
    const stores = this.data.stores.map((store) =>
      store.storeId === storeId ? { ...store, imageUrl: DEFAULT_STORE_IMAGE } : store
    );
    this.setData({ stores });
  },

  handleAvatarError() {
    if (this.data.avatarUrl === DEFAULT_AVATAR) {
      return;
    }
    console.warn('[home] avatar image load failed:', this.data.avatarUrl);
    this.setData({ avatarUrl: DEFAULT_AVATAR });
  },

  applyUserInfo(userInfo: UserInfo | null) {
    this.setData({
      userInfo,
      displayName: userInfo?.nickname || '微信用户',
      usernameText: userInfo?.username || '',
      phoneText: maskPhone(userInfo?.phone),
      avatarUrl: normalizeImageUrl(userInfo?.avatar, DEFAULT_AVATAR)
    });
  },

  applyAddress(address: Address | null) {
    this.setData({
      addressText: buildAddressSummary(address),
      hasDefaultAddress: Boolean(address)
    });
  }
});

function toStoreCard(store: StoreListItem): HomeStoreCard {
  return {
    ...store,
    imageUrl: normalizeImageUrl(store.storeLogo, DEFAULT_STORE_IMAGE),
    statusText: store.businessStatusText || (store.businessStatus === 1 ? '营业中' : '休息中'),
    statusClass: store.businessStatus === 1 ? 'store-status-open' : 'store-status-rest',
    timeText: buildStoreTimeText(store),
    addressText: [store.district, store.detailAddress].filter(Boolean).join('')
  };
}

function buildStoreTimeText(store: StoreListItem): string {
  if (!store.openTime || !store.closeTime) {
    return '营业时间待补充';
  }
  return `${store.openTime.slice(0, 5)}-${store.closeTime.slice(0, 5)}`;
}
