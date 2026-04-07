const config = require('./config')

module.exports = function request({ url, method = 'GET', data = {} }) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: config.baseUrl + url,
      method,
      data,
      header: {
        'Authorization': wx.getStorageSync('token') || ''
      },
      success: resolve,
      fail: reject
    })
  })
}
