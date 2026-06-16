<template>
  <div class="login-page">
    <div class="login-card">
      <div class="back-link" @click="router.push('/')">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回首页</span>
      </div>
      <h2 class="card-title">好物集市</h2>

      <el-tabs v-model="activeTab" class="login-tabs">
        <!-- ==================== 密码登录 ==================== -->
        <el-tab-pane label="密码登录" name="password">
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="0" size="large">
            <el-form-item prop="username">
              <el-input v-model="pwdForm.username" placeholder="用户名" prefix-icon="User" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="pwdForm.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
            </el-form-item>
            <el-form-item prop="captchaCode">
              <div class="captcha-row">
                <el-input v-model="pwdForm.captchaCode" placeholder="验证码" prefix-icon="Key" />
                <img
                  v-if="captchaImage"
                  :src="captchaImage"
                  class="captcha-img"
                  @click="refreshCaptcha"
                  title="点击刷新"
                />
                <div v-else class="captcha-img captcha-placeholder" @click="refreshCaptcha">加载中</div>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="login-btn" :loading="loading" @click="handlePwdLogin">
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- ==================== 短信登录 ==================== -->
        <el-tab-pane label="短信登录" name="sms">
          <el-form ref="smsFormRef" :model="smsForm" :rules="smsRules" label-width="0" size="large">
            <el-form-item prop="phone">
              <el-input v-model="smsForm.phone" placeholder="手机号" prefix-icon="Iphone" />
            </el-form-item>
            <el-form-item prop="code">
              <div class="captcha-row">
                <el-input v-model="smsForm.code" placeholder="短信验证码" prefix-icon="Message" />
                <el-button
                  :disabled="smsCooldown > 0"
                  @click="handleSendCode"
                  class="sms-btn"
                >
                  {{ smsCooldown > 0 ? `${smsCooldown}s` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="login-btn" :loading="loading" @click="handleSmsLogin">
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="card-footer">
        <router-link to="/register" class="link">注册账号</router-link>
        <router-link to="/forgot-password" class="link">找回密码</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getCaptcha, sendCode, login as loginBySms, loginByPassword } from '@/api/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeTab = ref('password')
const loading = ref(false)

// ==================== 密码登录 ====================
const pwdFormRef = ref(null)
const pwdForm = reactive({ username: '', password: '', captchaCode: '', captchaToken: '' })
const captchaImage = ref('')
const pwdRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}

async function refreshCaptcha() {
  try {
    const res = await getCaptcha()
    captchaImage.value = res.data.captchaImage
    pwdForm.captchaToken = res.data.captchaToken
    pwdForm.captchaCode = ''
  } catch {
    captchaImage.value = ''
  }
}

async function handlePwdLogin() {
  await pwdFormRef.value.validate()
  loading.value = true
  try {
    const res = await loginByPassword(pwdForm)
    userStore.setLoginInfo(res.data)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/')
  } catch {
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

// ==================== 短信登录 ====================
const smsFormRef = ref(null)
const smsForm = reactive({ phone: '', code: '' })
const smsCooldown = ref(0)
const smsRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' },
  ],
}

async function handleSendCode() {
  if (!smsForm.phone || !/^1[3-9]\d{9}$/.test(smsForm.phone)) {
    ElMessage.warning('请先输入正确的手机号')
    return
  }
  try {
    await sendCode({ phone: smsForm.phone })
    ElMessage.success('验证码已发送')
    smsCooldown.value = 60
    const timer = setInterval(() => {
      smsCooldown.value--
      if (smsCooldown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch {}
}

async function handleSmsLogin() {
  await smsFormRef.value.validate()
  loading.value = true
  try {
    const res = await loginBySms(smsForm)
    userStore.setLoginInfo(res.data)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/')
  } finally {
    loading.value = false
  }
}

onMounted(() => { refreshCaptcha() })
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
.card-title {
  text-align: center;
  font-size: 26px;
  color: #303133;
  margin: 0 0 24px;
}
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
  &:hover {
    color: #fff;
    background: #409eff;
    border-color: #409eff;
  }
}
.login-tabs { :deep(.el-tabs__nav-wrap::after) { display: none; } }
.captcha-row {
  display: flex;
  gap: 12px;
  width: 100%;
  .el-input { flex: 1; }
}
.captcha-img {
  width: 120px;
  height: 40px;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  object-fit: cover;
}
.captcha-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #c0c4cc;
}
.sms-btn { width: 120px; flex-shrink: 0; }
.login-btn { width: 100%; }
.card-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
  .link { font-size: 13px; color: #409eff; text-decoration: none; &:hover { text-decoration: underline; } }
}
</style>
