import { getMerchantOrders } from '../../api/orders';
import { localDayRange } from '../../utils/format';
import { errorMessage } from '../../utils/request';
Page({
  data: { loading: true, error: '', total: 0, waiting: 0, preparing: 0, waitingRider: 0, completed: 0, cancelled: 0 },
  onShow() { void this.load(); }, onPullDownRefresh() { void this.load().finally(() => wx.stopPullDownRefresh()); },
  async load() { this.setData({ loading: true, error: '' }); try { const range = localDayRange(); const queries = [undefined, 0, 6, 7, 4, 5].map(status => getMerchantOrders({ pageNum: 1, pageSize: 1, status, ...range })); const [total, waiting, preparing, waitingRider, completed, cancelled] = await Promise.all(queries); this.setData({ total: total.total, waiting: waiting.total, preparing: preparing.total, waitingRider: waitingRider.total, completed: completed.total, cancelled: cancelled.total }); } catch (error) { this.setData({ error: errorMessage(error) }); } finally { this.setData({ loading: false }); } }
});
