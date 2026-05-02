const api = require('../../api/index');

function getCategoryLabel(item) {
  if (item.category) return item.category;
  if (item.type === 1) return '失物招领';
  if (item.type === 2) return '二手交易';
  if (item.type === 4) return '求助';
  if (item.type === 5) return '自由动态';
  return '其他';
}

function parseTags(tags) {
  if (!tags) return [];
  if (Array.isArray(tags)) return tags;
  const text = String(tags).trim();
  if (!text) return [];
  try {
    const arr = JSON.parse(text);
    return Array.isArray(arr) ? arr : [];
  } catch (e) {
    return text.split(',').map((s) => s.trim()).filter(Boolean);
  }
}

Page({
  data: {
    user: {},
    editingProfile: false,
    profileForm: { nickname: '', phone: '', avatarUrl: '' },
    activeSection: 'collects',
    myCollects: [],
    myPosts: []
  },

  onShow() {
    this.loadUserInfo();
    this.loadMyCollects();
    this.loadMyPosts();
  },

  async loadUserInfo() {
    try {
      const res = await api.getUserInfo();
      this.setData({ user: res.data || {} });
    } catch (e) {}
  },

  async loadMyCollects() {
    try {
      const res = await api.getMyCollects({ page: 1, pageSize: 20 });
      const list = (res.data && res.data.list) || [];
      const decorated = list.map((item) => ({
        ...item,
        imageList: parseImages(item.images).slice(0, 3),
        summary: pickSummary(item.description),
        categoryLabel: getCategoryLabel(item),
        tagList: parseTags(item.tags)
      }));
      this.setData({ myCollects: decorated });
    } catch (e) {}
  },

  async loadMyPosts() {
    try {
      const res = await api.getMyPostList({ page: 1, pageSize: 20 });
      const list = (res.data && res.data.list) || [];
      const decorated = list.map((item) => ({
        ...item,
        imageList: parseImages(item.images).slice(0, 3),
        categoryLabel: getCategoryLabel(item),
        tagList: parseTags(item.tags)
      }));
      this.setData({ myPosts: decorated });
    } catch (e) {}
  },

  startEditProfile() {
    const u = this.data.user || {};
    this.setData({
      editingProfile: true,
      profileForm: {
        nickname: u.nickname || '',
        phone: u.phone || '',
        avatarUrl: u.avatarUrl || ''
      }
    });
  },

  cancelEditProfile() {
    this.setData({ editingProfile: false });
  },

  onProfileInput(e) {
    const key = e.currentTarget.dataset.key;
    this.setData({
      profileForm: {
        ...this.data.profileForm,
        [key]: e.detail.value || ''
      }
    });
  },

  chooseAvatar() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: async (res) => {
        const path = res.tempFilePaths[0];
        try {
          wx.showLoading({ title: '上传中...' });
          const uploadRes = await api.uploadPostImage(path);
          this.setData({
            profileForm: {
              ...this.data.profileForm,
              avatarUrl: uploadRes.data.url
            }
          });
          wx.hideLoading();
        } catch (e) {
          wx.hideLoading();
          wx.showToast({ title: '上传失败', icon: 'none' });
        }
      }
    });
  },

  async saveProfile() {
    const form = this.data.profileForm;
    if (!form.nickname.trim()) {
      wx.showToast({ title: '请输入昵称', icon: 'none' });
      return;
    }

    try {
      await api.updateUserInfo(form);
      wx.showToast({ title: '保存成功', icon: 'success' });
      this.setData({ editingProfile: false });
      await this.loadUserInfo();
    } catch (e) {}
  },

  switchSection(e) {
    const section = e.currentTarget.dataset.section;
    this.setData({ activeSection: section });
  },

  onSectionSwipe(e) {
    const idx = e.detail.current;
    this.setData({ activeSection: idx === 0 ? 'collects' : 'posts' });
  },

  goCollectDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/detail/detail?id=${id}` });
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/detail/detail?id=${id}&owner=1` });
  },

  async onDeletePost(e) {
    const id = Number(e.currentTarget.dataset.id);
    if (!id) return;

    try {
      const modalRes = await showModalAsync('确认删除', '删除后不可恢复，确定要删除吗？');
      if (!modalRes.confirm) return;

      await api.deletePost(id);
      wx.showToast({ title: '已删除', icon: 'success' });
      await this.loadMyPosts();
    } catch (e) {}
  },

  onLogout() {
    wx.removeStorageSync('token');
    wx.reLaunch({ url: '/pages/login/login' });
  }
});

function pickSummary(text) {
  if (!text) return '暂无描述';
  const t = String(text).trim();
  return t.length > 50 ? `${t.slice(0, 50)}...` : t;
}

function parseImages(images) {
  if (!images) return [];
  if (Array.isArray(images)) return images;
  const text = String(images).trim();
  if (!text) return [];
  try {
    const arr = JSON.parse(text);
    return Array.isArray(arr) ? arr : [];
  } catch (e) {
    return text.split(',').map((s) => s.trim()).filter(Boolean);
  }
}

function showModalAsync(title, content) {
  return new Promise((resolve) => {
    wx.showModal({
      title,
      content,
      success: resolve,
      fail: () => resolve({ confirm: false })
    });
  });
}
