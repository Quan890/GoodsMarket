import request from '@/utils/request'

/**
 * 订单相关接口
 *
 * 对接后端 OrderController（/user/order + /pay）
 *
 * 订单状态（与数据库 order.status 一致）：
 *   0=待支付 1=已支付(待发货) 2=已取消 3=已完成 4=已发货(待收货)
 *
 * 接口一览：
 *   POST  /user/order/create            → 创建订单（跨商家返回多个订单号）
 *   GET   /user/order/list              → 我的订单列表（分页）
 *   GET   /user/order/detail/{orderNo}  → 订单详情
 *   PUT   /user/order/cancel/{orderNo}  → 取消订单
 *   PUT   /user/order/receipt/{orderNo} → 确认收货
 *   GET   /user/order/pay-result/{orderNo} → 查询支付结果
 *   POST  /user/order/mock-pay          → 模拟支付（开发调试用）
 *   POST  /user/order/wx-pay/{orderNo}  → 唤起微信支付
 */

/**
 * 创建订单
 *
 * 后端会按商家拆单：跨商家结算一次会生成多个子订单
 *
 * @param {Object}    data
 * @param {string}    data.receiverName    - 收货人姓名
 * @param {string}    data.receiverPhone   - 收货人电话
 * @param {string}    data.receiverAddress - 收货地址
 * @param {string}    [data.remark]        - 订单备注
 * @param {Array}     data.items           - 商品明细列表
 * @param {number|string} data.items[].productId - 商品 ID
 * @param {number}    data.items[].quantity  - 购买数量
 * @returns {Promise<Result<string[]>>} 返回订单号列表
 *
 * @example
 *   const res = await createOrder({...})
 *   // res.data => ['GM202609...', 'GM202609...']（单商家为1个元素）
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
 * @param {number}  [params.status]       - 订单状态筛选（0待支付 1已支付 2已取消 3已完成 4已发货）
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
 *   getOrderDetail('GM2026061415301200000158')
 */
export function getOrderDetail(orderNo) {
  return request.get(`/user/order/detail/${orderNo}`)
}

/**
 * 取消订单（仅待支付状态可取消，取消后回滚库存）
 *
 * @param {string} orderNo - 订单号
 * @returns {Promise<Result<Void>>}
 */
export function cancelOrder(orderNo) {
  return request.put(`/user/order/cancel/${orderNo}`)
}

/**
 * 确认收货（仅已发货状态可操作，确认后订单变为已完成）
 *
 * @param {string} orderNo - 订单号
 * @returns {Promise<Result<Void>>}
 */
export function confirmReceipt(orderNo) {
  return request.put(`/user/order/receipt/${orderNo}`)
}

/**
 * 模拟支付（开发调试用，生产环境应下线）
 *
 * @param {Object}  data
 * @param {string}  data.orderNo   - 订单号
 * @param {number}  data.payMethod - 支付方式（1支付宝 2微信）
 * @returns {Promise<Result<Void>>}
 */
export function mockPay(data) {
  return request.post('/user/order/mock-pay', data)
}

/**
 * 唤起微信支付（需后端配置真实商户参数）
 *
 * 调用后端生成微信支付 Native 预付单，返回 codeUrl 供扫码支付
 *
 * @param {string} orderNo - 订单号
 * @returns {Promise<Result<{codeUrl: string, orderNo: string, payAmount: number}>>}
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
 * @returns {Promise<Result<{orderNo: string, status: number, statusDesc: string, paid: boolean}>>}
 */
export function getPayResult(orderNo) {
  return request.get(`/user/order/pay-result/${orderNo}`)
}
