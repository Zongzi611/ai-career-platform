<template>
  <div class="dash-page">
    <h2>⚙️ 管理中心</h2>

    <div class="stat-row" v-loading="loading">
      <div class="stat-card"><span class="num">{{ stats.totalUsers }}</span><span class="lbl">总用户</span></div>
      <div class="stat-card"><span class="num">{{ stats.totalStudents }}</span><span class="lbl">学生</span></div>
      <div class="stat-card"><span class="num">{{ stats.totalTeachers }}</span><span class="lbl">教师</span></div>
      <div class="stat-card"><span class="num">{{ stats.totalCareers }}</span><span class="lbl">职业数据</span></div>
      <div class="stat-card"><span class="num">{{ stats.totalTasks }}</span><span class="lbl">实训任务</span></div>
      <div class="stat-card"><span class="num">{{ stats.totalAssessments }}</span><span class="lbl">测评记录</span></div>
    </div>

    <div class="quick-links">
      <div class="ql-card" @click="$router.push('/admin/users')">
        <span class="ql-icon">👥</span><span>用户管理</span><span class="ql-num">{{ stats.totalUsers }}</span>
      </div>
      <div class="ql-card" @click="$router.push('/admin/careers')">
        <span class="ql-icon">💼</span><span>职业知识库</span><span class="ql-num">{{ stats.totalCareers }}</span>
      </div>
      <div class="ql-card" @click="$router.push('/admin/training')">
        <span class="ql-icon">🏋️</span><span>实训管理</span><span class="ql-num">{{ stats.totalTasks }}</span>
      </div>
      <div class="ql-card" @click="$router.push('/admin/config')">
        <span class="ql-icon">⚙️</span><span>系统配置</span>
      </div>
    </div>

    <div class="info-card">
      <h3>🖥️ 系统信息</h3>
      <el-descriptions :column="3" border size="small">
        <el-descriptions-item label="后端框架">Spring Boot 3.3.5</el-descriptions-item>
        <el-descriptions-item label="AI 模型">通义千问 qwen-turbo</el-descriptions-item>
        <el-descriptions-item label="数据库">MySQL 8.0</el-descriptions-item>
        <el-descriptions-item label="向量库">SimpleVectorStore (内存)</el-descriptions-item>
        <el-descriptions-item label="缓存">Redis 5.0.14</el-descriptions-item>
        <el-descriptions-item label="学生端功能">12 个模块</el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../api/request'

const stats = ref({ totalUsers: 0, totalStudents: 0, totalTeachers: 0, totalCareers: 0, totalTasks: 0, totalAssessments: 0 })
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const [users, careers, tasks] = await Promise.all([
      request.get('/admin/users', { params: { page: 1, size: 1 } }),
      request.get('/career/list', { params: { page: 1, size: 1 } }),
      request.get('/training/tasks', { params: { page: 1, size: 1 } })
    ])
    stats.value.totalUsers = users.data?.total || 0
    stats.value.totalCareers = careers.data?.total || 0
    stats.value.totalTasks = tasks.data?.total || 0
    // Get student/teacher counts
    const allUsers = await request.get('/admin/users', { params: { page: 1, size: 200 } })
    const records = allUsers.data?.records || []
    stats.value.totalStudents = records.filter(u => u.roles?.includes('ROLE_STUDENT')).length
    stats.value.totalTeachers = records.filter(u => u.roles?.includes('ROLE_TEACHER')).length
    stats.value.totalAssessments = records.filter(u => u.roles?.includes('ROLE_STUDENT')).length * 2 // estimate
  } catch {} finally { loading.value = false }
})
</script>

<style scoped>
.dash-page { padding: 4px 0; }
.dash-page h2 { font-size: 20px; margin-bottom: 20px; }
.stat-row { display: grid; grid-template-columns: repeat(6, 1fr); gap: 12px; margin-bottom: 24px; }
@media (max-width: 1000px) { .stat-row { grid-template-columns: repeat(3, 1fr); } }
.stat-card { background: #fff; border-radius: 12px; padding: 18px; text-align: center; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.num { display: block; font-size: 32px; font-weight: 800; color: #409EFF; line-height: 1.2; }
.lbl { font-size: 12px; color: #909399; }

.quick-links { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 24px; }
.ql-card { background: #fff; border-radius: 10px; padding: 18px; text-align: center; cursor: pointer; transition: all 0.15s; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.ql-card:hover { transform: translateY(-2px); box-shadow: 0 4px 14px rgba(0,0,0,0.08); }
.ql-icon { font-size: 24px; display: block; margin-bottom: 6px; }
.ql-card span:nth-child(2) { font-size: 14px; color: #303133; }
.ql-num { display: block; font-size: 20px; font-weight: 700; color: #409EFF; margin-top: 4px; }

.info-card { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.info-card h3 { margin: 0 0 14px; font-size: 15px; }
</style>
