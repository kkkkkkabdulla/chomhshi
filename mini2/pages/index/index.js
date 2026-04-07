const api = require('../../api/index')
Page({
  data: {
    posts: [],
    categories: ['全部', '二手交易', '失物招领'],
    category: '全部',
  },
  onLoad() {
    this.getPosts()
  },
  getPosts(keyword = '', category = '全部') {
    api.getPosts({ keyword, category }).then(res => {
      this.setData({ posts: res.data })
    })
  },
  onCategoryChange(e) {
    const idx = e.detail.value
    const category = this.data.categories[idx]
    this.setData({ category })
    this.getPosts('', category)
  },
  onSearch(e) {
    const keyword = e.detail.value
    this.getPosts(keyword, this.data.category)
  },
  goDetail(e) {
    wx.navigateTo({
      url: `/pages/detail/detail?id=${e.currentTarget.dataset.id}`
    })
  }
})
