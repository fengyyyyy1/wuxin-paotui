import { getStoreList } from '../../api/index';
import { ROUTES } from '../../constants/routes';
import type { StoreListItem } from '../../types/store';
import { DEFAULT_STORE_IMAGE, normalizeImageUrl } from '../../utils/image';

interface SearchStoreCard extends StoreListItem {
  imageUrl: string;
  statusText: string;
  addressText: string;
}

Page({
  data: {
    keyword: '',
    stores: [] as SearchStoreCard[],
    loading: false,
    errorMessage: '',
    searched: false
  },

  onInput(event: WechatMiniprogram.Input) {
    this.setData({ keyword: event.detail.value });
  },

  async submitSearch() {
    const keyword = this.data.keyword.trim();
    if (!keyword || this.data.loading) {
      wx.showToast({ title: keyword ? '正在搜索' : '请输入搜索内容', icon: 'none' });
      return;
    }

    this.setData({ loading: true, errorMessage: '', searched: true });
    try {
      const page = await getStoreList({ pageNum: 1, pageSize: 20, keyword });
      this.setData({ stores: page.records.map(toSearchStoreCard) });
    } catch (error) {
      const message = error instanceof Error ? error.message : '搜索失败';
      this.setData({ errorMessage: message });
    } finally {
      this.setData({ loading: false });
    }
  },

  clearSearch() {
    this.setData({ keyword: '', stores: [], errorMessage: '', searched: false });
  },

  retrySearch() {
    void this.submitSearch();
  },

  goToStoreDetail(event: WechatMiniprogram.BaseEvent) {
    const storeId = Number(event.currentTarget.dataset.id);
    if (!Number.isFinite(storeId)) {
      return;
    }
    wx.navigateTo({ url: `${ROUTES.storeDetail}?id=${storeId}` });
  },

  handleStoreImageError(event: WechatMiniprogram.BaseEvent) {
    const storeId = Number(event.currentTarget.dataset.id);
    if (!Number.isFinite(storeId)) {
      return;
    }
    const failedStore = this.data.stores.find((store) => store.storeId === storeId);
    if (!failedStore || failedStore.imageUrl === DEFAULT_STORE_IMAGE) {
      return;
    }
    console.warn('[search] store image load failed:', failedStore.imageUrl);
    this.setData({
      stores: this.data.stores.map((store) =>
        store.storeId === storeId ? { ...store, imageUrl: DEFAULT_STORE_IMAGE } : store
      )
    });
  }
});

function toSearchStoreCard(store: StoreListItem): SearchStoreCard {
  return {
    ...store,
    imageUrl: normalizeImageUrl(store.storeLogo, DEFAULT_STORE_IMAGE),
    statusText: store.businessStatusText || (store.businessStatus === 1 ? '营业中' : '休息中'),
    addressText: [store.district, store.detailAddress].filter(Boolean).join('')
  };
}
