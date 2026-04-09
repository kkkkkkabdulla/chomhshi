const api = require('../../api/index');

Page({
  data: {
    user: {},
    myPosts: []
  },

  onShow() {
    this.loadUser();
    this.loadMyPosts();
  },

  async loadUser() {
    try {
      const res = await api.getUserInfo();
      this.setData({ user: res.data || {} });
    } catch (e) {}
  },

  async loadMyPosts() {
    try {
      const res = await api.getMyPostList({ page: 1, pageSize: 20 });
      this.setData({ myPosts: (res.data && res.data.list) || [] });
    } catch (e) {}
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/detail/detail?id=${id}` });
  },

  async onDeletePost(e) {
    if (e && e.stopPropagation) e.stopPropagation();
    const id = e.currentTarget.dataset.id;
    if (!id) return;

    try {
      await showModalAsync({
        title: '确认删除',
        content: '删除后不可恢复，是否继续？'
      });
      await api.deletePost(id);
      wx.showToast({ title: '删除成功', icon: 'success' });
      this.loadMyPosts();
    } catch (err) {}
  },

  onLogout() {
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
    wx.reLaunch({ url: '/pages/login/login' });
  }
});

function showModalAsync({ title, content }) {
  return new Promise((resolve, reject) => {
    wx.showModal({
      title,
      content,
      success: (res) => {
        if (res.confirm) resolve(true);
        else reject(new Error('cancel'));
      },
      fail: reject
    });
  });
}
