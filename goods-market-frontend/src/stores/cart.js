import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getCartList as getCartListApi,
  addToCart as addToCartApi,
  updateCartQuantity as updateCartQuantityApi,
  deleteCartItem as deleteCartItemApi,
} from '@/api/cart'

/**
 * 购物车状态仓库
 *
 * 后端 CartVO 数据结构（价格单位：元，BigDecimal）：
 *   { id, productId, productName, productImage, price, originalPrice,
 *     quantity, checked(0/1), stock, productStatus, subtotal }
 *
 * 后端分页格式（MyBatis-Plus）：{ records: [], total, current, size, pages }
 */
export const useCartStore = defineStore('cart', () => {
  const cartList = ref([])
  const loading = ref(false)
  const CACHE_KEY = 'goods_market_cart_cache'

  // ==================== Getters ====================

  const totalCount = computed(() => cartList.value.reduce((sum, item) => sum + item.quantity, 0))
  const checkedCount = computed(() => cartList.value.filter((item) => item.checked).reduce((sum, item) => sum + item.quantity, 0))

  /** 已勾选商品总价（元），直接使用后端返回的元单位 */
  const checkedTotalPrice = computed(() => {
    return cartList.value
      .filter((item) => item.checked)
      .reduce((sum, item) => sum + Number(item.price) * item.quantity, 0)
  })

  /** 已勾选商品总价显示（保留两位小数） */
  const checkedTotalPriceYuan = computed(() => checkedTotalPrice.value.toFixed(2))

  const isAllChecked = computed(() => cartList.value.length > 0 && cartList.value.every((item) => item.checked))
  const checkedList = computed(() => cartList.value.filter((item) => item.checked))

  // ==================== Actions ====================

  /**
   * 拉取购物车列表
   * 后端返回 MyBatis-Plus 分页：{ records: [], total, ... }
   * 后端 checked 字段是 0/1 整数，转为 boolean
   */
  async function fetchCartList(params) {
    loading.value = true
    try {
      const res = await getCartListApi(params)
      const pageData = res.data || {}
      const list = pageData.records || []
      const cachedCheckedMap = getCheckedMapFromCache()
      cartList.value = list.map((item) => ({
        ...item,
        // 后端 checked 是 0/1，优先用缓存的 boolean 值
        checked: cachedCheckedMap[item.productId] ?? (item.checked === 1 || item.checked === true),
      }))
      saveToCache()
      return { list: cartList.value, total: pageData.total || 0 }
    } catch {
      loadFromCache()
      return { list: cartList.value, total: cartList.value.length }
    } finally {
      loading.value = false
    }
  }

  async function addItem(params) {
    const { productId, quantity = 1 } = params
    const existing = cartList.value.find((item) => item.productId === productId)
    if (existing) {
      await updateQuantity(productId, existing.quantity + quantity)
    } else {
      await addToCartApi({ productId, quantity })
      await fetchCartList()
      ElMessage.success('已加入购物车')
    }
  }

  async function updateQuantity(productId, quantity) {
    const item = cartList.value.find((i) => i.productId === productId)
    if (!item) return
    const newQty = Math.max(1, Math.min(quantity, item.stock))
    const oldQty = item.quantity
    item.quantity = newQty
    saveToCache()
    try {
      await updateCartQuantityApi({ productId, quantity: newQty })
    } catch {
      item.quantity = oldQty; saveToCache(); ElMessage.error('修改数量失败')
    }
  }

  async function removeItem(productId) {
    await deleteCartItemApi(productId)
    cartList.value = cartList.value.filter((i) => i.productId !== productId)
    saveToCache()
    ElMessage.success('已移除')
  }

  async function removeChecked() {
    const ids = checkedList.value.map((i) => i.productId)
    if (ids.length === 0) return
    await Promise.all(ids.map((id) => deleteCartItemApi(id)))
    cartList.value = cartList.value.filter((i) => !i.checked)
    saveToCache()
    ElMessage.success('已清空选中商品')
  }

  function toggleCheck(productId) {
    const item = cartList.value.find((i) => i.productId === productId)
    if (item) { item.checked = !item.checked; saveToCache() }
  }

  function toggleCheckAll() {
    const newState = !isAllChecked.value
    cartList.value.forEach((item) => { item.checked = newState })
    saveToCache()
  }

  // ==================== 缓存 ====================

  function saveToCache() {
    try { localStorage.setItem(CACHE_KEY, JSON.stringify(cartList.value)) } catch {}
  }

  function loadFromCache() {
    try { const c = localStorage.getItem(CACHE_KEY); if (c) cartList.value = JSON.parse(c) } catch { cartList.value = [] }
  }

  function getCheckedMapFromCache() {
    try {
      const c = localStorage.getItem(CACHE_KEY)
      if (c) return Object.fromEntries(JSON.parse(c).map((item) => [item.productId, item.checked]))
    } catch {}
    return {}
  }

  function clearCart() { cartList.value = []; localStorage.removeItem(CACHE_KEY) }

  return {
    cartList, loading,
    totalCount, checkedCount, checkedTotalPrice, checkedTotalPriceYuan, isAllChecked, checkedList,
    fetchCartList, addItem, updateQuantity, removeItem, removeChecked, toggleCheck, toggleCheckAll,
    saveToCache, loadFromCache, clearCart,
  }
})
