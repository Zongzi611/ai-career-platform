<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>👥 用户管理</h2>
      <div class="header-stats">
        <span class="hs-item">👨‍🎓 {{ studentCount }} 学生</span>
        <span class="hs-item">👩‍🏫 {{ teacherCount }} 教师</span>
        <span class="hs-item">⚙️ {{ adminCount }} 管理员</span>
      </div>
    </div>

    <el-tabs v-model="activeTab" @tab-change="load">
      <el-tab-pane label="🎒 学生" name="student" />
      <el-tab-pane label="👩‍🏫 教师" name="teacher" />
      <el-tab-pane label="⚙️ 管理员" name="admin" />
      <el-tab-pane label="📋 全部" name="all" />
    </el-tabs>

    <el-table :data="filteredUsers" stripe v-loading="loading" max-height="calc(100vh - 260px)">
      <el-table-column prop="id" label="ID" width="50" />
      <el-table-column label="头像" width="60" align="center">
        <template #default="{row}"><span class="t-avatar">{{ (row.realName || row.username || '?').charAt(0) }}</span></template>
      </el-table-column>
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="college" label="学院" width="150" show-overflow-tooltip />
      <el-table-column prop="major" label="专业" width="130" show-overflow-tooltip />
      <el-table-column prop="className" label="班级" width="130" />
      <el-table-column label="角色" width="140">
        <template #default="{row}">
          <el-tag v-for="r in row.roles" :key="r" size="small" style="margin:1px"
            :type="r==='ROLE_STUDENT'?'':r==='ROLE_TEACHER'?'warning':'danger'">
            {{ r==='ROLE_STUDENT'?'学生':r==='ROLE_TEACHER'?'教师':'管理员' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="70" align="center">
        <template #default="{row}"><el-tag :type="row.status===1?'success':'info'" size="small">{{ row.status===1?'正常':'禁用' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="{row}">
          <el-button size="small" :type="row.status===1?'warning':'success'" @click="toggleStatus(row)">{{ row.status===1?'禁用':'启用' }}</el-button>
          <el-popconfirm title="重置密码为 123456？" @confirm="resetPwd(row.id)">
            <template #reference><el-button size="small">重置</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../../api/request'
import { ElMessage } from 'element-plus'

const users = ref([])
const loading = ref(false)
const activeTab = ref('student')

const filteredUsers = computed(() => {
  if (activeTab.value === 'all') return users.value
  const roleMap = { student: 'ROLE_STUDENT', teacher: 'ROLE_TEACHER', admin: 'ROLE_ADMIN' }
  return users.value.filter(u => u.roles?.includes(roleMap[activeTab.value]))
})

const studentCount = computed(() => users.value.filter(u => u.roles?.includes('ROLE_STUDENT')).length)
const teacherCount = computed(() => users.value.filter(u => u.roles?.includes('ROLE_TEACHER')).length)
const adminCount = computed(() => users.value.filter(u => u.roles?.includes('ROLE_ADMIN')).length)

const load = async () => {
  loading.value = true
  const res = await request.get('/admin/users', { params: { page: 1, size: 200 } })
  users.value = res.data?.records || []
  loading.value = false
}

const toggleStatus = async (row) => {
  await request.put(`/admin/users/${row.id}`, { status: row.status === 1 ? 0 : 1 })
  ElMessage.success(row.status === 1 ? '已禁用' : '已启用')
  load()
}

const resetPwd = async (id) => {
  await request.put(`/admin/users/${id}/reset-password`)
  ElMessage.success('密码已重置为 123456')
}

onMounted(load)
</script>

<style scoped>
.admin-page { padding: 4px 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.page-header h2 { font-size: 20px; margin: 0; }
.header-stats { display: flex; gap: 16px; }
.hs-item { font-size: 14px; color: #606266; }

.t-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  display: inline-flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff; font-weight: 700; font-size: 13px;
}
</style>
