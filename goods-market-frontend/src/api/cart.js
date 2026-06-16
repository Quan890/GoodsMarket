import request from '@/utils/request'

/**
 * 购物车相关接口
 *
 * 对接后端 CartController（/user/cart）
 *
 * 接口一览：
 *   POST   /user/cart/add        → 添加商品到购物车
 *   PUT    /user/cart/quantity    → 修改商品数量
 *   DELETE /user/cart/delete      → 删除购物车商品
 *   GET    /user/cart/list        → 查询购物车列表（分页）
 *
 * DTO 字段（CartOperateDTO）：
 *   productId  - 商品 ID
 *   quantity   - 数量
 */

/**
 * 添加商品到购物车
 *
 * @param {Object}  data
 * @param {number}  data.productId - 商品 ID
 * @param {number}  data.quantity  - 数量
 * @returns {Promise<Result<Void>>}
 *
 * @example
 *   addToCart({ productId: 1, quantity: 2 })
 */
export function addToCart(data) {
  return request.post('/user/cart/add', data)
}

/**
 * 修改购物车商品数量
 *
 * @param {Object}  data
 * @param {number}  data.productId - 商品 ID
 * @param {number}  data.quantity  - 新数量
 * @returns {Promise<Result<Void>>}
 *
 * @example
 *   updateCartQuantity({ productId: 1, quantity: 5 })
 */
export function updateCartQuantity(data) {
  return request.put('/user/cart/quantity', data)
}

/**
 * 删除购物车商品
 *
 * @param {number} productId - 商品 ID
 * @returns {Promise<Result<Void>>}
 *
 * @example
 *   deleteCartItem(1)
 */
export function deleteCartItem(productId) {
  return request.delete('/user/cart/delete', {
    params: { productId },
  })
}

/**
 * 查询购物车列表
 *
 * @param {Object}  params
 * @param {number}  [params.pageNum=1]    - 当前页码
 * @param {number}  [params.pageSize=50]  - 每页条数（默认50，购物车通常一次性加载）
 * @returns {Promise<Result<PageResult<CartVO>>>}
 *
 * @example
 *   getCartList({ pageNum: 1, pageSize: 50 })
 */
export function getCartList(params) {
  return request.get('/user/cart/list', { params })
}
