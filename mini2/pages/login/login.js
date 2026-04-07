const api = require('../../api/index')
Page({
  onGetUserInfo(e) {
    const userInfo = e.detail.userInfo
    if (!userInfo) {
      wx.showToast({ title: '授权失败', icon: 'none' })
      return
    }
    api.login(userInfo).then(res => {
      wx.setStorageSync('token', res.data.token)
      wx.setStorageSync('userInfo', userInfo)
      wx.reLaunch({ url: '/pages/index/index' })
    })
  }
})
