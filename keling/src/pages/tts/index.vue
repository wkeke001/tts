<template>
  <view class="page">
    <view class="section hero">
      <text class="hero-title">一二布布语音合成</text>
      <text class="hero-desc">输入文字，用一二布布的声音说出来</text>
    </view>

    <view class="section">
      <text class="section-title">输入文字</text>
      <textarea 
        v-model="inputText" 
        class="input-text" 
        :maxlength="500" 
        placeholder="请输入要说的话（最多500字）"
        @input="handleInput"
      ></textarea>
      <text class="char-count">{{ inputText.length }}/500</text>
    </view>

    <view class="section">
      <text class="section-title">选择角色</text>
      <view class="voice-selector">
        <view 
          :class="['voice-chip', { active: selectedVoice === 'bubu' }]" 
          @click="selectVoice('bubu')"
        >
          <image class="voice-avatar" src="https://audio.box2ai.com/static/bubu.png" mode="aspectFit"></image>
          <text class="voice-name">布布</text>
        </view>
        <view 
          :class="['voice-chip', { active: selectedVoice === 'yier' }]" 
          @click="selectVoice('yier')"
        >
          <image class="voice-avatar" src="https://audio.box2ai.com/static/yier.png" mode="aspectFit"></image>
          <text class="voice-name">一二</text>
        </view>
        <view class="voice-chip disabled">
          <image class="voice-avatar" src="https://audio.box2ai.com/static/unknow.png" mode="aspectFit"></image>
          <text class="voice-name">自定义</text>
          <text class="voice-disabled-hint">维护中</text>
        </view>
      </view>
    </view>

    <view class="section">
      <text class="section-title">语速: {{ speed.toFixed(1) }}x</text>
      <slider 
        :value="speed" 
        :min="0.5" 
        :max="2.0" 
        :step="0.1" 
        @change="handleSpeedChange"
        activeColor="#764ba2"
        backgroundColor="#e9e9e9"
        block-size="28"
      ></slider>
    </view>

    <view class="section">
      <button class="generate-btn" @click="generateAudio" :disabled="!inputText || isGenerating">
        {{ isGenerating ? '生成中...' : '生成语音' }}
      </button>
    </view>

    <view v-if="audioUrl" class="section audio-result">
      <text class="section-title">生成结果</text>
      <view class="audio-player">
        <view class="audio-info">
          <text class="audio-name">{{ selectedVoice === 'bubu' ? '布布' : '一二' }}的语音</text>
          <text class="audio-length">{{ audioDuration || '0:00' }}</text>
        </view>
        <view class="audio-controls">
          <button class="play-btn" @click="togglePlay">
            {{ isPlaying ? '暂停' : '播放' }}
          </button>
          <button class="download-btn" @click="downloadHandle">
            下载
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      inputText: '',
      selectedVoice: 'bubu',
      speed: 1.0,
      isGenerating: false,
      audioUrl: '',
      isPlaying: false,
      audioDuration: '0:00',
      audioElement: null,
      downloadHandle: null
    }
  },
  methods: {
    handleInput(e) {
      this.inputText = e.detail.value
    },
    selectVoice(voice) {
      this.selectedVoice = voice
    },
    handleSpeedChange(e) {
      this.speed = e.detail.value
    },
    async generateAudio() {
      if (!this.inputText.trim()) {
        uni.showToast({
          title: '请输入文字',
          icon: 'none'
        })
        return
      }

      this.isGenerating = true
      
      try {
        // 统一的语音生成方法
        const result = await this.generateVoice(this.selectedVoice, this.inputText)
        
        if (result.success) {
          this.audioUrl = result.audioUrl
          this.downloadHandle = result.downloadAudio
          this.audioDuration = result.duration || '0:00'
          uni.showToast({
            title: '生成成功',
            icon: 'success'
          })
        } else {
          throw new Error(result.error || '生成失败')
        }
      } catch (error) {
        uni.showToast({
          title: '生成失败，请重试',
          icon: 'none'
        })
        console.error('生成语音失败', error)
      } finally {
        this.isGenerating = false
      }
    },
    // 根据WAV文件大小估算音频时长
    // 假设：16kHz采样率、16位深度、单声道
    // 每秒数据量 = 16000 * 16 * 1 / 8 = 32000 字节
    // 时长(秒) = (文件大小 - 44字节WAV头) / 32000
    estimateDurationFromWavSize(arrayBuffer) {
      const fileSize = arrayBuffer.byteLength
      const wavHeaderSize = 44
      const bytesPerSecond = 32000 // 16kHz * 16bit * 1channel / 8
      
      if (fileSize <= wavHeaderSize) {
        return '0:00'
      }
      
      const durationSeconds = Math.ceil((fileSize - wavHeaderSize) / bytesPerSecond)
      const minutes = Math.floor(durationSeconds / 60)
      const seconds = durationSeconds % 60
      
      return `${minutes}:${seconds.toString().padStart(2, '0')}`
    },

    generateVoice(voice, text) {
      return new Promise((resolve, reject) => {
        const fileName = `output_${Date.now()}.wav`;
        const url = '/api/tts/synthesis';

        const requestBody = {
          text: text,
          voiceId: voice === 'bubu' ? 'bubu' : 'yier',
          fileName: fileName
        };
        
        // ✅ 关键：必须设置 responseType: 'arraybuffer' 接收音频二进制流
        uni.request({
          url: url,
          method: 'POST',
          header: {
            'Content-Type': 'application/json'
          },
          data: requestBody,
          responseType: 'arraybuffer', // 🔥 核心配置
          success: (res) => {
            console.log('API响应:', res);
            if (res.statusCode === 200) {
              try {
                // 1. 二进制流转base64，生成**可播放的音频地址**
                const base64Audio = uni.arrayBufferToBase64(res.data);
                const audioUrl = `data:audio/wav;base64,${base64Audio}`;

                // 2. 根据音频文件大小估算时长
                const estimatedDuration = this.estimateDurationFromWavSize(res.data);

                // 3. 生成**下载方法**（点击即可保存wav文件）
                const downloadAudio = () => {
                  // H5端下载逻辑（uni-app/H5通用）
                  const a = document.createElement('a');
                  a.href = audioUrl;
                  a.download = fileName; // 下载文件名
                  document.body.appendChild(a);
                  a.click();
                  document.body.removeChild(a);
                };

                // 返回：播放地址 + 下载方法 + 估算时长
                resolve({
                  success: true,
                  audioUrl,       // 用于播放
                  downloadAudio,   // 调用即可下载
                  duration: estimatedDuration // 估算的音频时长
                });
              } catch (error) {
                console.error('处理音频失败:', error);
                reject(error);
              }
            } else {
              reject(new Error(`请求失败: ${res.statusCode}`));
            }
          },
          fail: (error) => {
            console.error('请求失败:', error);
            reject(error);
          }
        });
      });
    },

    togglePlay() {
      if (!this.audioUrl) return
      
      if (this.isPlaying) {
        this.pauseAudio()
      } else {
        this.playAudio()
      }
    },
    playAudio() {
      if (!this.audioElement) {
        this.audioElement = new Audio(this.audioUrl)
        this.audioElement.onended = () => {
          this.isPlaying = false
        }
        this.audioElement.onerror = (error) => {
          console.error('播放失败', error)
          uni.showToast({
            title: '播放失败，请重试',
            icon: 'none'
          })
          this.isPlaying = false
        }
      } else {
        this.audioElement.src = this.audioUrl
      }
      
      this.audioElement.play()
        .then(() => {
          this.isPlaying = true
        })
        .catch(error => {
          console.error('播放失败', error)
          uni.showToast({
            title: '播放失败，请重试',
            icon: 'none'
          })
          this.isPlaying = false
        })
    },
    pauseAudio() {
      if (this.audioElement) {
        this.audioElement.pause()
      }
      this.isPlaying = false
    }
  }
}
</script>

<style>
.page {
  min-height: 100vh;
  background: #f8f8f8;
  padding: 20rpx;
}

.section {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.hero {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx 30rpx;
  text-align: center;
}

.hero-title {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 12rpx;
}

.hero-desc {
  display: block;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
}

.section-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
}

.input-text {
  width: 100%;
  min-height: 200rpx;
  padding: 20rpx;
  border: 2rpx solid #e9e9e9;
  border-radius: 12rpx;
  font-size: 28rpx;
  line-height: 1.6;
  background: #fafafa;
}

.char-count {
  display: block;
  text-align: right;
  font-size: 24rpx;
  color: #999;
  margin-top: 10rpx;
}

.voice-selector {
  display: flex;
  gap: 20rpx;
  flex-wrap: wrap;
}

.voice-chip {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 30rpx;
  border-radius: 16rpx;
  background: #f5f5f5;
  border: 2rpx solid transparent;
  transition: all 0.3s;
  position: relative;
  min-width: 140rpx;
}

.voice-chip.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #764ba2;
}

.voice-chip.active .voice-name {
  color: #fff;
}

.voice-chip.disabled {
  opacity: 0.6;
}

.voice-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-bottom: 12rpx;
  background: #fff;
}

.voice-name {
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
}

.voice-disabled-hint {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  font-size: 20rpx;
  color: #ff6b6b;
  background: #fff;
  padding: 4rpx 10rpx;
  border-radius: 10rpx;
}

.generate-btn {
  width: 100%;
  height: 90rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 12rpx;
  font-size: 32rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.3);
}

.generate-btn[disabled] {
  opacity: 0.6;
}

.audio-result {
  animation: fadeIn 0.3s ease-in;
}

.audio-player {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx;
  background: #f8f8f8;
  border-radius: 12rpx;
}

.audio-info {
  flex: 1;
}

.audio-name {
  display: block;
  font-size: 28rpx;
  font-weight: 500;
  color: #333;
  margin-bottom: 8rpx;
}

.audio-length {
  font-size: 24rpx;
  color: #999;
}

.audio-controls {
  display: flex;
  gap: 12rpx;
}

.play-btn {
  padding: 16rpx 32rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 8rpx;
  font-size: 24rpx;
  font-weight: 500;
}

.download-btn {
  padding: 16rpx 32rpx;
  background: #4CAF50;
  color: #fff;
  border: none;
  border-radius: 8rpx;
  font-size: 24rpx;
  font-weight: 500;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 移动端适配 - 小屏幕 */
@media screen and (max-width: 375px) {
  .hero-title {
    font-size: 32rpx;
  }
  
  .hero-desc {
    font-size: 24rpx;
  }
  
  .section {
    padding: 25rpx;
  }
  
  .section-title {
    font-size: 28rpx;
  }
  
  .voice-chip {
    min-width: 120rpx;
    padding: 18rpx 24rpx;
  }
  
  .voice-avatar {
    width: 70rpx;
    height: 70rpx;
  }
  
  .voice-name {
    font-size: 24rpx;
  }
}
</style>
