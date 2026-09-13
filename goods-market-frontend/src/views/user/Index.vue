<template>
  <div class="user-center">
    <!-- 用户信息卡片 -->
    <el-card class="info-card">
      <template #header>
        <span>账户信息</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户ID">{{ userStore.userId }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ userStore.nickname }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="roleTagType">{{ roleLabel }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Token状态">
          <el-tag type="success">已登录</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 快捷操作 -->
    <el-card class="action-card">
      <template #header>
        <span>快捷操作</span>
      </template>
      <div class="action-grid">
        <el-button @click="$router.push('/user/collect')">我的收藏</el-button>
        <el-button @click="$router.push('/user/cart')">购物车</el-button>
        <el-button @click="$router.push('/user/order')">我的订单</el-button>
        <el-button @click="showPasswordDialog = true">修改密码</el-button>
      </div>
    </el-card>

    <!-- 危险操作 -->
    <el-card class="danger-card">
      <template #header>
        <span style="color: #f56c6c">危险操作</span>
      </template>
      <div class="danger-zone">
        <div class="danger-info">
          <h4>注销账户</h4>
          <p>账户注销后将无法恢复，请谨慎操作。</p>
          <p v-if="userStore.role === 3" class="warning-text">管理员账号不允许注销</p>
          <p v-if="userStore.role === 2" class="warning-text">商家注销前需先删除所有商品</p>
        </div>
        <el-button
          type="danger"
          :disabled="userStore.role === 3"
          @click="showDeleteDialog = true"
        >注销账户</el-button>
      </div>
    </el-card>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="420px" :close-on-click-modal="false" @closed="resetPasswordForm">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="6-20位新密码" show-password maxlength="20" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="再次输入新密码" show-password maxlength="20" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- 注销确认弹窗 -->
    <el-dialog v-model="showDeleteDialog" title="注销账户" width="420px" :close-on-click-modal="false">
      <el-alert type="error" :closable="false" style="margin-bottom: 16px">
        此操作不可逆，注销后您的所有数据将被删除！
      </el-alert>
      <el-form ref="deleteFormRef" :model="deleteForm" :rules="deleteRules">
        <el-form-item label="请输入密码确认" prop="password">
          <el-input
            v-model="deleteForm.password"
            type="password"
            placeholder="请输入登录密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDeleteDialog = false">取消</el-button>
        <el-button type="danger" :loading="deleteLoading" @click="handleDeleteAccount">
          确认注销
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore, useCartStore } from '@/stores'
import { deleteAccount, changePassword } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const roleLabel = computed(() => {
  const map = { 0: '游客', 1: '普通用户', 2: '商家', 3: '管理员' }
  return map[userStore.role] || '未知'
})
const roleTagType = computed(() => {
  const map = { 0: 'info', 1: '', 2: 'warning', 3: 'danger' }
  return map[userStore.role] || 'info'
})

// ==================== 修改密码 ====================
const showPasswordDialog = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref(null)
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '新密码长度需在6-20位之间', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, cb) => {
        value === passwordForm.newPassword ? cb() : cb(new Error('两次输入的密码不一致'))
      },
      trigger: 'blur',
    },
  ],
}

function resetPasswordForm() {
  passwordFormRef.value?.resetFields()
}

async function handleChangePassword() {
  if (passwordLoading.value) return
  let valid = false
  try { await passwordFormRef.value.validate(); valid = true } catch { return }
  if (!valid) return
  passwordLoading.value = true
  try {
    await changePassword({ oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    showPasswordDialog.value = false
    userStore.clearUserInfo()
    cartStore.clearCart()
    router.push({ name: 'Login' })
  } catch {} finally {
    passwordLoading.value = false
  }
}

// ==================== 注销账户 ====================
const showDeleteDialog = ref(false)
const deleteLoading = ref(false)
const deleteFormRef = ref(null)
const deleteForm = reactive({ password: '' })
const deleteRules = {
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleDeleteAccount() {
  try { await deleteFormRef.value.validate() } catch { return }
  try {
    await ElMessageBox.confirm('确定要注销账户吗？此操作不可逆！', '最终确认', {
      confirmButtonText: '确定注销',
      cancelButtonText: '取消',
      type: 'warning',
    })
    deleteLoading.value = true
    await deleteAccount({ password: deleteForm.password })
    ElMessage.success('账户已注销')
    userStore.clearUserInfo()
    cartStore.clearCart()
    router.push('/')
  } catch {} finally {
    deleteLoading.value = false
    showDeleteDialog.value = false
  }
}
</script>

<style lang="scss" scoped>
.user-center {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.info-card, .action-card, .danger-card { border-radius: 8px; }
.action-grid { display: flex; gap: 12px; flex-wrap: wrap; }
.danger-zone {
  display: flex;
  align-items: center;
  justify-content: space-between;
  h4 { margin: 0 0 4px; color: #303133; }
  p { margin: 0; font-size: 13px; color: #909399; }
  .warning-text { color: #e6a23c; margin-top: 4px; }
}
</style>
