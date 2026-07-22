interface IAppOption {
  globalData: {
    token: string | null;
    userInfo: import('../miniprogram/types/auth').UserInfo | null;
    riderProfile: import('../miniprogram/types/rider').RiderProfile | null;
  };
}
