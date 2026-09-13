import axios from 'axios'
import JSONBig from 'json-bigint'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores'

/**
 * Axios 全局二次封装
 *
 * 功能：
 *   1. baseURL 从 .env 文件读取，开发/生产环境自动切换
 *   2. 请求拦截器自动携带 token（Sa-Token Bearer 格式）
 *   3. 响应拦截器统一处理业务码 + HTTP 状态码
 *   4. 401 自动清除登录态并跳转登录页
 *   5. 使用 json-bigint 解析响应，防止 Long 型 ID 精度丢失
 *
 * 后端 RESTful 接口规范：
 *   成功响应格式 → { code: 200, message: "success", data: {...} }
 *   失败响应格式 → { code: xxx, message: "错误描述", data: null }
 */

const jsonBig = JSONBig({ storeAsString: true })

const service = axios.create({
  // 从环境变量读取，开发环境走 vite proxy（/api），生产环境走 Nginx 反向代理
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
  // 使用 json-bigint 解析响应 JSON，防止雪花 ID 精度丢失
  transformResponse: [(data) => {
    try {
      return jsonBig.parse(data)
    } catch {
      return data
    }
  }],
})

// ==================== 请求拦截器 ====================
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      // Sa-Token 要求的格式：Authorization: Bearer {token}
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// ==================== 响应拦截器 ====================
service.interceptors.response.use(
  (response) => {
    const res = response.data

    // 业务层判断：code !== 200 视为业务异常
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')

      // 401 未登录 → 清除登录态并跳转登录页
      if (res.code === 401) {
        handleUnauthorized()
      }

      return Promise.reject(new Error(res.message || '请求失败'))
    }

    // 返回完整数据，调用方可通过 res.data 取业务数据
    return res
  },
  (error) => {
    // 请求被主动取消（如页面切换），不弹错误提示
    if (axios.isCancel(error)) {
      return Promise.reject(error)
    }

    // HTTP 层错误处理
    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          handleUnauthorized()
          break
        case 403:
          ElMessage.error('权限不足，无法访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error(data?.message || '服务器内部错误')
          break
        default:
          ElMessage.error(data?.message || `请求失败(${status})`)
      }
    } else {
      ElMessage.error('网络连接异常，请检查网络')
    }
    return Promise.reject(error)
  }
)

/**
 * 统一处理 401 未授权
 * 清除 Pinia 用户状态 + localStorage token + 购物车缓存，跳转登录页并记录来源路径
 */
function handleUnauthorized() {
  const userStore = useUserStore()
  if (!userStore.isLoggedIn) return   // 已处理过，避免并发401重复弹跳转
  userStore.clearUserInfo()
  router.push({
    name: 'Login',
    query: { redirect: router.currentRoute.value.fullPath },
  })
}

export default service
