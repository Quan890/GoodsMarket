import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores'
import { isUserInfoFetched, setUserInfoFetched } from '@/utils/auth'

/**
 * 路由配置 + 全局路由守卫
 *
 * 路由分层（权限由低到高）：
 *   ┌─────────────────────────────────────────────────────────┐
 *   │  公开路由        无需登录，任何人可访问                    │
 *   │  用户路由        需登录（role ≥ 1）                      │
 *   │  商家路由        需商家角色（role = 2）                   │
 *   │  管理员路由      需管理员角色（role = 3）                 │
 *   └─────────────────────────────────────────────────────────┘
 *
 * meta 字段说明：
 *   requiresAuth  - boolean  是否需要登录（默认 false）
 *   role          - number   允许访问的角色编码（需配合 requiresAuth）
 *   guestOnly     - boolean  仅游客可访问（如登录页，已登录自动跳首页）
 *   title         - string   页面标题
 *   hideHeader    - boolean  是否隐藏全局顶栏（如登录页）
 *   keepAlive     - boolean  是否缓存组件实例
 */

// ============================================================
// 一、路由分组定义
// ============================================================

/**
 * 1. 公开路由 —— 任何人可访问，无需登录
 */
const publicRoutes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/home/Index.vue'),
    meta: { title: '首页', keepAlive: true },
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('@/views/home/ProductDetail.vue'),
    meta: { title: '商品详情' },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Index.vue'),
    meta: { title: '登录', guestOnly: true, hideHeader: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/Register.vue'),
    meta: { title: '注册', guestOnly: true, hideHeader: true },
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/login/ForgotPassword.vue'),
    meta: { title: '找回密码', guestOnly: true, hideHeader: true },
  },
]

/**
 * 2. 用户路由 —— 需登录（role ≥ 1）
 *
 * 包含：购物车、收藏、订单相关页面
 * 使用 UserLayout 作为父级布局（侧边栏 + 内容区）
 */
const userRoutes = [
  {
    path: '/user',
    component: () => import('@/views/user/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'UserCenter',
        component: () => import('@/views/user/Index.vue'),
        meta: { title: '个人中心' },
      },
      {
        path: 'collect',
        name: 'UserCollect',
        component: () => import('@/views/user/Collect.vue'),
        meta: { title: '我的收藏' },
      },
      {
        path: 'cart',
        name: 'UserCart',
        component: () => import('@/views/user/Cart.vue'),
        meta: { title: '购物车' },
      },
      {
        path: 'order',
        name: 'UserOrder',
        component: () => import('@/views/user/Order.vue'),
        meta: { title: '我的订单' },
      },
      {
        path: 'order/:orderNo',
        name: 'OrderDetail',
        component: () => import('@/views/user/OrderDetail.vue'),
        meta: { title: '订单详情' },
      },
      {
        path: 'order-confirm',
        name: 'OrderConfirm',
        component: () => import('@/views/user/OrderConfirm.vue'),
        meta: { title: '确认订单' },
      },
      {
        path: 'payment/:orderNo',
        name: 'Payment',
        component: () => import('@/views/user/Payment.vue'),
        meta: { title: '收银台' },
      },
    ],
  },
]

/**
 * 3. 商家路由 —— 需商家角色（role = 2）
 *
 * 包含：入驻申请、商品管理、店铺订单
 * 商家入驻申请页允许已登录用户访问（用于申请成为商家）
 * 其余页面仅商家可访问
 */
const merchantRoutes = [
  // 商家入驻申请 —— 独立路由，不使用商家侧边栏布局
  {
    path: '/merchant/apply',
    name: 'MerchantApply',
    component: () => import('@/views/merchant/Apply.vue'),
    meta: { title: '入驻申请', requiresAuth: true },
  },
  // 商家后台 —— 带侧边栏布局，仅商家可访问
  {
    path: '/merchant',
    component: () => import('@/views/merchant/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'MerchantHome',
        component: () => import('@/views/merchant/Index.vue'),
        meta: { title: '商家中心', role: 2 },
      },
      {
        path: 'product',
        name: 'MerchantProduct',
        component: () => import('@/views/merchant/Product.vue'),
        meta: { title: '商品管理', role: 2 },
      },
      {
        path: 'order',
        name: 'MerchantOrder',
        component: () => import('@/views/merchant/Order.vue'),
        meta: { title: '店铺订单', role: 2 },
      },
    ],
  },
]

/**
 * 4. 管理员路由 —— 需管理员角色（role = 3）
 *
 * 包含：后台首页、用户管理、商家审核、商品管控、全平台订单
 */
const adminRoutes = [
  {
    path: '/admin',
    component: () => import('@/views/admin/Layout.vue'),
    meta: { requiresAuth: true, role: 3 },
    children: [
      {
        path: '',
        name: 'AdminHome',
        component: () => import('@/views/admin/Index.vue'),
        meta: { title: '管理后台' },
      },
      {
        path: 'user',
        name: 'AdminUser',
        component: () => import('@/views/admin/User.vue'),
        meta: { title: '用户管理' },
      },
      {
        path: 'merchant',
        name: 'AdminMerchant',
        component: () => import('@/views/admin/Merchant.vue'),
        meta: { title: '商家审核' },
      },
      {
        path: 'product',
        name: 'AdminProduct',
        component: () => import('@/views/admin/Product.vue'),
        meta: { title: '商品管控' },
      },
      {
        path: 'order',
        name: 'AdminOrder',
        component: () => import('@/views/admin/Order.vue'),
        meta: { title: '全平台订单' },
      },
    ],
  },
]

/**
 * 5. 兜底路由 —— 404 页面
 */
const errorRoutes = [
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在' },
  },
]

// ============================================================
// 二、创建路由实例
// ============================================================

const router = createRouter({
  history: createWebHistory(),
  routes: [
    ...publicRoutes,
    ...userRoutes,
    ...merchantRoutes,
    ...adminRoutes,
    ...errorRoutes,
  ],
  // 路由跳转时滚动到顶部（前进新页面）或保持原位（浏览器后退）
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  },
})

// ============================================================
// 三、全局前置守卫（核心权限拦截逻辑）
// ============================================================

/**
 * 白名单路由：无需登录即可访问的路由名称
 * 未登录用户访问非白名单路由时，自动跳转登录页
 */
const WHITE_LIST = ['Home', 'ProductDetail', 'Login', 'Register', 'ForgotPassword']

router.beforeEach(async (to, from, next) => {
  // ---------- 1. 设置页面标题 ----------
  document.title = to.meta.title
    ? `${to.meta.title} - 好物集市`
    : '好物集市'

  // ---------- 2. 获取用户状态 ----------
  const userStore = useUserStore()
  let isLoggedIn = userStore.isLoggedIn
  let role = userStore.role

  // ---------- 2.1 已登录但角色未恢复 → 先拉取用户信息 ----------
  // 页面刷新后 Pinia 状态丢失，token 在 localStorage 中但 role=0
  // 必须先调用 fetchUserInfo 恢复角色，再做权限判断
  if (isLoggedIn && role === 0 && !isUserInfoFetched()) {
    try {
      await userStore.fetchUserInfo()
      setUserInfoFetched(true)
      role = userStore.role
    } catch {
      // 拉取失败：token 无效（过期/被注销）时 clearUserInfo 已清除本地登录态；
      // 请求层401会自动跳登录页，这里直接以未登录状态继续走守卫逻辑
      setUserInfoFetched(true)
      isLoggedIn = userStore.isLoggedIn
      role = userStore.role
    }
  }

  // ---------- 3. 已登录用户访问登录页 → 重定向 ----------
  if (to.meta.guestOnly && isLoggedIn) {
    const homeMap = {
      3: { name: 'AdminHome' },
      2: { name: 'MerchantHome' },
      1: { name: 'UserCenter' },
    }
    next(homeMap[role] || { name: 'Home' })
    return
  }

  // ---------- 4. 白名单路由直接放行 ----------
  if (WHITE_LIST.includes(to.name)) {
    next()
    return
  }

  // ---------- 5. 未登录拦截 ----------
  if (to.meta.requiresAuth && !isLoggedIn) {
    next({
      name: 'Login',
      query: { redirect: to.fullPath },
    })
    return
  }

  // ---------- 6. 角色权限校验 ----------
  const requiredRole = findRequiredRole(to)

  if (requiredRole !== null) {
    if (role !== requiredRole) {
      handleRoleDenied(userStore, to, next)
      return
    }
  }

  // ---------- 7. 放行 ----------
  next()
})

// ============================================================
// 四、辅助函数
// ============================================================

/**
 * 沿 matched 路由链向上查找 role 要求
 *
 * 优先取当前路由的 meta.role，若未定义则继承父级路由的 meta.role
 * 这样 /admin 下的所有子路由自动继承 role=3，无需每个子路由重复声明
 *
 * @param {Object} to - 目标路由对象
 * @returns {number|null} 所需角色编码，null 表示不限制
 *
 * @example
 *   /admin/user   → matched: [admin布局, user页面]
 *   admin布局 meta.role = 3 → 返回 3
 *
 *   /merchant/apply → matched: [merchant布局, apply页面]
 *   apply页面无 role → 继承 merchant布局 → 返回 undefined（不限制）
 */
function findRequiredRole(to) {
  // 从当前路由往父级遍历，找到第一个定义了 role 的路由
  for (let i = to.matched.length - 1; i >= 0; i--) {
    const meta = to.matched[i].meta
    if (meta.role !== undefined && meta.role !== null) {
      return meta.role
    }
  }
  return null
}

/**
 * 角色不匹配时的统一处理
 *
 * 根据用户实际角色跳转到对应首页，并弹出提示
 *
 * @param {Object}   userStore - 用户仓库实例
 * @param {Object}   to        - 目标路由
 * @param {Function} next      - 路由守卫 next 函数
 */
function handleRoleDenied(userStore, to, next) {
  // 构造角色名称映射，用于提示信息
  const roleNames = { 1: '用户', 2: '商家', 3: '管理员' }
  const requiredName = roleNames[findRequiredRole(to)] || '对应权限'

  ElMessage.warning(`您没有访问权限，需要${requiredName}身份`)

  // 游客（未登录或角色异常）→ 跳转登录页
  if (userStore.role === 0) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 跳转到当前用户角色对应的首页
  const fallbackMap = {
    3: { name: 'AdminHome' },
    2: { name: 'MerchantHome' },
    1: { name: 'UserCenter' },
  }
  next(fallbackMap[userStore.role] || { name: 'Home' })
}

// ============================================================
// 五、全局后置钩子
// ============================================================

/**
 * 全局后置钩子
 * 可用于：关闭全局 loading、页面访问统计等
 */
router.afterEach((to, from) => {
  // 预留：页面访问统计
})

export default router
