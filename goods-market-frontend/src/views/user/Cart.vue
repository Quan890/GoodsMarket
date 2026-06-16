<template>
  <div class="cart-page">
    <h2 class="page-title">购物车</h2>
    <div v-if="cartStore.loading" v-loading="true" style="min-height: 200px" />
    <el-empty v-else-if="cartStore.cartList.length === 0" description="购物车空空如也">
      <el-button type="primary" @click="router.push({ name: 'Home' })">去逛逛</el-button>
    </el-empty>
    <template v-else>
      <div class="cart-header">
        <el-checkbox :model-value="cartStore.isAllChecked" @change="cartStore.toggleCheckAll()">全选</el-checkbox>
        <span class="col-info">商品信息</span>
        <span class="col-price">单价</span>
        <span class="col-qty">数量</span>
        <span class="col-total">小计</span>
        <span class="col-action">操作</span>
      </div>
      <div v-for="item in cartStore.cartList" :key="item.productId" class="cart-item">
        <el-checkbox :model-value="item.checked" @change="cartStore.toggleCheck(item.productId)" />
        <div class="item-info" @click="goDetail(item.productId)">
          <el-image :src="item.productImage" fit="cover" class="item-image">
            <template #error>
              <div class="image-placeholder"><el-icon size="24"><Picture /></el-icon></div>
            </template>
          </el-image>
          <span class="item-name">{{ item.productName }}</span>
        </div>
        <span class="item-price">￥{{ item.price }}</span>
        <div class="item-qty">
          <el-input-number :model-value="item.quantity" :min="1" :max="item.stock" size="small" @change="(val) => handleQuantityChange(item.productId, val)" />
        </div>
        <span class="item-total">￥{{ (Number(item.price) * item.quantity).toFixed(2) }}</span>
        <el-button type="danger" link @click="handleRemove(item.productId)">删除</el-button>
      </div>
      <div class="cart-footer">
        <div class="footer-left">
          <el-checkbox :model-value="cartStore.isAllChecked" @change="cartStore.toggleCheckAll()">全选</el-checkbox>
          <el-button type="danger" link :disabled="cartStore.checkedCount === 0" @click="handleRemoveChecked">删除选中</el-button>
        </div>
        <div class="footer-right">
          <span class="footer-count">已选 <strong>{{ cartStore.checkedCount }}</strong> 件</span>
          <span class="footer-price">合计：<strong>￥{{ cartStore.checkedTotalPriceYuan }}</strong></span>
          <el-button type="danger" size="large" :disabled="cartStore.checkedCount === 0" @click="handleCheckout">去结算</el-button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { useCartStore } from '@/stores'

const router = useRouter()
const cartStore = useCartStore()

onMounted(() => { cartStore.fetchCartList() })

function handleQuantityChange(productId, val) {
  if (val === null || val === undefined) return
  cartStore.updateQuantity(productId, val)
}

async function handleRemove(productId) {
  try {
    await ElMessageBox.confirm('确定将该商品从购物车中移除？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await cartStore.removeItem(productId)
  } catch {}
}

async function handleRemoveChecked() {
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${cartStore.checkedCount} 件商品？`, '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await cartStore.removeChecked()
  } catch {}
}

function handleCheckout() { router.push({ name: 'OrderConfirm' }) }
function goDetail(productId) { router.push({ name: 'ProductDetail', params: { id: productId } }) }
</script>

<style lang="scss" scoped>
.cart-page { padding: 20px 0; }
.page-title { margin-bottom: 20px; font-size: 20px; font-weight: 600; color: #303133; }
.cart-header { display: flex; align-items: center; padding: 12px 16px; margin-bottom: 2px; background: #f5f7fa; border-radius: 4px; font-size: 13px; color: #909399; .col-info { flex: 1; margin-left: 16px; } .col-price { width: 100px; text-align: center; } .col-qty { width: 140px; text-align: center; } .col-total { width: 100px; text-align: center; } .col-action { width: 60px; text-align: center; } }
.cart-item { display: flex; align-items: center; padding: 16px; margin-bottom: 2px; background: #fff; border-radius: 4px; transition: background 0.2s; &:hover { background: #fafafa; } }
.item-info { flex: 1; display: flex; align-items: center; gap: 12px; margin-left: 16px; cursor: pointer; min-width: 0; }
.item-image { flex-shrink: 0; width: 80px; height: 80px; border-radius: 4px; overflow: hidden; background: #f5f7fa; }
.image-placeholder { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; color: #c0c4cc; }
.item-name { font-size: 14px; color: #303133; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.item-price { width: 100px; text-align: center; font-size: 14px; color: #606266; }
.item-qty { width: 140px; display: flex; justify-content: center; }
.item-total { width: 100px; text-align: center; font-size: 14px; font-weight: 600; color: #f56c6c; }
.cart-footer { display: flex; align-items: center; justify-content: space-between; position: sticky; bottom: 0; margin-top: 16px; padding: 16px 20px; background: #fff; border-radius: 4px; box-shadow: 0 -2px 12px rgba(0,0,0,0.06); }
.footer-left { display: flex; align-items: center; gap: 16px; }
.footer-right { display: flex; align-items: center; gap: 20px; }
.footer-count { font-size: 14px; color: #606266; strong { color: #f56c6c; } }
.footer-price { font-size: 14px; color: #303133; strong { font-size: 20px; color: #f56c6c; } }
</style>
