import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo } from '@/api/user'
import { resetUserInfoFetched } from '@/utils/auth'

/**
 * 用户状态仓库（Pinia Setup 语法）
 *
 * 职责：
 *   1. 管理 token、手机号、昵称、角色等登录态
 *   2. 提供登录 / 退出 / 拉取用户信息等 Action
 *   3. 全局身份判断，供页面按钮显隐、路由守卫使用
 *
 * 角色编码（与后端 role 字段一致）：
 *   0 = 游客（未登录）
 *   1 = 普通用户
 *   2 = 商家
 *   3 = 管理员
 *
 * 持久化策略：
 *   token → localStorage（刷新页面后自动恢复登录态）
 *   其余字段 → 内存，刷新后通过 getUserInfo 接口重新拉取
 */
export const useUserStore = defineStore('user', () => {
  // ==================== State ====================

  /** JWT / Sa-Token 令牌 */
  const token = ref(localStorage.getItem('token') || '')

  /** 用户 ID */
  const userId = ref(null)

  /** 手机号（脱敏展示用） */
  const phone = ref('')

  /** 用户昵称 */
  const nickname = ref('')

  /**
   * 角色编码
   * 0=游客  1=用户  2=商家  3=管理员
   */
  const role = ref(0)

  /** 角色名称（中文，用于页面展示） */
  const roleName = ref('')

  /** 用户头像 URL */
  const avatar = ref('')

  // ==================== Getters ====================

  /** 是否已登录 */
  const isLoggedIn = computed(() => !!token.value)

  /** 是否为普通用户 */
  const isUser = computed(() => role.value === 1)

  /** 是否为商家 */
  const isMerchant = computed(() => role.value === 2)

  /** 是否为管理员 */
  const isAdmin = computed(() => role.value === 3)

  /** 是否为商家或管理员（管理类页面通用判断） */
  const isManager = computed(() => role.value === 2 || role.value === 3)

  /**
   * 手机号脱敏（如 138****8000）
   * 未登录时返回空字符串
   */
  const maskedPhone = computed(() => {
    if (!phone.value || phone.value.length < 7) return ''
    return phone.value.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
  })

  // ==================== Actions ====================

  /**
   * 保存登录信息（登录接口成功后调用）
   *
   * @param {Object} data - 后端返回的登录数据
   * @param {string} data.token      - 令牌
   * @param {number} data.userId     - 用户 ID
   * @param {string} data.phone      - 手机号
   * @param {string} data.nickname   - 昵称
   * @param {number} data.role       - 角色编码
   * @param {string} data.roleName   - 角色名称
   * @param {string} [data.avatar]   - 头像 URL
   */
  function setLoginInfo(data) {
    token.value = data.token
    userId.value = data.userId
    phone.value = data.phone || ''
    nickname.value = data.nickname || ''
    role.value = data.role ?? 1
    roleName.value = data.roleName || ''
    avatar.value = data.avatar || ''

    // token 持久化到 localStorage
    localStorage.setItem('token', data.token)
  }

  /**
   * 执行登录
   *
   * @param {Object} params - 登录参数
   * @param {string} params.phone    - 手机号
   * @param {string} params.code     - 验证码
   * @returns {Promise<Object>} 登录响应数据
   */
  async function login(params) {
    const res = await loginApi(params)
    // 接口返回格式：{ code: 200, data: { token, userId, phone, nickname, role, roleName } }
    setLoginInfo(res.data)
    return res.data
  }

  /**
   * 拉取当前用户信息（用于页面刷新后恢复状态）
   * 依赖请求拦截器自动携带 token
   *
   * @returns {Promise<Object>} 用户信息
   */
  async function fetchUserInfo() {
    const res = await getUserInfo()
    const data = res.data
    userId.value = data.userId
    phone.value = data.phone || ''
    nickname.value = data.nickname || ''
    role.value = data.role ?? 1
    roleName.value = data.roleName || ''
    avatar.value = data.avatar || ''
    return data
  }

  /**
   * 退出登录
   *
   * 1. 调用后端退出接口（通知 Sa-Token 失效）
   * 2. 清除本地状态
   * 3. 如需跳转，由调用方负责 router.push
   */
  async function logout() {
    try {
      await logoutApi()
    } catch {
      // 即使后端退出失败，本地也要清除
    }
    clearUserInfo()
  }

  /**
   * 清除所有本地用户状态
   * 供 401 拦截器、退出登录等场景复用
   */
  function clearUserInfo() {
    token.value = ''
    userId.value = null
    phone.value = ''
    nickname.value = ''
    role.value = 0
    roleName.value = ''
    avatar.value = ''
    localStorage.removeItem('token')
    resetUserInfoFetched()
  }

  /**
   * 判断当前用户是否拥有指定角色
   *
   * @param {number|number[]} roles - 允许的角色编码，如 [2, 3] 表示商家或管理员
   * @returns {boolean}
   *
   * @example
   *   userStore.hasRole([2, 3])  // 商家或管理员返回 true
   *   userStore.hasRole(1)       // 普通用户返回 true
   */
  function hasRole(roles) {
    const list = Array.isArray(roles) ? roles : [roles]
    return list.includes(role.value)
  }

  return {
    // state
    token, userId, phone, nickname, role, roleName, avatar,
    // getters
    isLoggedIn, isUser, isMerchant, isAdmin, isManager, maskedPhone,
    // actions
    setLoginInfo, login, fetchUserInfo, logout, clearUserInfo, hasRole,
  }
})
