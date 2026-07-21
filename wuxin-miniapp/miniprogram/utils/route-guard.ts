import { ROUTES } from '../constants/routes';
import { restoreSession, verifySession } from '../services/auth';

let checkingLogin = false;

export async function requireLogin(): Promise<boolean> {
  const session = restoreSession();
  if (!session.token) {
    wx.redirectTo({ url: ROUTES.login });
    return false;
  }

  if (checkingLogin) {
    return session.isLoggedIn;
  }

  checkingLogin = true;
  try {
    const valid = await verifySession();
    if (!valid) {
      wx.redirectTo({ url: ROUTES.login });
    }
    return valid;
  } finally {
    checkingLogin = false;
  }
}
