const api = require('../../api/index');

Page({
  data: {
    user: {},
    myPosts: [],
    editingProfile: false,
    profileForm: {
      avatarUrl: '',
      nickname: '',
      phone: ''
    }
  },

  onShow() {
    this.loadUser();
    this.loadMyPosts();
  },

  async loadUser() {
    try {
      const res = await api.getUserInfo();
      const user = res.data || {};
      this.setData({
        user,
        profileForm: {
          avatarUrl: user.avatarUrl || '/assets/images/default-avatar.svg',
          nickname: user.nickname || '',
          phone: user.phone || ''
        }
      });
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
    wx.navigateTo({ url: `/pages/detail/detail?id=${id}&owner=1` });
  },

  startEditProfile() {
    this.setData({ editingProfile: true });
  },

  cancelEditProfile() {
    this.setData({
      editingProfile: false,
      profileForm: {
        avatarUrl: this.data.user.avatarUrl || '/assets/images/default-avatar.svg',
        nickname: this.data.user.nickname || '',
        phone: this.data.user.phone || ''
      }
    });
  },

  onProfileInput(e) {
    const key = e.currentTarget.dataset.key;
    const value = e.detail.value || '';
    this.setData({
      profileForm: {
        ...this.data.profileForm,
        [key]: value
      }
    });
  },

  chooseAvatar() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const filePath = res.tempFilePaths && res.tempFilePaths[0];
        if (!filePath) return;
        wx.showLoading({ title: '上传中...' });
        api.uploadPostImage(filePath)
          .then((r) => {
            this.setData({ 'profileForm.avatarUrl': r.data.url });
            wx.showToast({ title: '头像已选中', icon: 'success' });
          })
          .catch(() => {})
          .finally(() => wx.hideLoading());
      }
    });
  },

  async saveProfile() {
    const f = this.data.profileForm;
    if (!f.nickname.trim()) {
      wx.showToast({ title: '请填写昵称', icon: 'none' });
      return;
    }
    if (!f.phone.trim()) {
      wx.showToast({ title: '请填写手机号', icon: 'none' });
      return;
    }
    if (!/^1[3-9]\d{9}$/.test(f.phone.trim())) {
      wx.showToast({ title: '手机号格式不对', icon: 'none' });
      return;
    }
    try {
      await api.updateUserInfo({
        nickname: f.nickname.trim(),
        avatarUrl: f.avatarUrl || '/assets/images/default-avatar.svg',
        phone: f.phone.trim()
      });
      wx.showToast({ title: '保存成功', icon: 'success' });
      this.setData({ editingProfile: false });
      this.loadUser();
    } catch (e) {}
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
