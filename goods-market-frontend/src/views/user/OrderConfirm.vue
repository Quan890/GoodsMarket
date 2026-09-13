<template>
  <div class="confirm-page">
    <h2 class="page-title">确认订单</h2>
    <el-empty v-if="!loading && cartItems.length === 0" :description="isBuyNow ? '商品不存在或已下架' : '没有选中的商品'">
      <el-button v-if="!isBuyNow" type="primary" @click="router.push({ name: 'UserCart' })">返回购物车</el-button>
      <el-button v-else type="primary" @click="router.push({ name: 'Home' })">去逛逛</el-button>
    </el-empty>
    <template v-else>
      <!-- 收货地址 -->
      <div class="section address-section">
        <div class="section-title"><el-icon><Location /></el-icon>收货地址</div>
        <el-form ref="addressFormRef" :model="addressForm" :rules="addressRules" label-width="80px">
          <el-form-item label="收货人" prop="receiverName">
            <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
          </el-form-item>
          <el-form-item label="手机号" prop="receiverPhone">
            <el-input v-model="addressForm.receiverPhone" placeholder="请输入手机号" maxlength="11" />
          </el-form-item>
          <el-form-item label="地址" prop="receiverAddress">
            <el-input v-model="addressForm.receiverAddress" type="textarea" :rows="2" placeholder="请输入详细收货地址" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="addressForm.remark" placeholder="选填，如配送时间要求" />
          </el-form-item>
        </el-form>
      </div>

      <!-- 商品清单 -->
      <div class="section goods-section">
        <div class="section-title">
          <el-icon><ShoppingCart /></el-icon>商品清单
          <span class="goods-count">共 {{ totalQuantity }} 件</span>
        </div>
        <div class="goods-list">
          <div v-for="item in cartItems" :key="item.productId" class="goods-item">
            <el-image :src="item.productImage" fit="cover" class="goods-image">
              <template #error>
                <div class="image-placeholder"><el-icon size="24"><Picture /></el-icon></div>
              </template>
            </el-image>
            <div class="goods-info">
              <span class="goods-name">{{ item.productName }}</span>
              <span class="goods-qty">x{{ item.quantity }}</span>
            </div>
            <span class="goods-price">￥{{ (Number(item.price) * item.quantity).toFixed(2) }}</span>
          </div>
        </div>
      </div>

      <!-- 结算信息 -->
      <div class="section settle-section">
        <div class="settle-row"><span>商品金额</span><span>￥{{ totalPriceYuan }}</span></div>
        <div class="settle-row"><span>运费</span><span class="free-shipping">免运费</span></div>
        <div class="settle-divider" />
        <div class="settle-row settle-total"><span>应付金额</span><span class="total-price">￥{{ totalPriceYuan }}</span></div>
      </div>

      <!-- 提交栏 -->
      <div class="submit-bar">
        <div class="submit-left">
          <span class="submit-count">共 {{ totalQuantity }} 件</span>
          <span class="submit-price">应付：<strong>￥{{ totalPriceYuan }}</strong></span>
        </div>
        <el-button type="danger" size="large" :loading="submitting" :disabled="cartItems.length === 0" @click="handleSubmit">提交订单</el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location, ShoppingCart, Picture } from '@element-plus/icons-vue'
import { useCartStore } from '@/stores'
import { createOrder } from '@/api/order'
import { getProductDetail } from '@/api/product'
import { isValidPhone } from '@/utils/common'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

/**
 * 两种结算模式：
 *   1. buyNow（?productId=xxx&quantity=n）—— 商品详情"立即购买"，直接结算该商品，不经过购物车
 *   2. 购物车结算 —— 结算购物车中已勾选的商品
 */
const isBuyNow = computed(() => !!route.query.productId)
const buyNowItem = ref(null)
const loading = ref(false)
const submitting = ref(false)

const cartItems = computed(() => (isBuyNow.value ? (buyNowItem.value ? [buyNowItem.value] : []) : cartStore.checkedList))
const totalQuantity = computed(() => cartItems.value.reduce((sum, item) => sum + item.quantity, 0))
const totalPrice = computed(() => cartItems.value.reduce((sum, item) => sum + Number(item.price) * item.quantity, 0))
const totalPriceYuan = computed(() => totalPrice.value.toFixed(2))

// 收货地址表单（后端 createOrder 需要 receiverName/receiverPhone/receiverAddress/items）
const addressFormRef = ref(null)
const addressForm = reactive({ receiverName: '', receiverPhone: '', receiverAddress: '', remark: '' })
const addressRules = {
  receiverName: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { validator: (rule, value, cb) => { isValidPhone(value) ? cb() : cb(new Error('手机号格式不正确')) }, trigger: 'blur' },
  ],
  receiverAddress: [{ required: true, message: '请输入收货地址', trigger: 'blur' }],
}

/** 加载立即购买的商品 */
async function fetchBuyNowProduct() {
  const productId = route.query.productId
  const quantity = Math.max(1, parseInt(route.query.quantity, 10) || 1)
  if (!productId) return
  loading.value = true
  try {
    const detail = await getProductDetail(productId)
    const p = detail.data || {}
    if (p.status !== 1) { buyNowItem.value = null; return }
    buyNowItem.value = {
      productId: p.id,
      productName: p.name,
      productImage: p.mainImage,
      price: p.price,
      quantity: Math.min(quantity, p.stock > 0 ? p.stock : quantity),
    }
  } catch {
    buyNowItem.value = null
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (submitting.value) return
  try { await addressFormRef.value.validate() } catch { return }
  if (cartItems.value.length === 0) { ElMessage.warning('没有选中的商品'); return }

  submitting.value = true
  try {
    // 后端入参：{ receiverName, receiverPhone, receiverAddress, remark, items: [{productId, quantity}] }
    // 跨商家结算时后端会拆单，返回订单号数组
    const res = await createOrder({
      receiverName: addressForm.receiverName,
      receiverPhone: addressForm.receiverPhone,
      receiverAddress: addressForm.receiverAddress,
      remark: addressForm.remark || undefined,
      items: cartItems.value.map((item) => ({ productId: item.productId, quantity: item.quantity })),
    })
    const orderNos = res.data || []
    ElMessage.success(`订单创建成功（${orderNos.length} 个子订单）`)
    // 刷新购物车（下单成功后后端已清除对应条目）
    if (!isBuyNow.value) cartStore.fetchCartList()
    // 跳转收银台：多个订单号用逗号拼接
    router.replace({ name: 'Payment', params: { orderNo: orderNos.join(',') } })
  } catch {} finally { submitting.value = false }
}

onMounted(() => {
  if (isBuyNow.value) {
    fetchBuyNowProduct()
  } else if (cartStore.cartList.length === 0) {
    loading.value = true
    cartStore.fetchCartList().finally(() => { loading.value = false })
  }
})
</script>

<style lang="scss" scoped>
.confirm-page { padding: 20px 0 100px; }
.page-title { margin-bottom: 20px; font-size: 20px; font-weight: 600; color: #303133; }
.section { margin-bottom: 16px; padding: 20px; background: #fff; border-radius: 8px; }
.section-title { display: flex; align-items: center; gap: 8px; margin-bottom: 16px; font-size: 16px; font-weight: 600; color: #303133; }
.goods-count { margin-left: auto; font-size: 13px; font-weight: 400; color: #909399; }
.goods-item { display: flex; align-items: center; gap: 14px; padding: 12px 0; & + .goods-item { border-top: 1px solid #f5f5f5; } }
.goods-image { flex-shrink: 0; width: 72px; height: 72px; border-radius: 4px; overflow: hidden; background: #f5f7fa; }
.image-placeholder { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; color: #c0c4cc; }
.goods-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 6px; }
.goods-name { font-size: 14px; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.goods-qty { font-size: 12px; color: #909399; }
.goods-price { flex-shrink: 0; font-size: 14px; font-weight: 600; color: #303133; }
.settle-row { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px; color: #606266; }
.free-shipping { color: #67c23a; }
.settle-divider { margin: 8px 0; border-top: 1px dashed #ebeef5; }
.settle-total { font-size: 16px; font-weight: 600; color: #303133; }
.total-price { font-size: 22px; font-weight: 700; color: #f56c6c; }
.submit-bar { position: fixed; bottom: 0; left: 0; right: 0; display: flex; align-items: center; justify-content: space-between; padding: 14px 24px; background: #fff; box-shadow: 0 -2px 12px rgba(0,0,0,0.06); z-index: 100; }
.submit-left { display: flex; align-items: center; gap: 16px; }
.submit-count { font-size: 14px; color: #909399; }
.submit-price { font-size: 14px; color: #303133; strong { font-size: 22px; color: #f56c6c; } }
</style>
