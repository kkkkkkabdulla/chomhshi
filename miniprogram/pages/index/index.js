const api = require('../../api/index');

Page({
  data: {
    list: [],
    page: 1,
    pageSize: 10,
    keyword: '',
    refreshing: false,
    activeTab: '' // '' 表示不筛选，显示全部
  },

  onShow() {
    this.loadList();
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

      const res = await api.getPostList({
        page: this.data.page,
        pageSize: this.data.pageSize,
        keyword: this.data.keyword,
        type,
        category
      });

      const list = ((res.data && res.data.list) || []).map((item) => {
        const imageList = parseImages(item.images).slice(0, 3);
        return {
          ...item,
          imageList,
          summary: pickSummary(item.description)
        };
      });
      this.setData({ list });
    } catch (e) {
      console.error('loadList error:', e);
      this.setData({ list: [] });
    }
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
