const api = require('../../api/index')
Page({
  data: {
    post: {}
  },
  onLoad(options) {
    api.getPostDetail(options.id).then(res => {
      this.setData({ post: res.data })
    })
  },
  onShow() {
    // 评论功能可在此扩展
  },
  onComment(e) {
    const token = wx.getStorageSync('token')
    if (!token) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    // 评论逻辑
  }
})
