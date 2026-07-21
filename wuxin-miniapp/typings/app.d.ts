interface WuxinAppGlobalData {
  token: string | null;
  userInfo: import('../miniprogram/types/user').UserInfo | null;
  newUser: boolean | null;
  cartCount: number;
}

interface IAppOption {
  globalData: WuxinAppGlobalData;
}
