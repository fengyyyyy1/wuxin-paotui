import { getStatistics } from '../../api/rider';
import { requireApprovedRider, restoreSession } from '../../services/auth';
import { errorMessage } from '../../utils/request';

Page({
  data: { loading: true, error: '', today: 0, week: 0, month: 0, total: 0 },
  onShow() { void this.load(); },
  async load() {
    this.setData({ loading: true, error: '' });
    try { if (!await requireApprovedRider()) return; const profile = restoreSession().riderProfile; if (!profile) return; const stats = await getStatistics(profile.riderId); this.setData({ today: stats.todayCompletedCount, week: stats.weekCompletedCount, month: stats.monthCompletedCount, total: stats.totalCompletedCount }); }
    catch (error) { this.setData({ error: errorMessage(error) }); }
    finally { this.setData({ loading: false }); }
  }
});
