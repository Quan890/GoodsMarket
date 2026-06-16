import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'

// Element Plus 全局样式（按需导入由 unplugin-vue-components 处理）
import 'element-plus/dist/index.css'

// 全局自定义样式
import '@/assets/styles/index.scss'

// Element Plus 图标全局注册
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// NProgress 进度条
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// ==================== NProgress 配置 ====================
NProgress.configure({
  showSpinner: false,   // 隐藏右上角旋转圆圈
  minimum: 0.1,         // 最小百分比
  speed: 300,           // 速度
})

// 路由开始跳转 → 启动进度条
router.beforeEach(() => {
  NProgress.start()
})

// 路由跳转完成 → 关闭进度条
router.afterEach(() => {
  NProgress.done()
})

// ==================== 创建应用实例 ====================
const app = createApp(App)
const pinia = createPinia()

// 注册 Pinia（必须在 router 之前，路由守卫内需要 useUserStore）
app.use(pinia)
app.use(router)

// 注册所有 Element Plus 图标为全局组件
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// ==================== 挂载 ====================
app.mount('#app')
