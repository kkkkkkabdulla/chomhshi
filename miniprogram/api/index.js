const { request, uploadImage } = require('../utils/request');

function wxLogin(data) {
  return request({
    url: '/api/user/wxLogin',
    method: 'POST',
    data,
    needAuth: false
  });
}

function verifyToken() {
  return request({ url: '/api/user/verifyToken' });
}

function getUserInfo() {
  return request({ url: '/api/user/info' });
}

function updateUserInfo(data) {
  return request({
    url: '/api/user/updateInfo',
    method: 'PUT',
    data
  });
}

function publishPost(data) {
  return request({
    url: '/api/post/publish',
    method: 'POST',
    data
  });
}

function getPostList(params = {}) {
  const query = toQuery(params);
  return request({
    url: `/api/post/list${query}`,
    needAuth: false
  });
}

function getPostDetail(id) {
  return request({
    url: `/api/post/detail/${id}`,
    needAuth: false
  });
}

function getPostDetailForOwner(id) {
  return request({
    url: `/api/post/detailForOwner/${id}`
  });
}

function getMyPostList(params = {}) {
  const query = toQuery(params);
  return request({ url: `/api/post/myList${query}` });
}

function updatePost(id, data) {
  return request({
    url: `/api/post/update/${id}`,
    method: 'PUT',
    data
  });
}

function deletePost(id) {
  return request({
    url: `/api/post/delete/${id}`,
    method: 'DELETE'
  });
}

function toggleLike(id) {
  return request({
    url: `/api/post/like/${id}`,
    method: 'POST'
  });
}

function isLiked(id) {
  return request({ url: `/api/post/isLiked/${id}` });
}

function toggleCollect(id) {
  return request({
    url: `/api/post/collect/${id}`,
    method: 'POST'
  });
}

function isCollected(id) {
  return request({ url: `/api/post/isCollected/${id}` });
}

function addComment(data) {
  return request({
    url: '/api/comment/add',
    method: 'POST',
    data
  });
}

function getCommentList(postId, params = {}) {
  const query = toQuery(params);
  return request({
    url: `/api/comment/list/${postId}${query}`,
    needAuth: false
  });
}

function deleteComment(id) {
  return request({
    url: `/api/comment/delete/${id}`,
    method: 'DELETE'
  });
}

function addReport(data) {
  return request({
    url: '/api/report/add',
    method: 'POST',
    data
  });
}

function uploadPostImage(filePath) {
  return uploadImage(filePath, true);
}

function getLatestAnnouncement() {
  return request({ url: '/api/announcement/latest', needAuth: false });
}

function getAnnouncementList(params = {}) {
  const query = toQuery(params);
  return request({ url: `/api/announcement/list${query}`, needAuth: false });
}

function getMyCollects(params = {}) {
  const query = toQuery(params);
  return request({ url: `/api/post/myCollects${query}` });
}

function toQuery(obj = {}) {
  const arr = Object.keys(obj)
    .filter((k) => obj[k] !== undefined && obj[k] !== null && obj[k] !== '')
    .map((k) => `${encodeURIComponent(k)}=${encodeURIComponent(obj[k])}`);
  return arr.length ? `?${arr.join('&')}` : '';
}

module.exports = {
  wxLogin,
  verifyToken,
  getUserInfo,
  updateUserInfo,
  publishPost,
  getPostList,
  getPostDetail,
  getPostDetailForOwner,
  getMyPostList,
  getMyCollects,
  updatePost,
  deletePost,
  toggleLike,
  isLiked,
  toggleCollect,
  isCollected,
  addComment,
  getCommentList,
  deleteComment,
  addReport,
  uploadPostImage,
  getLatestAnnouncement,
  getAnnouncementList
};
