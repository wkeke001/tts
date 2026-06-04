<template>
  <view class="app-container">
    <slot></slot>
  </view>
</template>

<script>
export default {
  onLaunch: function() {
    console.log('App Launch')
  },
  onShow: function() {
    console.log('App Show')
  },
  onHide: function() {
    console.log('App Hide')
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f5f5f5;
  color: #333;
  line-height: 1.6;
}

page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.app-container {
  min-height: 100vh;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 顶部样式 */
.header {
  padding: 40px 0;
  text-align: center;
  position: relative;
}

.header-title {
  display: block;
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.header-subtitle {
  display: block;
  font-size: 16px;
  color: #666;
}

.login-btn {
  position: absolute;
  top: 40px;
  right: 20px;
  font-size: 16px;
  color: #666;
  text-decoration: none;
  cursor: pointer;
  transition: color 0.3s;
}

.login-btn:hover {
  color: #333;
}

.card-title {
  display: block;
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 8px;
}

.card-description {
  display: block;
  font-size: 14px;
  color: #666;
  flex-grow: 1;
}

.footer-link {
  color: #666;
  text-decoration: none;
  margin: 0 12px;
  font-size: 14px;
  cursor: pointer;
  transition: color 0.3s;
}

.footer-link:hover {
  color: #333;
}

/* 功能区样式 */
.features-section {
  padding: 40px 0;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 40px;
}

/* 卡片样式 */
.feature-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  position: relative;
  cursor: pointer;
  display: flex;
  flex-direction: column;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.feature-card.core {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.card-icon {
  font-size: 24px;
  margin-bottom: 16px;
}



.feature-card.core .card-description {
  color: rgba(255, 255, 255, 0.8);
}

.coming-soon {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #ff6b6b;
  color: #fff;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 12px;
}

/* 底部样式 */
.footer {
  padding: 40px 0;
  text-align: center;
  border-top: 1px solid #e0e0e0;
  margin-top: 40px;
}

.footer-links {
  margin-bottom: 16px;
}

.footer-links a {
  color: #666;
  text-decoration: none;
  margin: 0 12px;
  font-size: 14px;
}

.footer-links a:hover {
  color: #333;
}

.footer-copyright {
  font-size: 12px;
  color: #999;
}

/* 媒体查询 */
@media (max-width: 1024px) {
  .features-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .header-title {
    font-size: 28px;
  }
  
  .header-subtitle {
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .header {
    padding: 30px 0;
  }
  
  .header-title {
    font-size: 24px;
  }
  
  .login-btn {
    top: 30px;
  }
  
  .features-section {
    padding: 20px 0;
  }
  
  .feature-card {
    padding: 20px;
  }
  
  .footer {
    padding: 30px 0;
  }
}
</style>