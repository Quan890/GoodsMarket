import request from '@/utils/request'

/** 获取图形验证码 */
export function getCaptcha() {
  return request.get('/user/captcha')
}

/** 发送短信验证码 */
export function sendCode(data) {
  return request.post('/user/send-code', data)
}

/** 短信验证码登录（stores/user.js 中 import { login } 使用） */
export function login(data) {
  return request.post('/user/login', data)
}

/** 密码登录 */
export function loginByPassword(data) {
  return request.post('/user/login-password', data)
}

/** 用户注册 */
export function register(data) {
  return request.post('/user/register', data)
}

/** 找回密码 */
export function resetPassword(data) {
  return request.post('/user/reset-password', data)
}

/** 注销账户 */
export function deleteAccount(data) {
  return request.post('/user/delete-account', data)
}

/** 退出登录（stores/user.js 中 import { logout } 使用） */
export function logout() {
  return request.post('/user/logout')
}

/** 获取用户信息（stores/user.js 中 import { getUserInfo } 使用） */
export function getUserInfo() {
  return request.get('/user/info')
}
