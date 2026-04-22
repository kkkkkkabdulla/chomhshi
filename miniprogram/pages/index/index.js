const api = require('../../api/index');

Page({
  data: {
    list: [],
    page: 1,
    pageSize: 10,
    keyword: '',
    refreshing: false,
    activeTab: '' ,// '' 表示不筛选，显示全部
    noticePopupVisible: false,
    noticePopupLoading: false,
    noticePopup: null,
    noticeList: []
  },

  noop() {},

  onShow() {
    this.loadList();
    this.loadNoticeData();
  },

  onPullDownRefresh() {
    this.setData({ refreshing: true, page: 1 }, async () => {
      await this.loadList();
      this.setData({ refreshing: false });
      wx.stopPullDownRefresh();
    });
  },

  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value || '' });
  },

  onSearch() {
    this.setData({ page: 1 }, () => this.loadList());
  },

  onTabClick(e) {
    const tab = e.currentTarget.dataset.tab || '';
    const nextTab = this.data.activeTab === tab ? '' : tab; // 再点一次取消筛选
    this.setData({ activeTab: nextTab, page: 1 }, () => this.loadList());
  },

  async loadList() {
    try {
      let type = null;
      if (this.data.activeTab === 'second') type = 2;
      if (this.data.activeTab === 'lost') type = 1;

      const category = mapCategory(this.data.activeTab);

      const req = {
        page: this.data.page,
        pageSize: this.data.pageSize,
        keyword: this.data.keyword
      };
      if (type !== null) req.type = type;
      if (category) req.category = category;

      const res = await api.getPostList(req);

      let list = ((res.data && res.data.list) || []).map((item) => {
        const imageList = parseImages(item.images).slice(0, 3);
        return {
          ...item,
          imageList,
          summary: pickSummary(item.description),
          liked: false,
          liking: false
        };
      });

      if (hasLoginToken() && list.length) {
        list = await fillLikedState(list);
      }

      this.setData({ list });
    } catch (e) {
      console.error('loadList error:', e);
      this.setData({ list: [] });
    }
  },

  async onToggleLike(e) {
    const id = Number(e.currentTarget.dataset.id);
    if (!id) return;

    const currentList = this.data.list || [];
    const target = currentList.find((item) => Number(item.id) === id);
    if (!target || target.liking) return;

    const markLoadingList = currentList.map((item) =>
      Number(item.id) === id ? { ...item, liking: true } : item
    );
    this.setData({ list: markLoadingList });

    try {
      const res = await api.toggleLike(id);
      const liked = !!(res.data && res.data.liked);
      const likeCount = Number((res.data && res.data.likeCount) || 0);

      const list = (this.data.list || []).map((item) => {
        if (Number(item.id) !== id) return item;
        return {
          ...item,
          liked,
          likeCount,
          liking: false
        };
      });

      this.setData({ list });
      wx.showToast({
        title: liked ? '已点赞' : '已取消点赞',
        icon: 'none',
        duration: 800
      });
    } catch (e) {
      const rollbackList = (this.data.list || []).map((item) =>
        Number(item.id) === id ? { ...item, liking: false } : item
      );
      this.setData({ list: rollbackList });
      // 401 等错误由 request.js 统一处理
    }
  },

  async loadNoticeData() {
    try {
      const noticeListRes = await api.getAnnouncementList({ page: 1, pageSize: 10 });
      const noticeList = (noticeListRes.data && noticeListRes.data.list) || [];
      const decorated = noticeList.map((item, index) => ({
        ...item,
        isLatest: index === 0,
        createdAtText: formatTime(item.createdAt)
      }));
      this.setData({ noticeList: decorated });
      const notice = noticeList.length ? noticeList[0] : null;
      if (!notice || !notice.id) return;
      const seenKey = `notice_seen_${notice.id}`;
      const seen = wx.getStorageSync(seenKey);
      if (!seen) {
        this.setData({ noticePopupVisible: true, noticePopup: notice });
      }
    } catch (e) {
      console.error('loadNoticeData error:', e);
    }
  },

  closeNoticePopup() {
    const notice = this.data.noticePopup;
    if (notice && notice.id) {
      wx.setStorageSync(`notice_seen_${notice.id}`, true);
    }
    this.setData({ noticePopupVisible: false, noticePopup: null });
  },

  goNoticeDetail(e) {
    const id = e.currentTarget.dataset.id;
    const item = (this.data.noticeList || []).find((it) => Number(it.id) === Number(id));
    if (!item) return;
    this.setData({ noticePopupVisible: true, noticePopup: item });
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/detail/detail?id=${id}` });
  }
});

function mapCategory(tab) {
  if (tab === 'free') return '随心贴';
  if (tab === 'help') return '互助';
  if (tab === 'notice') return '公告';
  return null;
}

function pickSummary(text) {
  if (!text) return '暂无描述';
  const t = String(text).trim();
  return t.length > 50 ? `${t.slice(0, 50)}...` : t;
}

function formatTime(val) {
  if (val === null || val === undefined || val === '') return '-';
  const raw = String(val).trim();
  if (!raw) return '-';
  if (/^\d+$/.test(raw)) {
    let num = Number(raw);
    if (raw.length <= 10) num *= 1000;
    const d = new Date(num);
    if (!Number.isFinite(num) || isNaN(d.getTime())) return raw;
    return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())} ${pad2(d.getHours())}:${pad2(d.getMinutes())}`;
  }
  return raw;
}

function pad2(n) {
  return n < 10 ? `0${n}` : String(n);
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

function hasLoginToken() {
  return !!wx.getStorageSync('token');
}

async function fillLikedState(list) {
  const jobs = list.map(async (item) => {
    try {
      const res = await api.isLiked(item.id);
      return {
        ...item,
        liked: !!(res.data && res.data.liked)
      };
    } catch (e) {
      return item;
    }
  });

  return Promise.all(jobs);
}
