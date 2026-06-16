import request from '@/utils/request'

/**
 * 订单相关接口
 *
 * 对接后端 OrderController（/user/order + /pay）
 *
 * 接口一览：
 *   POST  /user/order/create          → 创建订单
 *   GET   /user/order/list            → 我的订单列表（分页）
 *   GET   /user/order/detail/{orderNo} → 订单详情
 *   PUT   /user/order/cancel/{orderNo} → 取消订单
 *   POST  /user/order/mock-pay        → 模拟支付（开发调试用）
 *   POST  /user/order/wx-pay/{orderNo} → 唤起微信支付
 *   GET   /user/order/pay-result       → 查询支付结果
 */

/**
 * 创建订单
 *
 * @param {Object}    data
 * @param {string}    data.receiverName    - 收货人姓名
 * @param {string}    data.receiverPhone   - 收货人电话
 * @param {string}    data.receiverAddress - 收货地址
 * @param {string}    [data.remark]        - 订单备注
 * @param {Array}     data.items           - 商品明细列表
 * @param {number}    data.items[].productId - 商品 ID
 * @param {number}    data.items[].quantity  - 购买数量
 * @returns {Promise<Result<string>>} 返回订单号 orderNo
 *
 * @example
 *   createOrder({
 *     receiverName: '张三', receiverPhone: '13800138000', receiverAddress: '北京市朝阳区xxx',
 *     items: [{ productId: 1, quantity: 2 }]
 *   })
 */
export function createOrder(data) {
  return request.post('/user/order/create', data)
}

/**
 * 我的订单列表
 *
 * @param {Object}  params
 * @param {number}  [params.pageNum=1]    - 当前页码
 * @param {number}  [params.pageSize=10]  - 每页条数
 * @param {number}  [params.status]       - 订单状态筛选（0待付款 1已付款 2已发货 3已完成 4已取消）
 * @returns {Promise<Result<PageResult<OrderVO>>>}
 *
 * @example
 *   getOrderList({ pageNum: 1, pageSize: 10, status: 0 })
 */
export function getOrderList(params) {
  return request.get('/user/order/list', { params })
}

/**
 * 订单详情
 *
 * @param {string} orderNo - 订单号
 * @returns {Promise<Result<OrderVO>>}
 *
 * @example
 *   getOrderDetail('202606151234567890')
 */
export function getOrderDetail(orderNo) {
  return request.get(`/user/order/detail/${orderNo}`)
}

/**
 * 取消订单
 *
 * @param {string} orderNo - 订单号
 * @returns {Promise<Result<Void>>}
 *
 * @example
 *   cancelOrder('202606151234567890')
 */
export function cancelOrder(orderNo) {
  return request.put(`/user/order/cancel/${orderNo}`)
}

/**
 * 模拟支付（开发调试用，生产环境隐藏）
 *
 * @param {Object}  data
 * @param {string}  data.orderNo   - 订单号
 * @param {number}  data.payMethod - 支付方式（1支付宝 2微信）
 * @returns {Promise<Result<Void>>}
 *
 * @example
 *   mockPay({ orderNo: '202606151234567890', payMethod: 2 })
 */
export function mockPay(data) {
  return request.post('/user/order/mock-pay', data)
}

/**
 * 唤起微信支付
 *
 * 调用后端生成微信支付预付单，返回前端调起支付所需的参数
 *
 * @param {string} orderNo - 订单号
 * @returns {Promise<Result<{appId, timeStamp, nonceStr, signType, paySign, prepayId}>>}
 *
 * @example
 *   createWxPayOrder('202606151234567890').then(res => {
 *     // 使用 res.data 调起微信支付 SDK
 *   })
 */
export function createWxPayOrder(orderNo) {
  return request.post(`/user/order/wx-pay/${orderNo}`)
}

/**
 * 查询支付结果
 *
 * 前端轮询此接口，确认支付是否成功
 *
 * @param {string} orderNo - 订单号
 * @returns {Promise<Result<{paid: boolean, status: number}>>}
 *
 * @example
 *   getPayResult('202606151234567890').then(res => {
 *     if (res.data.paid) { /* 跳转支付成功页 *​/ }
 *   })
 */
export function getPayResult(orderNo) {
  return request.get(`/user/order/pay-result/${orderNo}`)
}
