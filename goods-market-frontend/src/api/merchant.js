import request from '@/utils/request'

/**
 * 商家相关接口
 *
 * 对接后端 MerchantController（/merchant）+ ProductController 商家部分（/merchant/product）
 *
 * 接口一览：
 *
 * 【入驻与状态】
 *   POST  /merchant/apply              → 提交入驻申请
 *   GET   /merchant/my/status           → 查询我的商家状态
 *
 * 【商品管理】
 *   POST  /merchant/product/add         → 新增商品
 *   PUT   /merchant/product/edit        → 编辑商品
 *   PUT   /merchant/product/status      → 上架 / 下架
 *   DELETE /merchant/product/delete/{id} → 删除商品
 *   GET   /merchant/product/my          → 我的商品列表（分页）
 *
 * 【店铺订单】
 *   GET   /merchant/product/orders      → 我的商品订单（分页）
 */

// ==================== 入驻与状态 ====================

/**
 * 提交商家入驻申请
 *
 * @param {Object}  data
 * @param {string}  data.shopName     - 店铺名称
 * @param {string}  data.description  - 店铺描述
 * @param {string}  data.contactPhone - 联系电话
 * @param {string}  [data.licenseNo]  - 营业执照号（可选）
 * @returns {Promise<Result<Void>>}
 *
 * @example
 *   merchantApply({ shopName: '好物小铺', description: '精选好物', contactPhone: '13800138000' })
 */
export function merchantApply(data) {
  return request.post('/merchant/apply', data)
}

/**
 * 查询我的商家状态
 *
 * 返回商家信息及审核状态，非商家角色调用会返回未入驻状态
 *
 * @returns {Promise<Result<MerchantVO>>}
 *
 * @example
 *   getMerchantStatus().then(res => {
 *     // res.data.auditStatus: 0待审核 1通过 2驳回
 *   })
 */
export function getMerchantStatus() {
  return request.get('/merchant/my/status')
}

// ==================== 商品管理 ====================

/**
 * 新增商品
 *
 * @param {Object}  data
 * @param {string}  data.name          - 商品名称
 * @param {string}  data.subtitle      - 副标题/卖点
 * @param {number}  data.price         - 售价（元）
 * @param {number}  data.originalPrice - 原价（元）
 * @param {number}  data.stock         - 库存
 * @param {string}  data.mainImage     - 商品主图 URL
 * @param {number}  [data.categoryId]  - 分类 ID
 * @returns {Promise<Result<Void>>}
 *
 * @example
 *   addProduct({ name: '蓝牙耳机', subtitle: '高清音质', price: 199, originalPrice: 299, stock: 100, mainImage: '...' })
 */
export function addProduct(data) {
  return request.post('/merchant/product/add', data)
}

/**
 * 编辑商品
 *
 * @param {Object}  data       - 商品信息（需包含 id）
 * @param {number}  data.id    - 商品 ID
 * @param {string}  [data.name]
 * @param {string}  [data.subtitle]
 * @param {number}  [data.price]
 * @param {number}  [data.originalPrice]
 * @param {number}  [data.stock]
 * @param {string}  [data.mainImage]
 * @param {number}  [data.categoryId]
 * @returns {Promise<Result<Void>>}
 *
 * @example
 *   editProduct({ id: 1, name: '蓝牙耳机Pro', price: 299 })
 */
export function editProduct(data) {
  return request.put('/merchant/product/edit', data)
}

/**
 * 商品上架 / 下架
 *
 * @param {number} productId - 商品 ID
 * @param {number} status    - 目标状态（1上架 0下架）
 * @returns {Promise<Result<Void>>}
 *
 * @example
 *   changeProductStatus(1, 0)  // 下架
 */
export function changeProductStatus(productId, status) {
  return request.put('/merchant/product/status', null, {
    params: { productId, status },
  })
}

/**
 * 删除商品
 *
 * @param {number} id - 商品 ID
 * @returns {Promise<Result<Void>>}
 *
 * @example
 *   deleteProduct(1)
 */
export function deleteProduct(id) {
  return request.delete(`/merchant/product/delete/${id}`)
}

/**
 * 我的商品列表（分页）
 *
 * @param {Object}  params
 * @param {number}  [params.pageNum=1]    - 当前页码
 * @param {number}  [params.pageSize=10]  - 每页条数
 * @param {number}  [params.status]       - 状态筛选（0下架 1上架）
 * @returns {Promise<Result<PageResult<ProductVO>>>}
 *
 * @example
 *   getMyProducts({ pageNum: 1, pageSize: 10, status: 1 })
 */
export function getMyProducts(params) {
  return request.get('/merchant/product/my', { params })
}

// ==================== 店铺订单 ====================

/**
 * 我的商品订单（分页）
 *
 * 查询与当前商家商品相关的订单
 *
 * @param {Object}  params
 * @param {number}  [params.pageNum=1]    - 当前页码
 * @param {number}  [params.pageSize=10]  - 每页条数
 * @param {number}  [params.status]       - 订单状态筛选（0待支付 1已支付 2已取消 3已完成 4已发货）
 * @returns {Promise<Result<PageResult<OrderVO>>>}
 *
 * @example
 *   getMyProductOrders({ pageNum: 1, pageSize: 10, status: 1 })
 */
export function getMyProductOrders(params) {
  return request.get('/merchant/product/orders', { params })
}

/**
 * 商家发货
 *
 * 将本店铺已支付(1)的订单标记为已发货(4)
 *
 * @param {string} orderNo - 订单号
 * @returns {Promise<Result<Void>>}
 */
export function shipOrder(orderNo) {
  return request.put(`/merchant/order/ship/${orderNo}`)
}

// ==================== 经营统计 ====================

/**
 * 商家经营统计（商家中心看板）
 *
 * @returns {Promise<Result<{totalProducts, onSaleProducts, totalOrders, todayOrders,
 *   pendingShipOrders, shippedOrders, completedOrders, totalSales, todaySales}>>}
 */
export function getMerchantStats() {
  return request.get('/merchant/stats')
}
