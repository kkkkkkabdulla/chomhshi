const api = require('../../api/index')
Page({
  data: {
    userInfo: {},
    myPosts: []
  },
  onShow() {
    const token = wx.getStorageSync('token')
    if (!token) {
      this.setData({ userInfo: {}, myPosts: [] })
      return
    }
    const userInfo = wx.getStorageSync('userInfo') || {}
    this.setData({ userInfo })
    api.getMyPosts().then(res => {
      this.setData({ myPosts: res.data })
    })
  },
  goDetail(e) {
    wx.navigateTo({
      url: `/pages/detail/detail?id=${e.currentTarget.dataset.id}`
    })
  },
  logout() {
    wx.clearStorageSync()
    wx.reLaunch({ url: '/pages/login/login' })
  }
})
