/**
 * 登录态管理辅助模块
 *
 * 解决页面刷新后 Pinia 状态丢失的问题：
 *   - 路由守卫在首次进入时调用 fetchUserInfo 恢复角色
 *   - 退出登录时重置标记，确保下次登录后重新拉取
 */

/** 是否已完成首次用户信息拉取 */
let userInfoFetched = false

export function isUserInfoFetched() {
  return userInfoFetched
}

export function setUserInfoFetched(val) {
  userInfoFetched = val
}

export function resetUserInfoFetched() {
  userInfoFetched = false
}
