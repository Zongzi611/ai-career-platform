<template>
  <div class="reg-page">
    <div class="bg-particles">
      <div v-for="i in 15" :key="i" class="particle" :style="particleStyle(i)" />
    </div>

    <div class="reg-card">
      <div class="logo-area">
        <div class="logo-icon">🎓</div>
        <h1>CareerSail</h1>
        <p>创建你的账号</p>
      </div>

      <!-- Role Selection -->
      <div class="role-title">选择身份</div>
      <div class="role-selector">
        <div class="role-card" :class="{ active: form.role === 'student' }" @click="form.role = 'student'">
          <span class="role-icon">🎒</span>
          <span class="role-name">学生</span>
        </div>
        <div class="role-card" :class="{ active: form.role === 'teacher' }" @click="form.role = 'teacher'">
          <span class="role-icon">👩‍🏫</span>
          <span class="role-name">教师</span>
        </div>
      </div>

      <div class="form-area">
        <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
          <el-row :gutter="12">
            <el-col :span="12">
              <el-form-item prop="username"><el-input v-model="form.username" placeholder="用户名" size="large" /></el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item prop="password"><el-input v-model="form.password" type="password" placeholder="密码(6-32位)" size="large" show-password /></el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="12">
            <el-col :span="12">
              <el-form-item prop="realName"><el-input v-model="form.realName" placeholder="真实姓名" size="large" /></el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item><el-input v-model="form.email" placeholder="邮箱(选填)" size="large" /></el-form-item>
            </el-col>
          </el-row>
          <!-- Student-specific fields -->
          <template v-if="form.role === 'student'">
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item><el-input v-model="form.college" placeholder="学院(如计算机学院)" size="large" /></el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item><el-input v-model="form.major" placeholder="专业" size="large" /></el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item><el-input v-model="form.grade" placeholder="年级(如2024级)" size="large" /></el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item><el-input v-model="form.className" placeholder="班级" size="large" /></el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="12">
              <el-col :span="12">
                <el-form-item><el-input v-model="form.className" placeholder="班级" size="large" /></el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item><el-input v-model="form.studentNo" placeholder="学号" size="large" /></el-form-item>
              </el-col>
            </el-row>
          </template>
          <el-button type="primary" size="large" :loading="loading" @click="handleRegister" class="reg-btn">注册</el-button>
        </el-form>
      </div>
      <p class="login-link">已有账号？<router-link to="/login">立即登录</router-link></p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username:'',password:'',realName:'',email:'',major:'',grade:'',className:'',studentNo:'',role:'student' })

const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, min: 6, message: '密码至少6位' }],
  realName: [{ required: true, message: '请输入姓名' }]
}

const particleStyle = (i) => ({
  left: `${Math.random() * 100}%`, top: `${Math.random() * 100}%`,
  animationDelay: `${Math.random() * 5}s`, animationDuration: `${3 + Math.random() * 4}s`,
  opacity: 0.15 + Math.random() * 0.3, width: `${2 + Math.random() * 4}px`, height: `${2 + Math.random() * 4}px`,
})

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    const msg = e?.response?.data?.message || '注册失败，请稍后重试'
    ElMessage.error(msg)
  } finally { loading.value = false }
}
</script>

<style scoped>
.reg-page { display:flex; justify-content:center; align-items:center; min-height:100vh; background:#0a0e27; position:relative; overflow:hidden; }
.bg-particles { position:absolute; inset:0; pointer-events:none; }
.particle { position:absolute; border-radius:50%; background:linear-gradient(135deg,#667eea,#00d4ff); animation:float-up linear infinite; }
@keyframes float-up { 0%{transform:translateY(100vh) scale(0)} 50%{transform:translateY(50vh) scale(1)} 100%{transform:translateY(-10vh) scale(0)} }

.reg-card { position:relative; z-index:1; width:560px; background:rgba(16,20,45,0.85); backdrop-filter:blur(20px); border:1px solid rgba(102,126,234,0.2); border-radius:20px; padding:32px 36px; box-shadow:0 0 60px rgba(102,126,234,0.15); }
.logo-area { text-align:center; margin-bottom:24px; }
.logo-icon { font-size:40px; margin-bottom:6px; }
.logo-area h1 { margin:0; font-size:24px; font-weight:700; background:linear-gradient(135deg,#667eea,#00d4ff); -webkit-background-clip:text; -webkit-text-fill-color:transparent; background-clip:text; }
.logo-area p { margin:4px 0 0; font-size:13px; color:rgba(255,255,255,0.45); }

.role-title { text-align:center; font-size:12px; color:rgba(255,255,255,0.4); margin-bottom:10px; letter-spacing:2px; text-transform:uppercase; }
.role-selector { display:flex; gap:10px; justify-content:center; margin-bottom:20px; }
.role-card { flex:1; max-width:160px; background:rgba(255,255,255,0.03); border:1px solid rgba(255,255,255,0.08); border-radius:12px; padding:14px; text-align:center; cursor:pointer; transition:all 0.3s; }
.role-card:hover { background:rgba(102,126,234,0.1); }
.role-card.active { background:rgba(102,126,234,0.15); border-color:#667eea; box-shadow:0 0 20px rgba(102,126,234,0.2); }
.role-icon { font-size:28px; display:block; margin-bottom:4px; }
.role-name { font-size:14px; font-weight:600; color:#fff; }

.form-area :deep(.el-form-item) { margin-bottom:8px; }
.form-area :deep(.el-input__wrapper) { background:rgba(255,255,255,0.05); border:1px solid rgba(255,255,255,0.1); border-radius:10px; box-shadow:none; }
.form-area :deep(.el-input__wrapper:hover) { border-color:rgba(102,126,234,0.4); }
.form-area :deep(.el-input__wrapper.is-focus) { border-color:#667eea; box-shadow:0 0 0 2px rgba(102,126,234,0.15); }
.form-area :deep(input) { color:#fff; }
.form-area :deep(.el-input__inner::placeholder) { color:rgba(255,255,255,0.25); }
.form-area :deep(.el-form-item__label) { color:rgba(255,255,255,0.5); font-size:12px; }

.reg-btn { width:100%; height:44px; border-radius:10px; font-size:15px; font-weight:600; background:linear-gradient(135deg,#667eea,#764ba2); border:none; letter-spacing:2px; margin-top:4px; }
.reg-btn:hover { background:linear-gradient(135deg,#778ef0,#865ab8); box-shadow:0 4px 20px rgba(102,126,234,0.4); }
.login-link { text-align:center; margin-top:18px; font-size:13px; color:rgba(255,255,255,0.35); }
.login-link a { color:#8899ee; }
</style>
