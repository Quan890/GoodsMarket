/**
 * 通用工具函数库
 *
 * 包含项目中高频复用的纯函数，按职责分组：
 *   1. 正则校验（手机号等）
 *   2. 验证码倒计时
 *   3. 金额格式化
 *   4. 通用格式化
 */
import { ref } from 'vue'

// ==================== 正则校验 ====================

/**
 * 手机号正则校验（中国大陆）
 *
 * 规则：1 开头，第二位 3-9，后接 9 位数字
 * 覆盖三大运营商 + 虚拟运营商号段
 *
 * @param {string} phone - 待校验的手机号
 * @returns {boolean} 是否合法
 *
 * @example
 *   isValidPhone('13800138000')  // true
 *   isValidPhone('12345678901')  // false
 *   isValidPhone('1380013800')   // false（少一位）
 */
export function isValidPhone(phone) {
  return /^1[3-9]\d{9}$/.test(phone)
}

/**
 * 邮箱格式校验
 *
 * @param {string} email - 待校验的邮箱
 * @returns {boolean} 是否合法
 */
export function isValidEmail(email) {
  return /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(email)
}

/**
 * 身份证号简单校验（18 位，末位可为 X）
 *
 * @param {string} idCard - 待校验的身份证号
 * @returns {boolean} 是否合法
 */
export function isValidIdCard(idCard) {
  return /^[1-9]\d{5}(19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/.test(idCard)
}

// ==================== 验证码倒计时 ====================

/**
 * 验证码倒计时控制器
 *
 * 使用方式：
 *   const countdown = useCountdown(60, (sec) => { remaining.value = sec })
 *   countdown.start()  // 开始倒计时
 *   countdown.reset()  // 手动重置（如验证码发送失败时）
 *
 * @param {number}   seconds    - 倒计时总秒数，默认 60
 * @param {Function} onTick     - 每秒回调，参数为剩余秒数
 * @returns {{ start: Function, reset: Function, isCounting: import('vue').Ref<boolean>, remaining: import('vue').Ref<number> }}
 */
export function useCountdown(seconds = 60, onTick) {
  const remaining = ref(0)
  const isCounting = ref(false)
  let timer = null

  /**
   * 启动倒计时
   * 重复调用不会重叠计时器
   */
  function start() {
    if (isCounting.value) return

    remaining.value = seconds
    isCounting.value = true
    onTick?.(remaining.value)

    timer = setInterval(() => {
      remaining.value--
      onTick?.(remaining.value)

      if (remaining.value <= 0) {
        reset()
      }
    }, 1000)
  }

  /**
   * 重置倒计时状态
   * 清除定时器，恢复可点击状态
   */
  function reset() {
    clearInterval(timer)
    timer = null
    remaining.value = 0
    isCounting.value = false
  }

  return { start, reset, isCounting, remaining }
}

// ==================== 金额格式化 ====================

/**
 * 分转元 —— 将后端返回的整数「分」转换为带两位小数的「元」字符串
 *
 * 后端数据库通常以「分」为单位存储金额（避免浮点精度问题），
 * 前端展示时调用此函数转换。
 *
 * @param {number|string} fen - 以分为单位的金额（如 19900 → "199.00"）
 * @returns {string} 格式化后的金额字符串，保留两位小数
 *
 * @example
 *   fenToYuan(0)       // "0.00"
 *   fenToYuan(100)     // "1.00"
 *   fenToYuan(19900)   // "199.00"
 *   fenToYuan('12345') // "123.45"
 */
export function fenToYuan(fen) {
  const num = Number(fen) || 0
  return (num / 100).toFixed(2)
}

/**
 * 元转分 —— 将前端输入的「元」金额转为整数「分」提交给后端
 *
 * @param {number|string} yuan - 以元为单位的金额（如 "199.00" → 19900）
 * @returns {number} 以分为单位的整数
 *
 * @example
 *   yuanToFen('199.00') // 19900
 *   yuanToFen('1.05')   // 105
 *   yuanToFen('0.1')    // 10
 */
export function yuanToFen(yuan) {
  return Math.round(Number(yuan) * 100) || 0
}

/**
 * 金额格式化（带千分位分隔符）
 *
 * @param {number|string} amount - 金额数值
 * @param {number}  decimals    - 小数位数，默认 2
 * @param {string}  prefix      - 前缀，默认 "￥"
 * @returns {string} 格式化后的金额
 *
 * @example
 *   formatPrice(19900)       // "￥199.00"
 *   formatPrice(19900, 2, '$') // "$199.00"
 *   formatPrice(123456)      // "￥1,234.56"
 */
export function formatPrice(amount, decimals = 2, prefix = '￥') {
  const num = Number(amount) || 0
  const fixed = (num / 100).toFixed(decimals)
  // 整数部分添加千分位
  const [intPart, decPart] = fixed.split('.')
  const formatted = intPart.replace(/\B(?=(\d{3})+(?!\d))/g, ',')
  return `${prefix}${decPart !== undefined ? `${formatted}.${decPart}` : formatted}`
}

// ==================== 通用格式化 ====================

/**
 * 日期时间格式化
 *
 * @param {Date|string|number} date   - 日期对象、时间戳或日期字符串
 * @param {string}              format - 格式模板，默认 "YYYY-MM-DD HH:mm:ss"
 * @returns {string} 格式化后的日期字符串
 *
 * @example
 *   formatDate(new Date())               // "2026-06-15 14:30:00"
 *   formatDate(1718437800000)            // "2026-06-15 14:30:00"
 *   formatDate('2026-06-15', 'YYYY-MM-DD') // "2026-06-15"
 */
export function formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''

  const pad = (n) => String(n).padStart(2, '0')

  const map = {
    YYYY: d.getFullYear(),
    MM: pad(d.getMonth() + 1),
    DD: pad(d.getDate()),
    HH: pad(d.getHours()),
    mm: pad(d.getMinutes()),
    ss: pad(d.getSeconds()),
  }

  return Object.entries(map).reduce(
    (str, [token, value]) => str.replace(token, value),
    format
  )
}

/**
 * 手机号脱敏显示
 *
 * @param {string} phone - 手机号
 * @returns {string} 脱敏后的手机号（如 138****8000）
 *
 * @example
 *   maskPhone('13800138000') // "138****8000"
 */
export function maskPhone(phone) {
  if (!phone || phone.length < 7) return phone || ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 文本截断（超出长度用省略号替代）
 *
 * @param {string}  text   - 原始文本
 * @param {number}  maxLen - 最大长度，默认 50
 * @returns {string} 截断后的文本
 */
export function truncate(text, maxLen = 50) {
  if (!text) return ''
  return text.length > maxLen ? text.slice(0, maxLen) + '...' : text
}

/**
 * URL 参数解析
 *
 * @param {string} url - 完整 URL 或查询字符串
 * @returns {Object} 参数键值对
 *
 * @example
 *   parseQuery('?page=1&size=10') // { page: '1', size: '10' }
 */
export function parseQuery(url) {
  const search = url.includes('?') ? url.split('?')[1] : url
  if (!search) return {}
  return Object.fromEntries(new URLSearchParams(search))
}
