import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
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
 *
 * 缓存按用户隔离（key 携带 userId），避免公共电脑/切换账号时
 * 把上一个用户的购物车缓存展示给当前用户
 */
export const useCartStore = defineStore('cart', () => {
  const cartList = ref([])
  const loading = ref(false)

  // ==================== Getters ====================

  /** 商品是否可正常结算（未下架） */
  const isSaleable = (item) => item.productStatus !== 0

  const totalCount = computed(() => cartList.value.reduce((sum, item) => sum + item.quantity, 0))
  const checkedCount = computed(() => checkedList.value.reduce((sum, item) => sum + item.quantity, 0))

  /** 已勾选商品总价（元），直接使用后端返回的元单位 */
  const checkedTotalPrice = computed(() => {
    return checkedList.value
      .reduce((sum, item) => sum + Number(item.price) * item.quantity, 0)
  })

  /** 已勾选商品总价显示（保留两位小数） */
  const checkedTotalPriceYuan = computed(() => checkedTotalPrice.value.toFixed(2))

  /** 已勾选且可结算的商品（下架商品不可结算） */
  const checkedList = computed(() => cartList.value.filter((item) => item.checked && isSaleable(item)))

  const isAllChecked = computed(() => {
    const saleable = cartList.value.filter(isSaleable)
    return saleable.length > 0 && saleable.every((item) => item.checked)
  })

  // ==================== 缓存 Key ====================

  /** 当前用户的缓存 Key（未登录时返回 null，不读写缓存） */
  function getCacheKey() {
    const userStore = useUserStore()
    return userStore.userId ? `goods_market_cart_cache_${userStore.userId}` : null
  }

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
        // 后端 checked 是 0/1，优先用本地缓存的勾选状态
        checked: cachedCheckedMap[item.productId] ?? (item.checked === 1 || item.checked === true),
      }))
      saveToCache()
      return { list: cartList.value, total: pageData.total || 0 }
    } catch (e) {
      // 仅在当前列表为空时回退缓存，避免覆盖已加载的数据
      if (cartList.value.length === 0) loadFromCache()
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
      ElMessage.success('已加入购物车（数量已累加）')
    } else {
      await addToCartApi({ productId, quantity })
      await fetchCartList()
      ElMessage.success('已加入购物车')
    }
  }

  async function updateQuantity(productId, quantity) {
    const item = cartList.value.find((i) => i.productId === productId)
    if (!item) return
    // 后端未返回 stock 时退化为不设上限（仅靠后端校验兜底）
    const maxQty = Number.isFinite(Number(item.stock)) && item.stock > 0 ? item.stock : quantity
    const newQty = Math.max(1, Math.min(quantity, maxQty))
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
    try {
      await Promise.all(ids.map((id) => deleteCartItemApi(id)))
      cartList.value = cartList.value.filter((i) => !i.checked)
      saveToCache()
      ElMessage.success('已清空选中商品')
    } finally {
      // 无论部分成功与否，都以后端数据为准刷新一次
      fetchCartList()
    }
  }

  function toggleCheck(productId) {
    const item = cartList.value.find((i) => i.productId === productId)
    if (item) { item.checked = !item.checked; saveToCache() }
  }

  function toggleCheckAll() {
    const newState = !isAllChecked.value
    // 全选只作用于可结算商品（下架商品不可勾选）
    cartList.value.forEach((item) => { if (isSaleable(item)) item.checked = newState })
    saveToCache()
  }

  // ==================== 缓存 ====================

  function saveToCache() {
    const key = getCacheKey()
    if (!key) return
    try { localStorage.setItem(key, JSON.stringify(cartList.value)) } catch {}
  }

  function loadFromCache() {
    const key = getCacheKey()
    if (!key) { cartList.value = []; return }
    try { const c = localStorage.getItem(key); if (c) cartList.value = JSON.parse(c) } catch { cartList.value = [] }
  }

  function getCheckedMapFromCache() {
    const key = getCacheKey()
    if (!key) return {}
    try {
      const c = localStorage.getItem(key)
      if (c) return Object.fromEntries(JSON.parse(c).map((item) => [item.productId, item.checked]))
    } catch {}
    return {}
  }

  /** 清空购物车（退出登录/切换账号时调用，同时清除该用户的本地缓存） */
  function clearCart() {
    cartList.value = []
    const key = getCacheKey()
    if (key) localStorage.removeItem(key)
  }

  return {
    cartList, loading,
    totalCount, checkedCount, checkedTotalPrice, checkedTotalPriceYuan, isAllChecked, checkedList,
    fetchCartList, addItem, updateQuantity, removeItem, removeChecked, toggleCheck, toggleCheckAll,
    saveToCache, loadFromCache, clearCart,
  }
})
