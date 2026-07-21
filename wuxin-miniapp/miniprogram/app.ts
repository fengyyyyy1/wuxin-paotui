import { restoreSession } from './services/auth';
import { restoreCartSummary } from './services/cart';

App<IAppOption>({
  globalData: {
    token: null,
    userInfo: null,
    newUser: null,
    cartCount: 0
  },
  onLaunch() {
    restoreSession();
    restoreCartSummary();
  }
});
