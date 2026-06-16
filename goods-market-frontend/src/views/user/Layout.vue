<template>
  <div class="user-layout">
    <!-- ==================== 侧边栏 ==================== -->
    <aside class="user-sidebar">
      <div class="sidebar-header">
        <el-icon :size="28" color="#409eff"><UserFilled /></el-icon>
        <div class="user-brief">
          <span class="nickname">{{ userStore.nickname || '用户' }}</span>
          <el-tag size="small" :type="roleTagType">{{ roleLabel }}</el-tag>
        </div>
      </div>

      <el-menu
        :default-active="activeMenu"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/user">
          <el-icon><HomeFilled /></el-icon>
          <span>个人中心</span>
        </el-menu-item>
        <el-menu-item index="/user/cart">
          <el-icon><ShoppingCart /></el-icon>
          <span>购物车</span>
        </el-menu-item>
        <el-menu-item index="/user/collect">
          <el-icon><Star /></el-icon>
          <span>我的收藏</span>
        </el-menu-item>
        <el-menu-item index="/user/order">
          <el-icon><List /></el-icon>
          <span>我的订单</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- ==================== 右侧内容区 ==================== -->
    <main class="user-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  UserFilled, HomeFilled, ShoppingCart, Star, List,
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores'

const route = useRoute()
const userStore = useUserStore()

/** 当前激活的菜单项 */
const activeMenu = computed(() => {
  const path = route.path
  if (path === '/user') return '/user'
  const match = path.match(/^(\/user\/\w+)/)
  return match ? match[1] : '/user'
})

/** 角色标签 */
const roleLabel = computed(() => {
  const map = { 0: '游客', 1: '普通用户', 2: '商家', 3: '管理员' }
  return map[userStore.role] || '未知'
})

const roleTagType = computed(() => {
  const map = { 0: 'info', 1: '', 2: 'warning', 3: 'danger' }
  return map[userStore.role] || 'info'
})
</script>

<style lang="scss" scoped>
.user-layout {
  display: flex;
  min-height: calc(100vh - 60px);
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  gap: 20px;
}

/* 侧边栏 */
.user-sidebar {
  flex-shrink: 0;
  width: 220px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.user-brief {
  display: flex;
  flex-direction: column;
  gap: 4px;

  .nickname {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }
}

.sidebar-menu {
  border-right: none;

  :deep(.el-menu-item) {
    height: 48px;
    line-height: 48px;

    &:hover {
      background: #f5f7fa;
    }

    &.is-active {
      color: #409eff;
      background: #ecf5ff;
    }
  }
}

/* 右侧内容区 */
.user-main {
  flex: 1;
  min-width: 0;
}
</style>
