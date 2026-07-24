<template>
  <div class="login-page">
    <!-- Background particles -->
    <div class="bg-particles">
      <div v-for="i in 20" :key="i" class="particle" :style="particleStyle(i)" />
    </div>

    <!-- Main card -->
    <div class="login-card">
      <!-- Logo -->
      <div class="logo-area">
        <div class="logo-icon">🎓</div>
        <h1>CareerSail</h1>
        <p>AI 职业规划智能体</p>
      </div>

      <!-- Role Selection -->
      <div class="role-title">选择登录身份</div>
      <div class="role-selector">
        <div
          v-for="role in roles"
          :key="role.key"
          class="role-card"
          :class="{ active: selectedRole === role.key }"
          @click="selectRole(role.key)"
        >
          <div class="role-icon">{{ role.icon }}</div>
          <div class="role-name">{{ role.label }}</div>
          <div class="role-desc">{{ role.desc }}</div>
        </div>
      </div>

      <!-- Login Form -->
      <transition name="fade">
        <div v-if="selectedRole" class="form-area">
          <div class="form-header">
            <span class="selected-badge">{{ selectedRoleLabel }}</span>
          </div>
          <el-input
            v-model="form.username"
            placeholder="用户名"
            size="large"
            class="tech-input"
            @keyup.enter="handleLogin"
          >
            <template #prefix><span class="input-icon">👤</span></template>
          </el-input>
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            class="tech-input"
            show-password
            @keyup.enter="handleLogin"
          >
            <template #prefix><span class="input-icon">🔑</span></template>
          </el-input>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="login-btn"
          >
            进入系统
          </el-button>
        </div>
      </transition>

      <p class="register-link">还没有账号？<router-link to="/register">立即注册</router-link></p>
    </div>

    <!-- Footer -->
    <div class="login-footer">© 2026 CareerSail · 助力每一位大学生找到职业方向</div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const selectedRole = ref('student')
const form = reactive({ username: '', password: '' })

const roles = [
  { key: 'student', icon: '🎒', label: '学生', desc: '测评 · 咨询 · 实训' },
  { key: 'teacher', icon: '👩‍🏫', label: '教师', desc: '学情 · 任务 · 报告' },
  { key: 'admin', icon: '⚙️', label: '管理员', desc: '系统管理与配置' },
]

const selectedRoleLabel = computed(() => roles.find(r => r.key === selectedRole.value)?.label || '')

const selectRole = (key) => {
  selectedRole.value = key
}

const particleStyle = (i) => ({
  left: `${Math.random() * 100}%`,
  top: `${Math.random() * 100}%`,
  animationDelay: `${Math.random() * 5}s`,
  animationDuration: `${3 + Math.random() * 4}s`,
  opacity: 0.15 + Math.random() * 0.3,
  width: `${2 + Math.random() * 4}px`,
  height: `${2 + Math.random() * 4}px`,
})

const handleLogin = async () => {
  if (!form.username.trim()) { ElMessage.warning('请输入用户名'); return }
  if (!form.password.trim()) { ElMessage.warning('请输入密码'); return }
  loading.value = true
  try {
    await userStore.login(form)
    const roles = userStore.roles
    if (roles.includes('ROLE_ADMIN')) router.push('/admin/dashboard')
    else if (roles.includes('ROLE_TEACHER')) router.push('/teacher/dashboard')
    else router.push('/student/home')
  } catch (e) {
    const msg = e?.response?.data?.message || '登录失败，请检查用户名和密码'
    ElMessage.error(msg)
  } finally { loading.value = false }
}
</script>

<style scoped>
.login-page {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #0a0e27;
  position: relative;
  overflow: hidden;
}

/* Particles */
.bg-particles { position: absolute; inset: 0; pointer-events: none; }
.particle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #00d4ff);
  animation: float-up linear infinite;
}
@keyframes float-up {
  0% { transform: translateY(100vh) scale(0); }
  50% { transform: translateY(50vh) scale(1); }
  100% { transform: translateY(-10vh) scale(0); }
}

/* Card */
.login-card {
  position: relative;
  z-index: 1;
  width: 520px;
  background: rgba(16, 20, 45, 0.85);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 20px;
  padding: 36px 40px;
  box-shadow: 0 0 60px rgba(102, 126, 234, 0.15), inset 0 1px 0 rgba(255,255,255,0.05);
}

/* Logo */
.logo-area { text-align: center; margin-bottom: 28px; }
.logo-icon { font-size: 48px; margin-bottom: 8px; }
.logo-area h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea, #00d4ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.logo-area p { margin: 4px 0 0; font-size: 13px; color: rgba(255,255,255,0.45); }

/* Role */
.role-title { text-align: center; font-size: 13px; color: rgba(255,255,255,0.4); margin-bottom: 12px; letter-spacing: 2px; text-transform: uppercase; }
.role-selector { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin-bottom: 24px; }

.role-card {
  background: rgba(255,255,255,0.03);
  border: 1px solid rgba(255,255,255,0.08);
  border-radius: 12px;
  padding: 16px 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}
.role-card:hover {
  background: rgba(102, 126, 234, 0.1);
  border-color: rgba(102, 126, 234, 0.3);
  transform: translateY(-2px);
}
.role-card.active {
  background: rgba(102, 126, 234, 0.15);
  border-color: #667eea;
  box-shadow: 0 0 20px rgba(102, 126, 234, 0.2);
}
.role-icon { font-size: 28px; margin-bottom: 6px; }
.role-name { font-size: 14px; font-weight: 600; color: #fff; margin-bottom: 2px; }
.role-desc { font-size: 11px; color: rgba(255,255,255,0.35); }

/* Form */
.form-area { display: flex; flex-direction: column; gap: 14px; }
.form-header { text-align: center; margin-bottom: 4px; }
.selected-badge {
  display: inline-block;
  padding: 4px 16px;
  border-radius: 20px;
  background: rgba(102, 126, 234, 0.15);
  border: 1px solid rgba(102, 126, 234, 0.3);
  color: #8899ee;
  font-size: 13px;
}

.tech-input :deep(.el-input__wrapper) {
  background: rgba(255,255,255,0.05);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 10px;
  box-shadow: none;
  transition: all 0.3s;
}
.tech-input :deep(.el-input__wrapper:hover) { border-color: rgba(102, 126, 234, 0.4); }
.tech-input :deep(.el-input__wrapper.is-focus) { border-color: #667eea; box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.15); }
.tech-input :deep(input) { color: #fff; }
.tech-input :deep(.el-input__inner::placeholder) { color: rgba(255,255,255,0.25); }
.input-icon { font-size: 16px; }

.login-btn {
  width: 100%;
  height: 46px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  letter-spacing: 2px;
}
.login-btn:hover {
  background: linear-gradient(135deg, #778ef0, #865ab8);
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
}

.register-link { text-align: center; margin-top: 20px; font-size: 13px; color: rgba(255,255,255,0.35); }
.register-link a { color: #8899ee; text-decoration: none; }

.login-footer { position: absolute; bottom: 24px; z-index: 1; font-size: 12px; color: rgba(255,255,255,0.2); }

/* Transition */
.fade-enter-active, .fade-leave-active { transition: all 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateY(10px); }
</style>
