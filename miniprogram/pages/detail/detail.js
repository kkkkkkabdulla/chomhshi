const api = require('../../api/index');

function getCategoryLabel(post) {
  if (post.category) return post.category;
  if (post.type === 1) return '失物招领';
  if (post.type === 2) return '二手交易';
  if (post.type === 4) return '求助';
  if (post.type === 5) return '自由动态';
  return '其他';
}

Page({
  data: {
    id: null,
    isOwnerView: false,
    post: null,
    liked: false,
    likeAnimating: false,
    collected: false,
    collectAnimating: false,
    commentContent: '',
    commentList: [],
    imageList: [],
    categoryLabel: '',
    replyToId: null,
    replyToNickname: '',
  },

  onLoad(options) {
    const id = Number(options.id);
    const isOwnerView = String(options.owner || '') === '1';
    this.setData({ id: Number.isNaN(id) ? null : id, isOwnerView });
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
      this.loadCollected(),
      this.loadComments()
    ]);
  },

  async loadDetail() {
    try {
      const res = this.data.isOwnerView
        ? await api.getPostDetailForOwner(this.data.id)
        : await api.getPostDetail(this.data.id);
      const post = res.data || null;
      const imageList = parseImages(post && post.images).slice(0, 6);
      const categoryLabel = post ? getCategoryLabel(post) : '';
      this.setData({ post, imageList, imagePreviewList: imageList, categoryLabel });
    } catch (e) {}
  },

  async loadLiked() {
    try {
      const res = await api.isLiked(this.data.id);
      this.setData({ liked: !!(res.data && res.data.liked) });
    } catch (e) {}
  },

  async loadCollected() {
    try {
      const res = await api.isCollected(this.data.id);
      this.setData({ collected: !!(res.data && res.data.collected) });
    } catch (e) {}
  },

  async loadComments() {
    try {
      const res = await api.getCommentList(this.data.id, { page: 1, pageSize: 50 });
      var list = (res.data && res.data.list) || [];
      var userInfo = wx.getStorageSync('userInfo');
      var currentUserId = userInfo && userInfo.id;
      for (var i = 0; i < list.length; i++) {
        list[i].isOwner = (list[i].userId === currentUserId);
        list[i].createTimeAgo = formatTimeAgo(list[i].createTime);
      }
      this.setData({ commentList: list });
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

  async onToggleCollect() {
    try {
      const res = await api.toggleCollect(this.data.id);
      const post = this.data.post || {};
      post.collectCount = res.data.collectCount;
      const collected = !!res.data.collected;
      this.setData({
        collected,
        post,
        collectAnimating: collected
      });
      if (collected) {
        setTimeout(() => this.setData({ collectAnimating: false }), 450);
      }
    } catch (e) {}
  },

  onCommentInput(e) {
    this.setData({ commentContent: e.detail.value || '' });
  },

  onReplyComment(e) {
    var id = e.currentTarget.dataset.id;
    var nickname = e.currentTarget.dataset.nickname;
    var userId = e.currentTarget.dataset.userid;
    var userInfo = wx.getStorageSync('userInfo');
    var currentUserId = userInfo && userInfo.id;
    if (userId === currentUserId) return;
    this.setData({
      replyToId: id,
      replyToNickname: nickname || ('用户' + userId)
    });
  },

  onCancelReply() {
    this.setData({ replyToId: null, replyToNickname: '' });
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
        parentId: this.data.replyToId || 0
      });
      this.setData({ commentContent: '', replyToId: null, replyToNickname: '' });
      await Promise.allSettled([this.loadComments(), this.loadDetail()]);
      wx.showToast({ title: '评论成功', icon: 'success' });
    } catch (e) {}
  },

  onLongPressComment(e) {
    var isOwner = e.currentTarget.dataset.isowner;
    if (!isOwner) return;
    var commentId = e.currentTarget.dataset.id;
    if (!commentId) return;
    var that = this;
    wx.showModal({
      title: '确认删除',
      content: '删除后不可恢复，确定删除这条评论吗？',
      success: function(res) {
        if (!res.confirm) return;
        api.deleteComment(commentId).then(function() {
          Promise.allSettled([that.loadComments(), that.loadDetail()]);
          wx.showToast({ title: '已删除', icon: 'success' });
        }).catch(function() {});
      }
    });
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
  },

  previewImage(e) {
    const index = Number(e.currentTarget.dataset.index || 0);
    const urls = this.data.imageList || [];
    if (!urls.length) return;
    wx.previewImage({
      current: urls[index] || urls[0],
      urls
    });
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

function formatTimeAgo(timeStr) {
  if (!timeStr) return '';
  var date = new Date(timeStr);
  if (isNaN(date.getTime())) return timeStr;
  var now = new Date();
  var diff = now.getTime() - date.getTime();
  var seconds = Math.floor(diff / 1000);
  if (seconds < 60) return '刚刚';
  var minutes = Math.floor(seconds / 60);
  if (minutes < 60) return minutes + '分钟前';
  var hours = Math.floor(minutes / 60);
  if (hours < 24) return hours + '小时前';
  var days = Math.floor(hours / 24);
  if (days < 30) return days + '天前';
  var months = Math.floor(days / 30);
  if (months < 12) return months + '个月前';
  return Math.floor(months / 12) + '年前';
}

function showActionSheetAsync(itemList) {
  return new Promise(function(resolve, reject) {
    wx.showActionSheet({
      itemList,
      success: resolve,
      fail: reject
    });
  });
}

function showReportDescInput() {
  return new Promise(function(resolve) {
    wx.showModal({
      title: '补充描述（可选）',
      editable: true,
      placeholderText: '请输入举报补充说明',
      success: function(res) {
        if (!res.confirm) {
          resolve(null);
          return;
        }
        resolve((res.content || '').trim());
      },
      fail: function() { resolve(''); }
    });
  });
}
