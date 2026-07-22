interface WuxinAppGlobalData {
  token: string | null;
  userInfo: import('../miniprogram/types/user').UserInfo | null;
  newUser: boolean | null;
  cartCount: number;
  platformHome: import('../miniprogram/types/platform').PlatformHome | null;
}

interface IAppOption {
  globalData: WuxinAppGlobalData;
}
