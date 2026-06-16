<template>
  <div class="merchant-layout">
    <!-- ==================== 侧边栏 ==================== -->
    <aside class="merchant-sidebar">
      <div class="sidebar-logo">
        <h2>好物集市</h2>
        <span>商家后台</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="#ffffffb3"
        active-text-color="#ffffff"
        class="sidebar-menu"
      >
        <el-menu-item index="/merchant">
          <el-icon><HomeFilled /></el-icon>
          <span>商家中心</span>
        </el-menu-item>
        <el-menu-item index="/merchant/product">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/merchant/order">
          <el-icon><List /></el-icon>
          <span>店铺订单</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- ==================== 右侧主体 ==================== -->
    <div class="merchant-main">
      <!-- 顶部导航栏 -->
      <header class="merchant-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ name: 'MerchantHome' }">商家中心</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <span class="merchant-user">
            <el-icon><UserFilled /></el-icon>
            {{ userStore.nickname || '商家' }}
          </span>
          <el-button type="danger" link @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出
          </el-button>
        </div>
      </header>

      <!-- 页面内容区 -->
      <main class="merchant-content">
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
  HomeFilled, Goods, List,
  UserFilled, SwitchButton,
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

/** 当前激活的菜单项 */
const activeMenu = computed(() => {
  const path = route.path
  if (path === '/merchant') return '/merchant'
  const match = path.match(/^(\/merchant\/\w+)/)
  return match ? match[1] : '/merchant'
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
.merchant-layout {
  display: flex;
  min-height: calc(100vh - 60px);
}

/* 侧边栏 */
.merchant-sidebar {
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
.merchant-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #f0f2f5;
}

/* 顶部导航 */
.merchant-header {
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

.merchant-user {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
}

/* 内容区 */
.merchant-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
</style>
