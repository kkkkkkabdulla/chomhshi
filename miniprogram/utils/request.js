const { BASE_URL } = require('./config');

function getToken() {
  return wx.getStorageSync('token') || '';
}

function clearLoginAndGoLogin(msg = '登录已过期，请重新登录') {
  wx.removeStorageSync('token');
  wx.removeStorageSync('userInfo');
  wx.showToast({ title: msg, icon: 'none' });
  setTimeout(() => {
    wx.reLaunch({ url: '/pages/login/login' });
  }, 400);
}

function request({ url, method = 'GET', data = {}, header = {}, needAuth = true }) {
  return new Promise((resolve, reject) => {
    const finalHeader = {
      'Content-Type': 'application/json',
      ...header
    };

    if (needAuth) {
      const token = getToken();
      if (token) finalHeader.token = token;
    }

    wx.request({
      url: `${BASE_URL}${url}`,
      method,
      data,
      header: finalHeader,
      success: (res) => {
        const { statusCode, data: body } = res;

        if (statusCode >= 500) {
          wx.showToast({ title: '服务器异常', icon: 'none' });
          return reject(res);
        }

        if (!body || typeof body.code === 'undefined') {
          wx.showToast({ title: '响应格式异常', icon: 'none' });
          return reject(res);
        }

        if (body.code === 200) {
          return resolve(body);
        }

        if (body.code === 401) {
          clearLoginAndGoLogin();
          return reject(body);
        }

        wx.showToast({ title: body.msg || '请求失败', icon: 'none' });
        return reject(body);
      },
      fail: (err) => {
        wx.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
        reject(err);
      }
    });
  });
}

function uploadImage(filePath, needAuth = true) {
  return new Promise((resolve, reject) => {
    const header = {};
    if (needAuth) {
      const token = getToken();
      if (token) header.token = token;
    }

    wx.uploadFile({
      url: `${BASE_URL}/api/upload/image`,
      filePath,
      name: 'file',
      header,
      success: (res) => {
        let body = {};
        try {
          body = JSON.parse(res.data);
        } catch (e) {
          wx.showToast({ title: '上传响应解析失败', icon: 'none' });
          return reject(e);
        }

        if (body.code === 200) {
          resolve(body);
        } else if (body.code === 401) {
          clearLoginAndGoLogin();
          reject(body);
        } else {
          wx.showToast({ title: body.msg || '上传失败', icon: 'none' });
          reject(body);
        }
      },
      fail: (err) => {
        wx.showToast({ title: '上传失败', icon: 'none' });
        reject(err);
      }
    });
  });
}

module.exports = {
  request,
  uploadImage
};
