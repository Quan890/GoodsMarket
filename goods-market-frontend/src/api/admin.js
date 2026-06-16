import request from '@/utils/request'

/**
 * 管理员相关接口
 *
 * 对接后端 AdminController（/admin）
 * 所有接口需管理员角色（role=3），由路由守卫 + 后端 Sa-Token 双重校验
 *
 * 接口一览：
 *
 * 【用户管理】
 *   GET    /admin/user/list          → 用户列表（分页 + 多条件筛选）
 *   PUT    /admin/user/status        → 启用 / 禁用用户
 *   PUT    /admin/user/role          → 修改用户角色
 *
 * 【商家审核】
 *   GET    /admin/merchant/pending   → 待审核商家列表
 *   PUT    /admin/merchant/audit     → 审核商家（通过 / 驳回）
 *   GET    /admin/merchant/list      → 全部商家列表（分页 + 筛选）
 *
 * 【商品管控】
 *   GET    /admin/product/list       → 全平台商品列表（分页 + 筛选）
 *   PUT    /admin/product/off-shelf  → 强制下架商品
 *   DELETE /admin/product/delete      → 强制删除商品
 *
 * 【全平台订单】
 *   GET    /admin/order/list         → 全平台订单列表（分页 + 筛选）
 *   PUT    /admin/order/handle       → 处理异常订单
 *
 * 【数据统计】
 *   GET    /admin/statistics         → 首页统计数据
 */

// ==================== 用户管理 ====================

/**
 * 用户列表（分页 + 多条件筛选）
 *
 * @param {Object}  params
 * @param {number}  [params.pageNum=1]    - 当前页码
 * @param {number}  [params.pageSize=10]  - 每页条数
 * @param {string}  [params.phone]        - 按手机号模糊搜索
 * @param {number}  [params.role]         - 按角色筛选（1用户 2商家 3管理员）
 * @param {number}  [params.status]       - 按状态筛选（0禁用 1正常）
 * @returns {Promise<Result<PageResult<User>>>}
 *
 * @example
 *   getUserList({ pageNum: 1, pageSize: 10, role: 2, status: 1 })
 */
export function getUserList(params) {
  return request.get('/admin/user/list', { params })
}

/**
 * 启用 / 禁用用户
 *
 * @param {number} userId - 用户 ID
 * @param {number} status - 目标状态（0禁用 1正常）
 * @returns {Promise<Result<Void>>}
 *
 * @example
 *   updateUserStatus(1001, 0)  // 禁用用户
 */
export function updateUserStatus(userId, status) {
  return request.put('/admin/user/status', null, {
    params: { userId, status },
  })
}

/**
 * 修改用户角色
 *
 * @param {Object}  data
 * @param {number}  data.userId - 用户 ID
 * @param {number}  data.role   - 目标角色（1用户 2商家 3管理员）
 * @returns {Promise<Result<Void>>}
 *
 * @example
 *   updateUserRole({ userId: 1001, role: 2 })  // 提升为商家
 */
export function updateUserRole(data) {
  return request.put('/admin/user/role', data)
}

// ==================== 商家审核 ====================

/**
 * 待审核商家列表
 *
 * @param {Object}  params
 * @param {number}  [params.pageNum=1]    - 当前页码
 * @param {number}  [params.pageSize=10]  - 每页条数
 * @returns {Promise<Result<PageResult<MerchantVO>>>}
 *
 * @example
 *   getPendingMerchants({ pageNum: 1, pageSize: 10 })
 */
export function getPendingMerchants(params) {
  return request.get('/admin/merchant/pending', { params })
}

/**
 * 审核商家（通过 / 驳回）
 *
 * @param {Object}  data
 * @param {number}  data.merchantId   - 商家 ID
 * @param {number}  data.auditStatus  - 审核结果（1通过 2驳回）
 * @param {string}  [data.auditRemark] - 驳回原因（驳回时必填）
 * @returns {Promise<Result<Void>>}
 *
 * @example
 *   auditMerchant({ merchantId: 101, auditStatus: 1 })           // 通过
 *   auditMerchant({ merchantId: 102, auditStatus: 2, reason: '信息不完整' })  // 驳回
 */
export function auditMerchant(data) {
  return request.put('/admin/merchant/audit', data)
}

/**
 * 全部商家列表（分页 + 筛选）
 *
 * @param {Object}  params
 * @param {number}  [params.pageNum=1]      - 当前页码
 * @param {number}  [params.pageSize=10]    - 每页条数
 * @param {string}  [params.shopName]       - 按店铺名模糊搜索
 * @param {number}  [params.auditStatus]    - 按审核状态筛选（0待审核 1通过 2驳回）
 * @returns {Promise<Result<PageResult<MerchantVO>>>}
 *
 * @example
 *   getAllMerchants({ pageNum: 1, pageSize: 10, auditStatus: 1 })
 */
export function getAllMerchants(params) {
  return request.get('/admin/merchant/list', { params })
}

// ==================== 商品管控 ====================

/**
 * 全平台商品列表（分页 + 筛选）
 *
 * @param {Object}  params
 * @param {number}  [params.pageNum=1]      - 当前页码
 * @param {number}  [params.pageSize=10]    - 每页条数
 * @param {string}  [params.name]           - 按商品名模糊搜索
 * @param {number}  [params.merchantId]     - 按商家 ID 筛选
 * @param {number}  [params.status]         - 按状态筛选（0下架 1上架）
 * @returns {Promise<Result<PageResult<ProductVO>>>}
 *
 * @example
 *   getAllProducts({ pageNum: 1, pageSize: 10, status: 1 })
 */
export function getAllProducts(params) {
  return request.get('/admin/product/list', { params })
}

/**
 * 强制下架商品
 *
 * @param {number} productId - 商品 ID
 * @returns {Promise<Result<Void>>}
 *
 * @example
 *   forceOffShelfProduct(42)
 */
export function forceOffShelfProduct(productId) {
  return request.put('/admin/product/off-shelf', null, {
    params: { productId },
  })
}

/**
 * 强制删除商品
 *
 * @param {number} productId - 商品 ID
 * @returns {Promise<Result<Void>>}
 *
 * @example
 *   forceDeleteProduct(42)
 */
export function forceDeleteProduct(productId) {
  return request.delete('/admin/product/delete', {
    params: { productId },
  })
}

/**
 * 重新上架商品
 *
 * @param {number} productId - 商品 ID
 * @returns {Promise<Result<Void>>}
 *
 * @example
 *   forceOnShelfProduct(42)
 */
export function forceOnShelfProduct(productId) {
  return request.put('/admin/product/on-shelf', null, {
    params: { productId },
  })
}

// ==================== 全平台订单 ====================

/**
 * 全平台订单列表（分页 + 筛选）
 *
 * @param {Object}  params
 * @param {number}  [params.pageNum=1]      - 当前页码
 * @param {number}  [params.pageSize=10]    - 每页条数
 * @param {string}  [params.orderNo]        - 按订单号精确搜索
 * @param {number}  [params.status]         - 按订单状态筛选（0待付款 1已付款 2已发货 3已完成 4已取消）
 * @param {number}  [params.merchantId]     - 按商家 ID 筛选
 * @returns {Promise<Result<PageResult<OrderVO>>>}
 *
 * @example
 *   getAllOrders({ pageNum: 1, pageSize: 10, status: 1, merchantId: 5 })
 */
export function getAllOrders(params) {
  return request.get('/admin/order/list', { params })
}

/**
 * 处理异常订单（手动变更状态）
 *
 * @param {Object}  data
 * @param {string}  data.orderNo      - 订单号
 * @param {number}  data.targetStatus - 目标状态
 * @returns {Promise<Result<Void>>}
 *
 * @example
 *   handleAbnormalOrder({ orderNo: '202606151234567890', targetStatus: 3 })
 */
export function handleAbnormalOrder(data) {
  return request.put('/admin/order/handle', null, {
    params: data,
  })
}

// ==================== 数据统计 ====================

/**
 * 首页统计数据（Dashboard）
 *
 * 返回平台整体运营数据概览
 *
 * @returns {Promise<Result<OrderStatisticsVO>>}
 *
 * @example
 *   getStatistics().then(res => {
 *     // res.data: { totalOrders, totalAmount, totalUsers, totalMerchants, ... }
 *   })
 */
export function getStatistics() {
  return request.get('/admin/statistics')
}
