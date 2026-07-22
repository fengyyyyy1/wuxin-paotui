interface IAppOption {
  globalData: {
    token: string | null;
    userInfo: import('../miniprogram/types/auth').UserInfo | null;
    merchantProfile: import('../miniprogram/types/merchant').MerchantProfile | null;
  };
}
