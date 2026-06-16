import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),

    // Element Plus 按需自动导入
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia'],
      // 自动导入生成的类型声明文件
      dts: 'src/auto-imports.d.ts',
    }),
    Components({
      resolvers: [ElementPlusResolver()],
      // 自动导入生成的组件类型声明文件
      dts: 'src/components.d.ts',
    }),
  ],

  // 路径别名
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },

  // 开发服务器配置
  server: {
    port: 5173,
    open: true,
    // 代理跨域：将 /api 请求转发到后端 SpringBoot 服务
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 后端 context-path 已配置 /api，此处无需重写
        // rewrite: (path) => path.replace(/^\/api/, ''),
      },
      // 静态图片代理：数据库中图片路径为 /images/xxx，需转发到后端 /api/images/xxx
      '/images': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => '/api' + path,
      },
    },
  },

  // 构建配置
  build: {
    outDir: 'dist',
    minify: 'esbuild',
  },
})
