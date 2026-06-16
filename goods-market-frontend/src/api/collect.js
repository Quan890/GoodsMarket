import request from '@/utils/request'

/**
 * 收藏相关接口
 *
 * 对接后端 CollectController（/user/collect）
 *
 * 接口一览：
 *   POST   /user/collect/add     → 收藏商品
 *   DELETE /user/collect/cancel   → 取消收藏
 *   GET    /user/collect/list     → 我的收藏列表（分页）
 *   GET    /user/collect/check    → 查询商品是否已收藏
 */

/**
 * 收藏商品
 *
 * @param {number} productId - 商品 ID
 * @returns {Promise<Result<Void>>}
 *
 * @example
 *   addCollect(42)
 */
export function addCollect(productId) {
  return request.post('/user/collect/add', null, {
    params: { productId },
  })
}

/**
 * 取消收藏
 *
 * @param {number} productId - 商品 ID
 * @returns {Promise<Result<Void>>}
 *
 * @example
 *   cancelCollect(42)
 */
export function cancelCollect(productId) {
  return request.delete('/user/collect/cancel', {
    params: { productId },
  })
}

/**
 * 我的收藏列表（分页）
 *
 * @param {Object}  params
 * @param {number}  [params.pageNum=1]    - 当前页码
 * @param {number}  [params.pageSize=10]  - 每页条数
 * @returns {Promise<Result<PageResult<CollectVO>>>}
 *
 * @example
 *   getCollectList({ pageNum: 1, pageSize: 10 })
 */
export function getCollectList(params) {
  return request.get('/user/collect/list', { params })
}

/**
 * 查询商品是否已收藏
 *
 * @param {number} productId - 商品 ID
 * @returns {Promise<Result<{collected: boolean}>>}
 *
 * @example
 *   checkCollected(42).then(res => {
 *     if (res.data.collected) { ... }
 *   })
 */
export function checkCollected(productId) {
  return request.get('/user/collect/check', {
    params: { productId },
  })
}
