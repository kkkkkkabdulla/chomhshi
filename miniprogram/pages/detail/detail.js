const api = require('../../api/index');

Page({
  data: {
    id: null,
    post: null,
    liked: false,
    likeAnimating: false,
    commentContent: '',
    commentList: [],
    imageList: []
  },

  onLoad(options) {
    const id = Number(options.id);
    this.setData({ id: Number.isNaN(id) ? null : id });
  },

  onShow() {
    if (!this.data.id) {
      wx.showToast({ title: '参数错误', icon: 'none' });
      return;
    }
    this.loadAll();
  },

  async loadAll() {
    await Promise.allSettled([
      this.loadDetail(),
      this.loadLiked(),
      this.loadComments()
    ]);
  },

  async loadDetail() {
    try {
      const res = await api.getPostDetail(this.data.id);
      const post = res.data || null;
      const imageList = parseImages(post && post.images).slice(0, 6);
      this.setData({ post, imageList });
    } catch (e) {}
  },

  async loadLiked() {
    try {
      const res = await api.isLiked(this.data.id);
      this.setData({ liked: !!(res.data && res.data.liked) });
    } catch (e) {
      // 未登录会在 request.js 统一处理
    }
  },

  async loadComments() {
    try {
      const res = await api.getCommentList(this.data.id, { page: 1, pageSize: 20 });
      this.setData({ commentList: (res.data && res.data.list) || [] });
    } catch (e) {}
  },

  async onToggleLike() {
    try {
      const res = await api.toggleLike(this.data.id);
      const post = this.data.post || {};
      post.likeCount = res.data.likeCount;
      const liked = !!res.data.liked;
      this.setData({
        liked,
        post,
        likeAnimating: liked
      });
      if (liked) {
        setTimeout(() => this.setData({ likeAnimating: false }), 450);
      }
    } catch (e) {}
  },

  onCommentInput(e) {
    this.setData({ commentContent: e.detail.value || '' });
  },

  async onAddComment() {
    const content = (this.data.commentContent || '').trim();
    if (!content) {
      wx.showToast({ title: '请输入评论', icon: 'none' });
      return;
    }

    try {
      await api.addComment({
        postId: this.data.id,
        content,
        parentId: 0
      });
      this.setData({ commentContent: '' });
      await Promise.allSettled([this.loadComments(), this.loadDetail()]);
      wx.showToast({ title: '评论成功', icon: 'success' });
    } catch (e) {}
  },

  async onReport() {
    const reasons = [
      { text: '违规内容', value: 1 },
      { text: '广告骚扰', value: 2 },
      { text: '色情低俗', value: 3 },
      { text: '其他', value: 4 }
    ];
    try {
      const tap = await showActionSheetAsync(reasons.map((r) => r.text));
      const selected = reasons[tap.tapIndex];
      if (!selected) return;

      const desc = await showReportDescInput();
      if (desc === null) return;

      await api.addReport({
        postId: this.data.id,
        reasonType: selected.value,
        reasonDesc: desc
      });
      wx.showToast({ title: '举报已提交', icon: 'success' });
    } catch (e) {}
  }
});

function parseImages(images) {
  if (!images) return [];
  if (Array.isArray(images)) return images;
  if (typeof images !== 'string') return [];

  const text = images.trim();
  if (!text) return [];

  try {
    const arr = JSON.parse(text);
    return Array.isArray(arr) ? arr : [];
  } catch (e) {
    return text.split(',').map(s => s.trim()).filter(Boolean);
  }
}

function showActionSheetAsync(itemList) {
  return new Promise((resolve, reject) => {
    wx.showActionSheet({
      itemList,
      success: resolve,
      fail: reject
    });
  });
}

function showReportDescInput() {
  return new Promise((resolve) => {
    wx.showModal({
      title: '补充描述（可选）',
      editable: true,
      placeholderText: '请输入举报补充说明',
      success: (res) => {
        if (!res.confirm) {
          resolve(null);
          return;
        }
        resolve((res.content || '').trim());
      },
      fail: () => resolve('')
    });
  });
}
