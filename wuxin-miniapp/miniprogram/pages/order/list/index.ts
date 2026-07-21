import { requireLogin } from '../../../utils/route-guard';

Page({
  async onShow() {
    await requireLogin();
  }
});
