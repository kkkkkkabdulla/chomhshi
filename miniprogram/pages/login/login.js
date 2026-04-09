const api = require('../../api/index');

Page({
  data: {
    code: '123456',
    loading: false
  },

  onCodeInput(e) {
    this.setData({ code: e.detail.value || '' });
  },

  async onLogin() {
    if (this.data.loading) return;
    const code = (this.data.code || '').trim();
    if (!code) {
      wx.showToast({ title: '请输入 code', icon: 'none' });
      return;
    }

    try {
      this.setData({ loading: true });
      const res = await api.wxLogin({
        code,
        encryptedData: 'mock',
        iv: 'mock'
      });

      wx.setStorageSync('token', res.data.token);
      wx.setStorageSync('userInfo', res.data.user);
      wx.showToast({ title: '登录成功', icon: 'success' });
      setTimeout(() => {
        wx.switchTab({ url: '/pages/index/index' });
      }, 300);
    } catch (e) {
      // 错误已在 request 里统一提示
    } finally {
      this.setData({ loading: false });
    }
  }
});
