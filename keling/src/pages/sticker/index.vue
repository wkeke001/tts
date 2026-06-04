<template>
  <view class="page">
    <view class="container">
      <view class="input-section">
        <text class="input-label">输入文字，获取匹配的表情包</text>
        <textarea 
          v-model="inputText" 
          class="input-textarea" 
          placeholder="请输入文字，例如：你好、开心、难过..."
          placeholder-class="placeholder-text"
          maxlength="100"
        ></textarea>
        <view class="input-row">
          <text class="char-count">{{ inputText.length }}/100</text>
          <button 
            class="generate-btn" 
            @click="generateStickers"
            :disabled="!inputText.trim() || isLoading"
          >
            {{ isLoading ? '生成中...' : '获取推荐' }}
          </button>
        </view>
      </view>

      <view v-if="stickers.length > 0" class="result-section">
        <text class="result-title">推荐表情包 (共{{ stickers.length }}个)</text>
        <view class="sticker-grid">
          <view v-for="(sticker, index) in stickers" :key="index" class="sticker-item">
            <image 
              :src="sticker.url" 
              class="sticker-image" 
              mode="aspectFit"
              @longpress="saveSticker(sticker)"
              @error="onImageError(sticker, $event)"
              @load="onImageLoad(sticker, $event)"
            ></image>
            <text class="sticker-desc">{{ sticker.description }}</text>
            <button class="download-btn" @click="downloadSticker(sticker)">
              下载
            </button>
          </view>
        </view>
      </view>

      <view v-if="errorMessage" class="error-section">
        <text class="error-message">{{ errorMessage }}</text>
      </view>

      <view v-if="stickers.length === 0 && !isLoading && inputText.trim()" class="empty-section">
        <text class="empty-message">暂无匹配的表情包</text>
        <text class="empty-hint">试试其他关键词吧</text>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      inputText: '',
      stickers: [],
      isLoading: false,
      errorMessage: ''
    }
  },
  methods: {
    // 生成表情包推荐
    async generateStickers() {
      if (!this.inputText.trim()) {
        uni.showToast({
          title: '请输入文字',
          icon: 'none'
        })
        return
      }

      this.isLoading = true
      this.errorMessage = ''

      try {
        // 调用真实API接口
        const res = await uni.request({
          url: `/api/emojis?keyword=${encodeURIComponent(this.inputText.trim())}`,
          method: 'GET',
          timeout: 10000
        })

        if (res.statusCode === 200 && res.data.code === 200) {
          this.stickers = res.data.data.files.map(file => ({
            url: `/api/emojis/${encodeURIComponent(file.fileName)}`,
            description: file.fileName.replace('.gif', ''),
            fileName: file.fileName
          }))
        } else {
          this.errorMessage = '获取表情包失败，请稍后重试'
        }
      } catch (error) {
        this.errorMessage = '网络错误，请稍后重试'
        console.error('生成表情包失败:', error)
      } finally {
        this.isLoading = false
      }
    },

    // 下载表情包
    downloadSticker(sticker) {
      uni.showLoading({
        title: '下载中...'
      })

      const downloadUrl = `/api/emojis/${encodeURIComponent(sticker.fileName)}`

      uni.downloadFile({
        url: downloadUrl,
        success: (res) => {
          if (res.statusCode === 200) {
            uni.saveImageToPhotosAlbum({
              filePath: res.tempFilePath,
              success: () => {
                uni.hideLoading()
                uni.showToast({
                  title: '保存成功',
                  icon: 'success'
                })
              },
              fail: (err) => {
                uni.hideLoading()
                console.error('保存失败:', err)
                uni.showToast({
                  title: '保存失败，请重试',
                  icon: 'none'
                })
              }
            })
          } else {
            uni.hideLoading()
            uni.showToast({
              title: '下载失败',
              icon: 'none'
            })
          }
        },
        fail: (err) => {
          uni.hideLoading()
          console.error('下载失败:', err)
          uni.showToast({
            title: '下载失败，请重试',
            icon: 'none'
          })
        }
      })
    },

    // 保存表情包
    saveSticker(sticker) {
      uni.showToast({
        title: '长按保存功能开发中',
        icon: 'none'
      })
    },

    // 图片加载成功
    onImageLoad(sticker, event) {
      console.log('图片加载成功:', sticker.url)
    },

    // 图片加载失败
    onImageError(sticker, event) {
      console.error('图片加载失败:', sticker.url, event)
      uni.showToast({
        title: '图片加载失败',
        icon: 'none'
      })
    }
  }
}
</script>

<style>
.page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20rpx;
}

.container {
  max-width: 1200rpx;
  margin: 0 auto;
}

.input-section {
  background-color: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.input-label {
  display: block;
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.input-textarea {
  width: 100%;
  min-height: 160rpx;
  padding: 20rpx;
  border: 1rpx solid #e0e0e0;
  border-radius: 10rpx;
  font-size: 28rpx;
  color: #333;
  resize: none;
  box-sizing: border-box;
}

.placeholder-text {
  color: #999;
}

.input-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20rpx;
}

.char-count {
  font-size: 24rpx;
  color: #999;
}

.generate-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 30rpx;
  padding: 15rpx 40rpx;
  font-size: 28rpx;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
}

.generate-btn:hover {
  opacity: 0.9;
}

.generate-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.result-section {
  background-color: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.result-title {
  display: block;
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.sticker-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.sticker-item {
  background-color: #f9f9f9;
  border-radius: 15rpx;
  padding: 20rpx;
  text-align: center;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.sticker-image {
  width: 240rpx;
  height: 240rpx;
  margin-bottom: 15rpx;
  border-radius: 10rpx;
  background-color: #f0f0f0;
}

.sticker-desc {
  display: block;
  font-size: 24rpx;
  color: #333;
  margin-bottom: 15rpx;
}

.download-btn {
  background-color: #667eea;
  color: #fff;
  border: none;
  border-radius: 20rpx;
  padding: 10rpx 30rpx;
  font-size: 22rpx;
  cursor: pointer;
  transition: all 0.3s ease;
}

.download-btn:hover {
  background-color: #764ba2;
}

.error-section {
  background-color: #ffebee;
  border-radius: 15rpx;
  padding: 20rpx;
  margin-bottom: 30rpx;
  border-left: 4rpx solid #f44336;
}

.error-message {
  font-size: 26rpx;
  color: #d32f2f;
}

.empty-section {
  background-color: #f3e5f5;
  border-radius: 15rpx;
  padding: 40rpx;
  text-align: center;
  margin-bottom: 30rpx;
}

.empty-message {
  display: block;
  font-size: 28rpx;
  color: #666;
  margin-bottom: 10rpx;
}

.empty-hint {
  display: block;
  font-size: 24rpx;
  color: #999;
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .sticker-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media screen and (max-width: 480px) {
  .input-section,
  .result-section {
    padding: 20rpx;
  }

  .input-label,
  .result-title {
    font-size: 28rpx;
  }

  .input-textarea {
    min-height: 140rpx;
    font-size: 26rpx;
  }

  .generate-btn {
    font-size: 26rpx;
    padding: 12rpx 30rpx;
  }

  .sticker-image {
    width: 140rpx;
    height: 140rpx;
  }

  .sticker-desc {
    font-size: 22rpx;
  }

  .download-btn {
    font-size: 20rpx;
    padding: 8rpx 25rpx;
  }
}
</style>