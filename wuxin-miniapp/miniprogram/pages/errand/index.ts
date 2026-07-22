import { ERRAND_SERVICES } from '../../constants/errand';
import { ROUTES } from '../../constants/routes';
import { requireLogin } from '../../utils/route-guard';

Page({
  data: {
    serviceRows: [
      { id: 'first', items: ERRAND_SERVICES.slice(0, 3) },
      { id: 'second', items: ERRAND_SERVICES.slice(3, 6) },
      { id: 'third', items: ERRAND_SERVICES.slice(6) }
    ]
  },

  async onShow() {
    await requireLogin();
  },

  selectService(event: WechatMiniprogram.BaseEvent) {
    const type = String(event.currentTarget.dataset.id || 'send');
    wx.navigateTo({ url: `${ROUTES.errandCreate}?type=${type}` });
  }
});
