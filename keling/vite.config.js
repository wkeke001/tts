import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

export default defineConfig({
  plugins: [uni()],
  server: {
    port: 8080,
    host: '0.0.0.0',
    proxy: {
      '/api/aliyun': {
        target: 'https://dashscope.aliyuncs.com',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/aliyun/, '')
      },
      '/api': {
        target: 'http://localhost:8087',
        changeOrigin: true
      }
    }
  }
})