<template>
  <div class="login-page">
    <div class="login-card">
      <div class="back-link" @click="router.push('/login')">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回登录</span>
      </div>
      <h2 class="card-title">注册账号</h2>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号" prefix-icon="Iphone" />
        </el-form-item>
        <el-form-item prop="code">
          <div class="captcha-row">
            <el-input v-model="form.code" placeholder="短信验证码" prefix-icon="Message" />
            <el-button :disabled="cooldown > 0" @click="handleSendCode" class="sms-btn">
              {{ cooldown > 0 ? `${cooldown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名（3-20位）" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（6-20位）" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="submit-btn" :loading="loading" @click="handleRegister">
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="card-footer">
        <router-link to="/login" class="link">已有账号？去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { sendCode, register } from '@/api/user'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const cooldown = ref(0)

const form = reactive({ phone: '', code: '', username: '', password: '', confirmPassword: '' })

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) callback(new Error('两次密码不一致'))
  else callback()
}

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20位', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' },
  ],
}

async function handleSendCode() {
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请先输入正确的手机号')
    return
  }
  try {
    await sendCode({ phone: form.phone })
    ElMessage.success('验证码已发送')
    cooldown.value = 60
    const timer = setInterval(() => { cooldown.value--; if (cooldown.value <= 0) clearInterval(timer) }, 1000)
  } catch {}
}

async function handleRegister() {
  await formRef.value.validate()
  loading.value = true
  try {
    await register({
      phone: form.phone,
      code: form.code,
      username: form.username,
      password: form.password,
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 420px;
  background: #fff;
  border-radius: 12px;
  padding: 40px 36px 30px;
  box-shadow: 0 12px 40px rgba(0,0,0,0.2);
}
.card-title { text-align: center; font-size: 26px; color: #303133; margin: 0 0 24px; }
.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 20px;
  padding: 8px 18px;
  font-size: 14px;
  font-weight: 500;
  color: #409eff;
  background: #ecf5ff;
  border: 1px solid #b3d8ff;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  &:hover { color: #fff; background: #409eff; border-color: #409eff; }
}
.captcha-row { display: flex; gap: 12px; width: 100%; .el-input { flex: 1; } }
.sms-btn { width: 120px; flex-shrink: 0; }
.submit-btn { width: 100%; }
.card-footer { text-align: center; margin-top: 16px; .link { font-size: 13px; color: #409eff; text-decoration: none; &:hover { text-decoration: underline; } } }
</style>
