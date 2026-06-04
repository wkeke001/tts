<template>
  <view class="page">
    <view class="header">
      <view class="header-left" @click="goBack">
        <text class="back-btn">←</text>
      </view>
      <text class="header-title">语音表情包对话</text>
      <view class="header-right">
        <image :class="['avatar-btn', selectedType === 'yier' ? 'avatar-selected' : '']" src="https://audio.box2ai.com/static/yier.png" mode="aspectFit" @click="switchChat('yier')"></image>
        <image :class="['avatar-btn', selectedType === 'bubu' ? 'avatar-selected' : '']" src="https://audio.box2ai.com/static/bubu.png" mode="aspectFit" @click="switchChat('bubu')"></image>
      </view>
    </view>

    <view class="chat-container">
      <view class="message-list" ref="messageList">
        <!-- 系统消息 -->
        <view class="system-message">
          <text class="system-text">对话已开始，每发送一条消息，对方会回复语音</text>
        </view>

        <!-- 消息列表 -->
        <view v-for="(message, index) in messages" :key="index" :class="['message-item', message.type === 'user' ? 'user-message' : 'bot-message']">
          <image v-if="message.type === 'bot'" class="avatar" :src="selectedType === 'yier' ? 'https://audio.box2ai.com/static/bubu.png' : 'https://audio.box2ai.com/static/yier.png'" mode="aspectFit"></image>
          <view :class="['message-bubble', message.type === 'user' ? 'user-bubble' : 'bot-bubble']">
            <text v-if="message.type === 'user'" class="message-text">{{ message.content }}</text>
            <view v-else class="voice-message">
              <view class="voice-wave">
                <view v-for="i in 5" :key="i" :class="['voice-bar', message.isPlaying ? 'playing' : '']" :style="{ animationDelay: i * 0.1 + 's' }"></view>
              </view>
              <button class="voice-play-btn" @click="toggleVoicePlay(index)">
                {{ message.isPlaying ? '暂停' : '播放' }}
              </button>
            </view>
          </view>
          <image v-if="message.type === 'user'" class="avatar" :src="selectedType === 'yier' ? 'https://audio.box2ai.com/static/yier.png' : 'https://audio.box2ai.com/static/bubu.png'" mode="aspectFit"></image>
        </view>

        <!-- 加载动画 -->
        <view v-if="isLoading" class="loading-message">
          <text class="loading-text">对方正在输入...</text>
        </view>
      </view>

      <!-- 输入区域 -->
      <view class="input-container">
        <input
          v-model="inputMessage"
          class="input-field"
          placeholder="请输入消息..."
          @keyup.enter="sendMessage"
        />
        <button class="send-btn" @click="sendMessage" :disabled="!inputMessage.trim()">
          发送
        </button>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      messages: [],
      inputMessage: '',
      isLoading: false,
      audioElements: [],
      selectedType: 'yier' // 默认选中白熊
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    switchChat(type) {
      this.selectedType = type
    },
    sendMessage() {
      if (!this.inputMessage.trim()) return

      // 添加用户消息
      this.messages.push({
        type: 'user',
        content: this.inputMessage
      })

      // 清空输入框
      const message = this.inputMessage
      this.inputMessage = ''

      // 滚动到底部
      this.scrollToBottom()

      // 显示加载状态
      this.isLoading = true

      // 调用语音接口获取回复
      this.getCompletionVoice(message)
    },
    async getCompletionVoice(prompt) {
      try {
        console.log('发送语音请求:', prompt, '类型:', this.selectedType);
        const response = await uni.request({
          url: '/api/tts/synthesis-from-completion',
          method: 'POST',
          header: {
            'Content-Type': 'application/json'
          },
          data: {
            prompt: prompt,
            type: this.selectedType
          },
          responseType: 'arraybuffer'
        });

        console.log('语音请求响应:', response);
        if (response.statusCode === 200) {
          // 二进制流转base64，生成可播放的音频地址
          const base64Audio = uni.arrayBufferToBase64(response.data);
          const audioUrl = `data:audio/wav;base64,${base64Audio}`;

          // 添加机器人语音回复
          this.messages.push({
            type: 'bot',
            content: '',
            audioUrl: audioUrl,
            isPlaying: false
          });

          // 滚动到底部
          this.scrollToBottom();
        } else {
          console.error('获取语音失败，状态码:', response.statusCode);
          throw new Error(`获取语音失败，状态码: ${response.statusCode}`);
        }
      } catch (error) {
        console.error('获取语音失败', error);
        // 添加错误消息
        this.messages.push({
          type: 'bot',
          content: `语音生成失败: ${error.message || '未知错误'}`
        });
        // 滚动到底部
        this.scrollToBottom();
      } finally {
        this.isLoading = false;
      }
    },
    toggleVoicePlay(index) {
      const message = this.messages[index];
      if (!message.audioUrl) return;

      // 停止其他正在播放的语音
      this.messages.forEach((msg, i) => {
        if (i !== index && msg.isPlaying) {
          this.stopVoice(i);
        }
      });

      if (message.isPlaying) {
        this.stopVoice(index);
      } else {
        this.playVoice(index);
      }
    },
    playVoice(index) {
      const message = this.messages[index];
      if (!message.audioUrl) return;

      // 创建音频元素
      if (!this.audioElements[index]) {
        this.audioElements[index] = new Audio(message.audioUrl);
        this.audioElements[index].onended = () => {
          message.isPlaying = false;
          this.$forceUpdate();
        };
        this.audioElements[index].onerror = () => {
          message.isPlaying = false;
          this.$forceUpdate();
        };
      }

      // 播放语音
      this.audioElements[index].play()
        .then(() => {
          message.isPlaying = true;
          this.$forceUpdate();
        })
        .catch(error => {
          console.error('播放失败', error);
          message.isPlaying = false;
          this.$forceUpdate();
        });
    },
    stopVoice(index) {
      const message = this.messages[index];
      if (this.audioElements[index]) {
        this.audioElements[index].pause();
        this.audioElements[index].currentTime = 0;
      }
      message.isPlaying = false;
      this.$forceUpdate();
    },
    scrollToBottom() {
      setTimeout(() => {
        if (this.$refs.messageList) {
          this.$refs.messageList.scrollTop = this.$refs.messageList.scrollHeight;
        }
      }, 100);
    }
  },
  beforeUnmount() {
    // 清理音频元素
    this.audioElements.forEach(audio => {
      if (audio) {
        audio.pause();
        audio.currentTime = 0;
      }
    });
  }
}
</script>

<style>
.page {
  min-height: 100vh;
  background: #f8f8f8;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx 20rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.header-left {
  width: 60rpx;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.avatar-btn {
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
  border: 2rpx solid rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: transform 0.2s;
}

.avatar-btn:active {
  transform: scale(0.95);
}

.avatar-selected {
  border-color: #fff;
  box-shadow: 0 0 0 4rpx rgba(255, 255, 255, 0.5);
  transform: scale(1.05);
}

.back-btn {
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
}

.header-title {
  font-size: 32rpx;
  font-weight: 600;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20rpx;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20rpx;
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
}

.system-message {
  text-align: center;
  margin: 20rpx 0;
}

.system-text {
  font-size: 22rpx;
  color: #999;
  background: #f5f5f5;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  display: inline-block;
}

.message-item {
  display: flex;
  margin-bottom: 30rpx;
  align-items: flex-end;
}

.user-message {
  flex-direction: row-reverse;
}

.avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  margin: 0 15rpx;
}

.message-bubble {
  max-width: 70%;
  padding: 20rpx;
  border-radius: 20rpx;
  position: relative;
}

.user-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-bottom-right-radius: 5rpx;
}

.bot-bubble {
  background: #f0f0f0;
  color: #333;
  border-bottom-left-radius: 5rpx;
}

.message-text {
  font-size: 28rpx;
  line-height: 1.5;
}

.voice-message {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 10rpx 0;
}

.voice-wave {
  display: flex;
  align-items: center;
  gap: 6rpx;
  height: 60rpx;
}

.voice-bar {
  width: 8rpx;
  background: #667eea;
  border-radius: 4rpx;
  height: 20rpx;
  transition: height 0.3s;
}

.voice-bar.playing {
  animation: voice-wave 1s infinite ease-in-out;
}

@keyframes voice-wave {
  0%, 100% {
    height: 20rpx;
  }
  50% {
    height: 60rpx;
  }
}

.voice-play-btn {
  padding: 10rpx 20rpx;
  background: #667eea;
  color: #fff;
  border: none;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: 500;
}

.loading-message {
  display: flex;
  justify-content: flex-start;
  margin: 20rpx 0;
  padding-left: 90rpx;
}

.loading-text {
  font-size: 24rpx;
  color: #999;
  background: #f5f5f5;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
}

.input-container {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 20rpx;
  padding: 10rpx;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.input-field {
  flex: 1;
  height: 80rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
  border: none;
  outline: none;
  background: #f5f5f5;
  border-radius: 40rpx;
  margin-right: 15rpx;
}

.send-btn {
  width: 120rpx;
  height: 80rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
}

.send-btn[disabled] {
  opacity: 0.6;
}

/* 滚动条样式 */
.message-list::-webkit-scrollbar {
  width: 4rpx;
}

.message-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 2rpx;
}

.message-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2rpx;
}

.message-list::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}

/* 移动端适配 - 小屏幕 */
@media screen and (max-width: 375px) {
  .header {
    padding: 25rpx 20rpx;
  }
  
  .header-title {
    font-size: 28rpx;
  }
  
  .message-list {
    padding: 15rpx;
  }
  
  .message-bubble {
    max-width: 75%;
    padding: 16rpx;
  }
  
  .message-text {
    font-size: 26rpx;
  }
  
  .input-field {
    height: 70rpx;
    font-size: 26rpx;
  }
  
  .send-btn {
    width: 100rpx;
    height: 70rpx;
    font-size: 26rpx;
  }
}
</style>