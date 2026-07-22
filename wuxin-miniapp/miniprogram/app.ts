import { restoreSession } from './services/auth';
import { restoreCartSummary } from './services/cart';
import { getPlatformHome } from './api/platform';

App<IAppOption>({
  globalData: {
    token: null,
    userInfo: null,
    newUser: null,
    cartCount: 0,
    platformHome: null
  },
  onLaunch() {
    restoreSession();
    restoreCartSummary();
    void getPlatformHome()
      .then((platformHome) => {
        const app = getApp<IAppOption>();
        if (app?.globalData) app.globalData.platformHome = platformHome;
      })
      .catch(() => undefined);
  }
});
