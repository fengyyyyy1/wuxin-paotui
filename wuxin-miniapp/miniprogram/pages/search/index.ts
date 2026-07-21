import { getStoreList } from '../../api/index';
import { ROUTES } from '../../constants/routes';
import { clearSearchHistory, getRecentStores, getSearchHistory, saveRecentStore, saveSearchKeyword } from '../../services/discovery';
import { replaceStoreImage, toStoreCard, type StoreCardView } from '../../utils/catalog';

Page({
  data: {
    keyword: '',
    stores: [] as StoreCardView[],
    discoveryStores: [] as StoreCardView[],
    recentStores: [] as StoreCardView[],
    history: [] as string[],
    suggestions: [] as string[],
    loading: false,
    errorMessage: '',
    searched: false
  },

  onLoad() {
    this.setData({ history: getSearchHistory(), recentStores: getRecentStores() });
    void this.loadDiscovery();
  },

  async loadDiscovery() {
    try {
      const page = await getStoreList({ pageNum: 1, pageSize: 20 });
      this.setData({ discoveryStores: page.records.map(toStoreCard) });
    } catch {
      this.setData({ discoveryStores: [] });
    }
  },

  onInput(event: WechatMiniprogram.Input) {
    const keyword = event.detail.value;
    const normalized = keyword.trim().toLowerCase();
    const suggestions = normalized
      ? this.data.discoveryStores.filter((store) => store.storeName.toLowerCase().includes(normalized)).slice(0, 5).map((store) => store.storeName)
      : [];
    this.setData({ keyword, suggestions });
  },

  useKeyword(event: WechatMiniprogram.BaseEvent) {
    this.setData({ keyword: String(event.currentTarget.dataset.value || ''), suggestions: [] });
    void this.submitSearch();
  },

  async submitSearch() {
    const keyword = this.data.keyword.trim();
    if (!keyword || this.data.loading) {
      if (!keyword) wx.showToast({ title: '请输入搜索内容', icon: 'none' });
      return;
    }
    this.setData({ loading: true, errorMessage: '', searched: true, suggestions: [], history: saveSearchKeyword(keyword) });
    try {
      const page = await getStoreList({ pageNum: 1, pageSize: 30, keyword });
      this.setData({ stores: page.records.map(toStoreCard) });
    } catch (error) {
      this.setData({ errorMessage: error instanceof Error ? error.message : '搜索失败' });
    } finally {
      this.setData({ loading: false });
    }
  },

  clearInput() { this.setData({ keyword: '', stores: [], suggestions: [], searched: false, errorMessage: '' }); },
  clearHistory() { clearSearchHistory(); this.setData({ history: [] }); },
  retry() { void this.submitSearch(); },

  openStore(event: WechatMiniprogram.CustomEvent<{ id: number }>) {
    const allStores = [...this.data.stores, ...this.data.recentStores, ...this.data.discoveryStores];
    const store = allStores.find((item) => item.storeId === Number(event.detail.id));
    if (!store) return;
    saveRecentStore(store);
    wx.navigateTo({ url: `${ROUTES.storeDetail}?id=${store.storeId}` });
  },

  handleImageError(event: WechatMiniprogram.CustomEvent<{ id: number }>) {
    const id = Number(event.detail.id);
    this.setData({ stores: replaceStoreImage(this.data.stores, id), recentStores: replaceStoreImage(this.data.recentStores, id) });
  }
});
