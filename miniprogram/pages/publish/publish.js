const api = require('../../api/index');

const CATEGORY_CONFIG = [
  { label: '二手交易', value: '二手交易', type: 2 },
  { label: '失物招领', value: '失物招领', type: 1 },
  { label: '求助', value: '求助', type: 4 },
  { label: '自由动态', value: '自由动态', type: 5 }
];

const PRESET_TAGS = [
  '书籍', '电子产品', '生活用品', '代跑服务',
  '学习资料', '校园活动', '寻物启事', '技能交换', '闲置物品'
];

Page({
  data: {
    currentType: 2,
    categoryTabs: CATEGORY_CONFIG,
    activeCategory: '',
    presetTags: PRESET_TAGS,
    selectedTags: [],
    form: {
      title: '',
      description: '',
      price: '',
      contact: ''
    },
    imageUrls: [],
    loading: false
  },

  onCategorySelect(e) {
    const value = e.currentTarget.dataset.value;
    const selected = this.data.categoryTabs.find((it) => it.value === value);
    if (!selected) return;
    this.setData({
      activeCategory: selected.value,
      currentType: Number(selected.type || 3)
    });
  },

  onTagToggle(e) {
    const tag = e.currentTarget.dataset.tag;
    let selectedTags = this.data.selectedTags.slice();
    const idx = selectedTags.indexOf(tag);
    if (idx > -1) {
      selectedTags.splice(idx, 1);
    } else {
      if (selectedTags.length >= 5) {
        wx.showToast({ title: '最多选择5个标签', icon: 'none' });
        return;
      }
      selectedTags.push(tag);
    }
    this.setData({ selectedTags });
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

  removeImage(e) {
    const index = Number(e.currentTarget.dataset.index);
    if (Number.isNaN(index) || index < 0) return;
    const next = this.data.imageUrls.slice();
    next.splice(index, 1);
    this.setData({ imageUrls: next });
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

    if (!this.data.activeCategory) {
      wx.showToast({ title: '请选择分类', icon: 'none' });
      return;
    }

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
    if ((type === 1 || type === 2) && !f.contact.trim()) {
      wx.showToast({ title: '请填写联系方式', icon: 'none' });
      return;
    }

    const payload = {
      type,
      title: f.title.trim(),
      description: f.description.trim(),
      category: this.data.activeCategory,
      tags: JSON.stringify(this.data.selectedTags),
      price: type === 2 ? Number(f.price) : null,
      images: JSON.stringify(this.data.imageUrls),
      contact: f.contact.trim(),
      location: null,
      lostFoundTime: type === 1 ? null : null,
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
