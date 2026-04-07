const request = require('../utils/request')

module.exports = {
  getPosts(params) {
    return request({ url: '/posts', method: 'GET', data: params })
  },
  getPostDetail(id) {
    return request({ url: `/posts/${id}`, method: 'GET' })
  },
  publishPost(data) {
    return request({ url: '/posts', method: 'POST', data })
  },
  login(userInfo) {
    return request({ url: '/login', method: 'POST', data: userInfo })
  },
  getMyPosts() {
    return request({ url: '/my/posts', method: 'GET' })
  }
}
