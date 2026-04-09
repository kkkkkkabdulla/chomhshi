const api = require('../../api/index');

Page({
  data: {
    currentType: 2, // 2 二手物品，1 失物招领
    categoryTabs: [
      { label: '二手', value: '二手' },
      { label: '失物', value: '失物' },
      { label: '随心贴', value: '随心贴' },
      { label: '互助', value: '互助' },
      { label: '公告', value: '公告' }
    ],
    activeCategory: '二手',
    form: {
      title: '',
      description: '',
      price: '',
      contact: ''
    },
    imageUrls: [],
    loading: false
  },

  switchType(e) {
    const type = Number(e.currentTarget.dataset.type);
    if (!type || type === this.data.currentType) return;
    this.setData({
      currentType: type,
      form: {
        ...this.data.form,
        price: type === 2 ? this.data.form.price : '',
        contact: type === 1 ? this.data.form.contact : ''
      }
    });
  },

  onInput(e) {
    const key = e.currentTarget.dataset.key;
    const value = e.detail.value || '';
    this.setData({
      form: {
        ...this.data.form,
        [key]: value
      }
    });
  },

  onCategorySelect(e) {
    const value = e.currentTarget.dataset.value;
    if (!value) return;
    this.setData({ activeCategory: value });
  },

  async chooseImages() {
    try {
      const maxCount = Math.max(0, 6 - this.data.imageUrls.length);
      if (maxCount <= 0) {
        wx.showToast({ title: '最多上传6张', icon: 'none' });
        return;
      }

      const res = await wxChooseImageAsync(Math.min(3, maxCount));
      const paths = res.tempFilePaths || [];
      if (!paths.length) return;

      wx.showLoading({ title: '上传中...' });
      const uploaded = [];
      for (const p of paths) {
        const r = await api.uploadPostImage(p);
        uploaded.push(r.data.url);
      }
      wx.hideLoading();

      this.setData({ imageUrls: [...this.data.imageUrls, ...uploaded] });
      wx.showToast({ title: '上传成功', icon: 'success' });
    } catch (e) {
      wx.hideLoading();
    }
  },

  async submit() {
    if (this.data.loading) return;

    const type = this.data.currentType;
    const f = this.data.form;

    if (!f.title.trim()) {
      wx.showToast({ title: '请填写标题', icon: 'none' });
      return;
    }
    if (!f.description.trim()) {
      wx.showToast({ title: '请填写描述', icon: 'none' });
      return;
    }
    if (type === 2 && !String(f.price).trim()) {
      wx.showToast({ title: '请填写价格', icon: 'none' });
      return;
    }
    if (!f.contact.trim()) {
      wx.showToast({ title: '请填写联系方式', icon: 'none' });
      return;
    }

    const payload = {
      type,
      title: f.title.trim(),
      description: f.description.trim(),
      category: this.data.activeCategory,
      price: type === 2 ? Number(f.price) : null,
      images: JSON.stringify(this.data.imageUrls),
      contact: f.contact.trim(),
      location: null,
      lostFoundTime: null,
      lostStatus: type === 1 ? 1 : null
    };

    try {
      this.setData({ loading: true });
      await api.publishPost(payload);
      wx.showToast({ title: '发布成功', icon: 'success' });
      setTimeout(() => {
        wx.switchTab({ url: '/pages/index/index' });
      }, 350);
    } catch (e) {
    } finally {
      this.setData({ loading: false });
    }
  }
});

function wxChooseImageAsync(count = 3) {
  return new Promise((resolve, reject) => {
    wx.chooseImage({
      count,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: resolve,
      fail: reject
    });
  });
}
