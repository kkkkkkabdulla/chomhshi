App({
  globalData: {
    userInfo: null
  },

  onLaunch() {
    const token = wx.getStorageSync('token');
    if (!token) return;
    // 可在此做 verifyToken 预检
  }
});
