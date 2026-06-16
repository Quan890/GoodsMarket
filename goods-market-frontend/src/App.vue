<template>
  <!-- 管理员后台使用独立布局，不显示全局顶栏 -->
  <div id="app">
    <!-- 全局顶栏（管理员后台隐藏） -->
    <header v-if="showHeader" class="global-header">
      <div class="header-inner">
        <!-- Logo -->
        <router-link to="/" class="header-logo">
          <h1>好物集市</h1>
        </router-link>

        <!-- 导航链接 -->
        <nav class="header-nav">
          <router-link to="/" class="nav-link nav-home-btn">
            <el-icon><HomeFilled /></el-icon>
            首页
          </router-link>

          <!-- 已登录 -->
          <template v-if="userStore.isLoggedIn">
            <router-link to="/user/cart" class="nav-link">
              <el-badge :value="cartStore.totalCount" :hidden="cartStore.totalCount === 0" :max="99">
                <el-icon><ShoppingCart /></el-icon>
                购物车
              </el-badge>
            </router-link>
            <router-link to="/user/order" class="nav-link">我的订单</router-link>
            <router-link to="/user/collect" class="nav-link">我的收藏</router-link>

            <!-- 角色入口 -->
            <router-link v-if="userStore.isMerchant" to="/merchant" class="nav-link">
              商家中心
            </router-link>
            <router-link v-if="userStore.isAdmin" to="/admin" class="nav-link">
              管理后台
            </router-link>

            <!-- 用户菜单 -->
            <el-dropdown trigger="click" @command="handleUserCommand">
              <span class="user-dropdown">
                <el-icon><UserFilled /></el-icon>
                {{ userStore.nickname || userStore.maskedPhone || '用户' }}
                <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="center">个人中心</el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isUser" command="apply">商家入驻</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>

          <!-- 未登录 -->
          <template v-else>
            <router-link to="/login" class="nav-link login-btn">
              登录
            </router-link>
          </template>
        </nav>
      </div>
    </header>

    <!-- 路由视图 -->
    <router-view />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { ShoppingCart, UserFilled, ArrowDown } from '@element-plus/icons-vue'
import { useUserStore, useCartStore } from '@/stores'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

/**
 * 是否显示全局顶栏
 * 管理员后台（/admin）和登录页使用独立布局，隐藏顶栏
 */
const showHeader = computed(() => {
  return !route.path.startsWith('/admin') && !route.meta?.hideHeader
})

/**
 * 用户下拉菜单命令处理
 */
function handleUserCommand(command) {
  switch (command) {
    case 'center':
      router.push({ name: 'UserCenter' })
      break
    case 'apply':
      router.push({ name: 'MerchantApply' })
      break
    case 'logout':
      handleLogout()
      break
  }
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定退出登录？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await userStore.logout()
    cartStore.clearCart()
    router.push({ name: 'Home' })
  } catch {
    // 用户取消
  }
}
</script>

<style>
/* ==================== 全局基础样式 ==================== */
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

a {
  text-decoration: none;
  color: inherit;
}
</style>

<style lang="scss" scoped>
/* ==================== 全局顶栏 ==================== */
.global-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  height: 60px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200px;
  height: 100%;
  margin: 0 auto;
  padding: 0 20px;
}

.header-logo {
  h1 {
    margin: 0;
    font-size: 22px;
    font-weight: 700;
    color: #409eff;
    letter-spacing: 2px;
  }
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 6px;
}

.nav-link {
  padding: 6px 14px;
  font-size: 14px;
  color: #606266;
  border-radius: 4px;
  transition: all 0.2s;

  &:hover {
    color: #409eff;
    background: #ecf5ff;
  }

  &.router-link-active {
    color: #409eff;
    font-weight: 600;
  }
}

.nav-home-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 16px;
  font-weight: 600;
  color: #409eff;
  background: #ecf5ff;
  border: 1px solid #b3d8ff;
  border-radius: 6px;

  &:hover {
    color: #fff;
    background: #409eff;
    border-color: #409eff;
  }
}

.login-btn {
  color: #409eff;
  font-weight: 600;
}

/* 用户下拉 */
.user-dropdown {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  font-size: 14px;
  color: #606266;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;

  &:hover {
    color: #409eff;
    background: #ecf5ff;
  }

  .dropdown-arrow {
    font-size: 12px;
  }
}
</style>
