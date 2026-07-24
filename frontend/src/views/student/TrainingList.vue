<template>
  <div class="training-page">
    <div class="page-header">
      <h2>🏋️ 微实训任务</h2>
      <p class="subtitle">完成真实职场场景训练，AI 自动评分反馈</p>
    </div>

    <!-- Teacher Assigned Tasks -->
    <div v-if="assignedTasks.length > 0" class="assigned-section">
      <h3>📋 教师下发任务</h3>
      <div class="assigned-list">
        <div v-for="t in assignedTasks" :key="t.id" class="assigned-card" @click="openAssignedTask(t)">
          <div class="as-left">
            <el-tag size="small" :type="t.taskType==='ASSESSMENT'?'success':''">{{ t.taskType==='ASSESSMENT'?'测评':'实训' }}</el-tag>
            <span class="as-title">{{ t.title }}</span>
            <span class="as-desc">{{ t.description?.substring(0,60) }}...</span>
          </div>
          <div class="as-right">
            <span v-if="t.deadline" class="as-deadline">📅 {{ t.deadline }}</span>
            <el-tag size="small" type="warning">待完成</el-tag>
            <span class="as-arrow">→</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters -->
    <div class="filter-bar">
      <el-radio-group v-model="filterCategory" @change="applyFilter">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="resume">📄 简历优化</el-radio-button>
        <el-radio-button value="interview">🎤 面试模拟</el-radio-button>
        <el-radio-button value="workplace_comm">📧 职场沟通</el-radio-button>
        <el-radio-button value="tech_practice">💻 技术实战</el-radio-button>
        <el-radio-button value="career_plan">📝 职业规划</el-radio-button>
        <el-radio-button value="business_analysis">📊 商业分析</el-radio-button>
      </el-radio-group>
      <el-select v-model="filterDifficulty" placeholder="难度筛选" clearable @change="applyFilter" style="width:130px;margin-left:12px">
        <el-option label="全部难度" value="" />
        <el-option label="⭐ 入门" value="EASY" />
        <el-option label="⭐⭐ 进阶" value="MEDIUM" />
        <el-option label="⭐⭐⭐ 挑战" value="HARD" />
      </el-select>
      <span class="task-count">共 {{ tasks.length }} 个任务</span>
    </div>

    <!-- Task Grid -->
    <div class="task-grid">
      <div v-for="t in tasks" :key="t.id" class="task-card" :class="{ completed: completedIds.has(t.id) }" @click="$router.push(`/student/training/${t.id}`)">
        <div class="card-cat">{{ catIcon(t.category) }} {{ catLabel(t.category) }}</div>
        <h3 class="card-title">{{ t.title }}</h3>
        <p class="card-desc">{{ t.description?.substring(0, 60) }}...</p>
        <div class="card-footer">
          <el-tag :type="diffType(t.difficulty)" size="small" effect="light">{{ diffLabel(t.difficulty) }}</el-tag>
          <span v-if="completedIds.has(t.id)" class="done-badge">✅ 已完成</span>
          <span v-else class="go-badge">去训练 →</span>
        </div>
      </div>
    </div>

    <el-empty v-if="tasks.length === 0" description="暂无匹配任务" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTasks, getMyRecords } from '../../api/training'
import request from '../../api/request'

const router = useRouter()

const tasks = ref([])
const assignedTasks = ref([])
const completedIds = ref(new Set())
const filterCategory = ref('')
const filterDifficulty = ref('')

const openAssignedTask = (t) => {
  const query = { teacherTaskId: t.id, teacherTaskTitle: t.title, teacherTaskDesc: t.description || '' }
  if (t.taskType === 'ASSESSMENT') {
    if (t.refId) router.push({ path: `/student/assessment/${t.refId}`, query })
    else router.push({ path: '/student/assessment', query })
  } else {
    if (t.refId) router.push({ path: `/student/training/${t.refId}`, query })
    else router.push({ path: '/student/training', query })
  }
}

const catIcon = (c) => ({ resume:'📄', interview:'🎤', workplace_comm:'📧', tech_practice:'💻', career_plan:'📝', business_analysis:'📊' }[c] || '📌')
const catLabel = (c) => ({ resume:'简历优化', interview:'面试模拟', workplace_comm:'职场沟通', tech_practice:'技术实战', career_plan:'职业规划', business_analysis:'商业分析' }[c] || c)
const diffType = (d) => d === 'HARD' ? 'danger' : d === 'MEDIUM' ? 'warning' : 'success'
const diffLabel = (d) => d === 'HARD' ? '⭐⭐⭐ 挑战' : d === 'MEDIUM' ? '⭐⭐ 进阶' : '⭐ 入门'

const applyFilter = () => {
  loadTasks()
}

const loadTasks = async () => {
  const params = { page: 1, size: 50 }
  if (filterCategory.value) params.category = filterCategory.value
  if (filterDifficulty.value) params.difficulty = filterDifficulty.value
  const res = await getTasks(params)
  tasks.value = res.data?.records || []
}

onMounted(async () => {
  await loadTasks()
  // Load assigned teacher tasks
  try {
    const at = await request.get('/training/assigned-tasks')
    assignedTasks.value = at.data || []
  } catch {}
  // Load completed records to mark done tasks
  try {
    const r = await getMyRecords({ page: 1, size: 100 })
    const records = r.data?.records || []
    records.forEach(r => {
      if (r.status === 'SCORED' || r.aiScore) completedIds.value.add(r.taskId)
    })
  } catch(e) { /* ignore */ }
})
</script>

<style scoped>
.training-page { padding: 4px 0; }

.page-header {
  margin-bottom: 20px;
}
.page-header h2 {
  font-size: 22px;
  color: #303133;
  margin-bottom: 4px;
}
.subtitle {
  color: #909399;
  font-size: 13px;
}

.filter-bar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 8px;
}

.task-count {
  margin-left: auto;
  font-size: 13px;
  color: #909399;
}

.task-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.task-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #ebeef5;
  position: relative;
}

.task-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.08);
  border-color: #c6e2ff;
}

.task-card.completed {
  opacity: 0.75;
  background: #fafbfc;
}

.card-cat {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.card-title {
  font-size: 15px;
  color: #303133;
  margin-bottom: 8px;
  line-height: 1.4;
}

.card-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
  margin-bottom: 14px;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.done-badge {
  font-size: 12px;
  color: #67C23A;
  font-weight: 500;
}
.go-badge {
  font-size: 12px;
  color: #409EFF;
  font-weight: 500;
}

.assigned-section { margin-bottom: 24px; }
.assigned-section h3 { font-size: 15px; margin-bottom: 10px; color: #303133; }
.assigned-list { display: flex; flex-direction: column; gap: 6px; }
.assigned-card {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 18px; background: #fef7e0; border: 1px solid #f5dab1;
  border-radius: 10px; cursor: pointer; transition: all 0.15s;
}
.assigned-card:hover { background: #fdf0c8; border-color: #e6c88a; }
.as-arrow { font-size: 16px; color: #E6A23C; margin-left: 4px; }
.as-left { display: flex; align-items: center; gap: 10px; flex: 1; min-width: 0; }
.as-title { font-weight: 600; color: #303133; font-size: 14px; }
.as-desc { font-size: 12px; color: #909399; }
.as-right { display: flex; align-items: center; gap: 10px; flex-shrink: 0; }
.as-deadline { font-size: 12px; color: #E6A23C; }
</style>
