<template>
  <div class="admin-layout">
    <!-- ==================== 侧边栏 ==================== -->
    <aside class="admin-sidebar">
      <div class="sidebar-logo">
        <h2>好物集市</h2>
        <span>管理后台</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="#ffffffb3"
        active-text-color="#ffffff"
        class="sidebar-menu"
      >
        <el-menu-item index="/admin">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-menu-item index="/admin/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/merchant">
          <el-icon><Shop /></el-icon>
          <span>商家审核</span>
        </el-menu-item>
        <el-menu-item index="/admin/product">
          <el-icon><Goods /></el-icon>
          <span>商品管控</span>
        </el-menu-item>
        <el-menu-item index="/admin/order">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- ==================== 右侧主体 ==================== -->
    <div class="admin-main">
      <!-- 顶部导航栏 -->
      <header class="admin-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ name: 'AdminHome' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-button link @click="router.push('/')">
            <el-icon><HomeFilled /></el-icon>
            返回前台
          </el-button>
          <el-divider direction="vertical" />
          <span class="admin-user">
            <el-icon><UserFilled /></el-icon>
            {{ userStore.nickname || '管理员' }}
          </span>
          <el-button type="danger" link @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出
          </el-button>
        </div>
      </header>

      <!-- 页面内容区 -->
      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  DataAnalysis, User, Shop, Goods, List,
  UserFilled, SwitchButton, HomeFilled,
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

/** 当前激活的菜单项 */
const activeMenu = computed(() => {
  // 匹配 /admin 或 /admin/xxx 的前缀
  const path = route.path
  if (path === '/admin') return '/admin'
  const match = path.match(/^(\/admin\/\w+)/)
  return match ? match[1] : '/admin'
})

/** 面包屑当前页标题 */
const currentTitle = computed(() => route.meta?.title || '')

/** 退出登录 */
async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定退出登录？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await userStore.logout()
    router.replace({ name: 'Login' })
  } catch {
    // 用户取消
  }
}
</script>

<style lang="scss" scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

/* 侧边栏 */
.admin-sidebar {
  flex-shrink: 0;
  width: 220px;
  background: #001529;
  display: flex;
  flex-direction: column;
}

.sidebar-logo {
  padding: 20px 16px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);

  h2 {
    font-size: 20px;
    font-weight: 700;
    color: #fff;
    letter-spacing: 2px;
  }

  span {
    font-size: 12px;
    color: #ffffffb3;
  }
}

.sidebar-menu {
  flex: 1;
  border-right: none;

  :deep(.el-menu-item) {
    &:hover {
      background: rgba(255, 255, 255, 0.06) !important;
    }

    &.is-active {
      background: #409eff !important;
    }
  }
}

/* 右侧主体 */
.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #f0f2f5;
}

/* 顶部导航 */
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  flex-shrink: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.admin-user {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
}

/* 内容区 */
.admin-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
</style>
