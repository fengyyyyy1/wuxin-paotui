import { getProducts } from '../../api/catalog';
import { getMerchantOrders } from '../../api/orders';
import { ROUTES } from '../../constants/routes';
import { requireApprovedMerchant, restoreSession } from '../../services/auth';
import { localDayRange } from '../../utils/format';
import { errorMessage } from '../../utils/request';

Page({
  data: { loading: true, storeName: '五鑫商家', businessText: '', businessOpen: false, todayOrders: 0, waiting: 0, preparing: 0, waitingRider: 0, completed: 0, products: 0 },
  onShow() { void this.load(); },
  onPullDownRefresh() { void this.load().finally(() => wx.stopPullDownRefresh()); },
  async load() {
    this.setData({ loading: true });
    try {
      if (!await requireApprovedMerchant()) return; const profile = restoreSession().merchantProfile; if (!profile) return; const range = localDayRange();
      const [today, waiting, preparing, waitingRider, completed, products] = await Promise.all([
        getMerchantOrders({ pageNum: 1, pageSize: 1, ...range }), getMerchantOrders({ pageNum: 1, pageSize: 1, status: 0 }), getMerchantOrders({ pageNum: 1, pageSize: 1, status: 6 }), getMerchantOrders({ pageNum: 1, pageSize: 1, status: 7 }), getMerchantOrders({ pageNum: 1, pageSize: 1, status: 4, ...range }), getProducts({ pageNum: 1, pageSize: 1 })
      ]);
      this.setData({ storeName: profile.storeName, businessText: profile.businessStatusText, businessOpen: profile.businessStatus === 1, todayOrders: today.total, waiting: waiting.total, preparing: preparing.total, waitingRider: waitingRider.total, completed: completed.total, products: products.total });
    } catch (error) { wx.showToast({ title: errorMessage(error), icon: 'none' }); }
    finally { this.setData({ loading: false }); }
  },
  toOrders() { wx.switchTab({ url: ROUTES.orders }); }, toStore() { wx.navigateTo({ url: ROUTES.store }); }, toProducts() { wx.navigateTo({ url: ROUTES.products }); }, toCategories() { wx.navigateTo({ url: ROUTES.categories }); }, toAnalytics() { wx.navigateTo({ url: ROUTES.analytics }); }
});
