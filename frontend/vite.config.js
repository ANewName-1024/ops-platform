import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  
  // 构建优化
  build: {
    // 代码分割 - 使用函数形式
    rollupOptions: {
      output: {
        // 手动指定分块 (函数形式)
        manualChunks(id) {
          if (id.includes('node_modules')) {
            if (id.includes('element-plus')) {
              return 'element-plus'
            }
            if (id.includes('echarts')) {
              return 'echarts'
            }
            if (id.includes('vue') || id.includes('pinia')) {
              return 'vue-vendor'
            }
          }
        }
      }
    },
    // 启用 CSS 代码分割
    cssCodeSplit: true,
    // 生成 sourcemap（生产关闭）
    sourcemap: false,
    // 使用默认的 esbuild 压缩
    minify: 'esbuild',
    // 分块大小警告限制
    chunkSizeWarningLimit: 600
  },
  
  // 开发服务器配置
  server: {
    // 启用 Gzip 压缩
    compress: true,
    // 端口
    port: 5173,
    // 代理配置
    proxy: {
      '/ops': {
        target: 'http://localhost:8083',
        changeOrigin: true
      }
    }
  },
  
  // 依赖优化
  optimizeDeps: {
    include: ['vue', 'vue-router', 'pinia', 'element-plus', 'echarts', 'axios']
  },
  
  // 路径别名
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  }
})
