<template>
  <div class="profile-page">
    <h2>👤 个人中心</h2>

    <div class="profile-layout">
      <!-- Avatar & Name -->
      <div class="profile-card">
        <div class="pc-avatar">{{ form.realName?.charAt(0) || '?' }}</div>
        <h3>{{ form.realName || '未设置姓名' }}</h3>
        <p>{{ userStore.userInfo?.username }}</p>
        <el-tag v-for="r in (userStore.roles||[])" :key="r" size="small" style="margin:2px">{{ r==='ROLE_STUDENT'?'学生':r==='ROLE_TEACHER'?'教师':r==='ROLE_ADMIN'?'管理员':r }}</el-tag>
      </div>

      <!-- Edit Form -->
      <div class="edit-card">
        <h3>📝 编辑资料</h3>
        <el-form :model="form" label-width="80px" label-position="top">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="真实姓名"><el-input v-model="form.realName" /></el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="性别">
                <el-select v-model="form.gender"><el-option :value="0" label="未知" /><el-option :value="1" label="男" /><el-option :value="2" label="女" /></el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="学院"><el-input v-model="form.college" /></el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="专业"><el-input v-model="form.major" /></el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="年级"><el-input v-model="form.grade" placeholder="如 2024级" /></el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="班级"><el-input v-model="form.className" /></el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16" v-if="userStore.isStudent">
            <el-col :span="12">
              <el-form-item label="学号"><el-input v-model="form.studentNo" /></el-form-item>
            </el-col>
          </el-row>
          <el-button type="primary" size="large" @click="save" :loading="saving">保存修改</el-button>
        </el-form>

        <!-- Password Change -->
        <h3 style="margin-top:28px">🔒 修改密码</h3>
        <el-form :model="pwdForm" label-width="100px" label-position="top">
          <el-row :gutter="16">
            <el-col :span="8"><el-form-item label="旧密码"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="确认密码"><el-input v-model="pwdForm.confirmPassword" type="password" show-password /></el-form-item></el-col>
          </el-row>
          <el-button type="warning" size="large" @click="changePwd" :loading="pwdSaving">修改密码</el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import request from '../api/request'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const saving = ref(false)
const pwdSaving = ref(false)
const form = reactive({ realName:'', gender:0, email:'', phone:'', college:'', major:'', grade:'', className:'', studentNo:'' })
const pwdForm = reactive({ oldPassword:'', newPassword:'', confirmPassword:'' })

onMounted(async () => {
  try {
    const res = await request.get('/auth/info')
    const u = res.data || {}
    Object.keys(form).forEach(k => { if (u[k] !== undefined && u[k] !== null) form[k] = u[k] })
  } catch {}
})

const save = async () => {
  saving.value = true
  try {
    await request.put('/auth/profile', form)
    ElMessage.success('资料已更新')
  } catch { ElMessage.error('更新失败') }
  finally { saving.value = false }
}

const changePwd = async () => {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) { ElMessage.warning('请填写密码'); return }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) { ElMessage.warning('两次密码不一致'); return }
  pwdSaving.value = true
  try {
    await request.put('/auth/password', pwdForm)
    ElMessage.success('密码已修改，请重新登录')
    pwdForm.oldPassword = ''; pwdForm.newPassword = ''; pwdForm.confirmPassword = ''
  } catch { ElMessage.error('密码修改失败') }
  finally { pwdSaving.value = false }
}
</script>

<style scoped>
.profile-page { max-width: 860px; margin: 0 auto; padding: 4px 0; }
.profile-page h2 { font-size: 20px; margin-bottom: 20px; }

.profile-layout { display: grid; grid-template-columns: 220px 1fr; gap: 24px; align-items: start; }
@media (max-width: 700px) { .profile-layout { grid-template-columns: 1fr; } }

.profile-card { background: #fff; border-radius: 14px; padding: 32px 24px; text-align: center; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.pc-avatar { width: 80px; height: 80px; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg,#667eea,#764ba2); color:#fff; font-size: 32px; font-weight: 700; margin: 0 auto 12px; }
.profile-card h3 { margin: 0 0 4px; font-size: 18px; }
.profile-card p { margin: 0 0 10px; font-size: 13px; color: #909399; }

.edit-card { background: #fff; border-radius: 14px; padding: 28px 32px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.edit-card h3 { margin: 0 0 16px; font-size: 15px; color: #303133; }
</style>
