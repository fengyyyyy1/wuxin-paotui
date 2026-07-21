import { getStoreList } from '../../../api/index';
import { ROUTES } from '../../../constants/routes';
import { saveRecentStore } from '../../../services/discovery';
import { replaceStoreImage, toStoreCard, type StoreCardView } from '../../../utils/catalog';

Page({
  data: {
    stores: [] as StoreCardView[],
    businessStatus: null as 0 | 1 | null,
    pageNum: 1,
    pageSize: 10,
    hasMore: false,
    loading: false,
    errorMessage: ''
  },

  onLoad() { void this.loadStores(1, false); },
  onReachBottom() { if (this.data.hasMore && !this.data.loading) void this.loadStores(this.data.pageNum + 1, true); },
  async onPullDownRefresh() { await this.loadStores(1, false); wx.stopPullDownRefresh(); },

  async loadStores(pageNum: number, append: boolean) {
    if (this.data.loading) return;
    this.setData({ loading: true, errorMessage: '' });
    try {
      const page = await getStoreList({ pageNum, pageSize: this.data.pageSize, businessStatus: this.data.businessStatus ?? undefined });
      const next = page.records.map(toStoreCard);
      this.setData({
        stores: append ? [...this.data.stores, ...next] : next,
        pageNum: Number(page.pageNum),
        hasMore: Number(page.pageNum) < Number(page.pages)
      });
    } catch (error) {
      this.setData({ errorMessage: error instanceof Error ? error.message : '门店加载失败' });
    } finally {
      this.setData({ loading: false });
    }
  },

  selectStatus(event: WechatMiniprogram.BaseEvent) {
    const value = String(event.currentTarget.dataset.value);
    const businessStatus = value === 'all' ? null : Number(value) as 0 | 1;
    if (businessStatus === this.data.businessStatus) return;
    this.setData({ businessStatus, stores: [] });
    void this.loadStores(1, false);
  },

  openStore(event: WechatMiniprogram.CustomEvent<{ id: number }>) {
    const store = this.data.stores.find((item) => item.storeId === Number(event.detail.id));
    if (!store) return;
    saveRecentStore(store);
    wx.navigateTo({ url: `${ROUTES.storeDetail}?id=${store.storeId}` });
  },

  handleImageError(event: WechatMiniprogram.CustomEvent<{ id: number }>) {
    this.setData({ stores: replaceStoreImage(this.data.stores, Number(event.detail.id)) });
  },

  retry() { void this.loadStores(1, false); }
});
