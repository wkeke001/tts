<template>
  <view class="page">
    <view class="header">
      <view class="header-top-row">
        <view class="user-btn" @click="handleUserClick">
          <template v-if="isLoggedIn && userInfo.avatar">
            <image class="user-avatar-img" :src="userInfo.avatar" mode="aspectFill"></image>
          </template>
          <template v-else-if="isLoggedIn && userInfo.nickname">
            <view class="user-avatar-char">{{ userInfo.nickname.charAt(0).toUpperCase() }}</view>
          </template>
          <template v-else>
            <text class="login-link">登录</text>
          </template>
        </view>
      </view>
      <text class="app-title">一二布布语音视频工坊</text>
      <text class="app-subtitle">用一二布布的声音，说你想说的话</text>
    </view>

    <view class="grid">
      <view class="card card-primary" @click="navigateTo('/pages/tts/index')">
        <image class="card-icon-img" src="/static/audio.gif" mode="aspectFit"></image>
        <text class="card-title">一二布布语音合成</text>
        <text class="card-desc">输入文字，一键生成一二布布的角色语音</text>
      </view>

      <view class="card" @click="navigateTo('/pages/sticker/index')">
        <image class="card-icon-img" src="/static/emoj.gif" mode="aspectFit"></image>
        <text class="card-title">表情包自动推荐</text>
        <text class="card-desc">根据文字语义，推荐匹配的一二布布表情包</text>
      </view>

      <view class="card" @click="navigateTo('/pages/sound/index')">
        <image class="card-icon-img" src="/static/sound.gif" mode="aspectFit"></image>
        <text class="card-title">音效自动推荐</text>
        <text class="card-desc">输入文字，推荐匹配的一二布布音效</text>
      </view>

      <view class="card card-primary" @click="navigateTo('/pages/video/index')">
        <image class="card-icon-img" src="/static/video.gif" mode="aspectFit"></image>
        <text class="card-title">和布布一二聊天</text>
        <text class="card-desc">与布布一二进行实时对话，享受智能交互体验</text>
      </view>

      <view class="card card-primary" @click="navigateTo('/pages/sing/index')">
        <image class="card-icon-img" src="/static/sing.gif" mode="aspectFit"></image>
        <text class="card-title">一二布布唱歌</text>
        <text class="card-desc">输入歌词和风格，由一二布布的声音来演唱歌曲</text>
      </view>

      <view class="card card-primary" @click="navigateTo('/pages/scene/index')">
        <image class="card-icon-img" src="/static/scene.png" mode="aspectFit"></image>
        <text class="card-title">小熊场景生成</text>
        <text class="card-desc">输入文字描述，一键生成一二布布风格的场景图</text>
      </view>

      <view class="card card-disabled" @click="showComingSoon('故事演绎')">
        <view class="badge-coming">即将上线</view>
        <image class="card-icon-img" src="/static/story.gif" mode="aspectFit"></image>
        <text class="card-title">故事演绎</text>
        <text class="card-desc">上传剧本片段，用一二布布的声音和表情包重新演绎故事</text>
      </view>

      <view class="card card-disabled" @click="showComingSoon('更多实用工具')">
        <view class="badge-coming">即将上线</view>
        <image class="card-icon-img" src="/static/tools.gif" mode="aspectFit"></image>
        <text class="card-title">更多实用工具</text>
        <text class="card-desc">水印去除、白色背景去除，在线图片处理</text>
      </view>
    </view>

    <view class="about-entry" @click="navigateTo('/pages/about/index')">
      <text class="about-text">关于我们</text>
      <text class="about-arrow">›</text>
    </view>

    <view class="footer">
      <text class="footer-text">© 一二布布语音视频工坊 2026</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      isLoggedIn: false,
      userInfo: {
        nickname: '',
        avatar: ''
      }
    }
  },
  onLoad() {
    this.checkLoginStatus()
  },
  methods: {
    // 检查登录状态
    checkLoginStatus() {
      try {
        const token = uni.getStorageSync('box2audio_token')
        const user = uni.getStorageSync('box2audio_user')
        if (token && user) {
          this.isLoggedIn = true
          this.userInfo = JSON.parse(user)
        }
      } catch (e) {
        console.log('获取登录状态失败', e)
      }
    },
    // 处理用户点击
    handleUserClick() {
      if (this.isLoggedIn) {
        uni.navigateTo({
          url: '/pages/profile/index'
        })
      } else {
        uni.navigateTo({
          url: '/pages/login/index'
        })
      }
    },
    // 页面跳转
    navigateTo(url) {
      uni.navigateTo({
        url: url,
        fail: () => {
          uni.showToast({
            title: '页面开发中，敬请期待~',
            icon: 'none'
          })
        }
      })
    },
    // 显示即将上线提示
    showComingSoon(feature) {
      uni.showToast({
        title: `${feature}即将上线，敬请期待~`,
        icon: 'none',
        duration: 2000
      })
    }
  }
}
</script>

<style>
.page {
  max-width: 1500rpx;
  margin: 0 auto;
  padding: 0 20rpx;
  min-height: 100vh;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.header {
  padding: 40rpx 30rpx 30rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 0 0 30rpx 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(102, 126, 234, 0.3);
}

.header-top-row {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20rpx;
}

.user-btn {
  padding: 10rpx;
}

.login-link {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.2);
  padding: 10rpx 24rpx;
  border-radius: 30rpx;
}

.user-avatar-img {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  border: 2rpx solid rgba(255, 255, 255, 0.5);
}

.user-avatar-char {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  font-size: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid rgba(255, 255, 255, 0.5);
}

.app-title {
  display: block;
  font-size: 44rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 12rpx;
  text-align: center;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
}

.app-subtitle {
  display: block;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.85);
  text-align: center;
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
  padding: 30rpx;
}

.card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx 20rpx 30rpx 100rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  position: relative;
  display: flex;
  flex-direction: column;
  min-height: 160rpx;
  transition: transform 0.2s, box-shadow 0.2s;
}

.card:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

.card-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.card-primary .card-title,
.card-primary .card-desc {
  color: #fff;
}

.card-disabled {
  opacity: 0.7;
  background: #f8f9fa;
}

.card-disabled .card-title,
.card-disabled .card-desc {
  color: #999;
}

.card-icon-img {
  position: absolute;
  top: 30rpx;
  left: 20rpx;
  width: 70rpx;
  height: 70rpx;
  border-radius: 16rpx;
}

.card-title {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  margin-bottom: 12rpx;
  color: #333;
  line-height: 1.4;
}

.card-desc {
  display: block;
  font-size: 22rpx;
  color: #666;
  line-height: 1.5;
}

.badge-coming {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  color: #fff;
  font-size: 18rpx;
  padding: 6rpx 14rpx;
  border-radius: 20rpx;
  font-weight: 500;
  box-shadow: 0 2rpx 8rpx rgba(255, 107, 107, 0.3);
}

.about-entry {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 40rpx;
  background: #fff;
  margin: 0 30rpx 20rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
}

.about-text {
  font-size: 30rpx;
  color: #333;
  font-weight: 500;
}

.about-arrow {
  font-size: 40rpx;
  color: #999;
}

.footer {
  padding: 40rpx 0;
  text-align: center;
  margin-top: auto;
}

.footer-text {
  color: #999;
  font-size: 24rpx;
}

/* 微信小程序适配 */
#ifdef MP-WEIXIN
.card {
  box-shadow: none;
  border: 1rpx solid rgba(0, 0, 0, 0.05);
}

.card-primary {
  border: none;
}
#endif

/* 移动端适配 - 小屏幕 */
@media screen and (max-width: 375px) {
  .header {
    padding: 30rpx 20rpx 25rpx;
  }
  
  .app-title {
    font-size: 38rpx;
  }
  
  .app-subtitle {
    font-size: 24rpx;
  }
  
  .grid {
    padding: 20rpx;
    gap: 15rpx;
  }
  
  .card {
    padding: 25rpx 15rpx 25rpx 90rpx;
    min-height: 150rpx;
  }
  
  .card-icon-img {
    width: 60rpx;
    height: 60rpx;
    top: 25rpx;
    left: 15rpx;
  }
  
  .card-title {
    font-size: 26rpx;
  }
  
  .card-desc {
    font-size: 20rpx;
  }
  
  .about-entry {
    margin: 0 20rpx 20rpx;
    padding: 25rpx 30rpx;
  }
}

/* iPhone X 及以上机型安全区域适配 */
.safe-area-bottom {
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
