const api = require('../../api/index')
Page({
  data: {
    categories: ['二手交易', '失物招领'],
    category: '二手交易',
    title: '',
    desc: '',
    price: '',
    images: []
  },
  onCategoryChange(e) {
    const idx = e.detail.value
    const category = this.data.categories[idx]
    this.setData({ category })
  },
  onInputTitle(e) { this.setData({ title: e.detail.value }) },
  onInputDesc(e) { this.setData({ desc: e.detail.value }) },
  onInputPrice(e) { this.setData({ price: e.detail.value }) },
  chooseImage() {
    wx.chooseImage({
      count: 6,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: res => {
        this.setData({ images: this.data.images.concat(res.tempFilePaths) })
      }
    })
  },
  onSubmit() {
    if (!this.data.title || !this.data.desc) {
      wx.showToast({ title: '请填写标题和描述', icon: 'none' })
      return
    }
    // 登录校验
    const token = wx.getStorageSync('token')
    if (!token) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    // 模拟图片上传，实际应为后端上传接口
    const uploadImages = this.data.images.map(img => Promise.resolve({ url: img }))
    Promise.all(uploadImages).then(imgs => {
      const data = {
        type: this.data.category === '二手交易' ? 'second' : 'lost',
        title: this.data.title,
        desc: this.data.desc,
        price: this.data.category === '二手交易' ? this.data.price : undefined,
        images: imgs.map(i => i.url)
      }
      api.publishPost(data).then(() => {
        wx.showToast({ title: '发布成功' })
        wx.navigateBack()
      })
    })
  }
})
