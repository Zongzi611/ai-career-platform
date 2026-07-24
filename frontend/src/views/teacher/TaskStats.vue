<template>
  <div class="admin-page">
    <h2>📈 任务完成统计</h2>

    <div v-loading="loading" class="stats-grid">
      <div v-for="s in stats" :key="s.taskId" class="stat-card">
        <div class="sc-header">
          <el-tag size="small" :type="s.taskType==='ASSESSMENT'?'success':''">{{ s.taskType==='ASSESSMENT'?'测评':'实训' }}</el-tag>
          <span class="sc-class">{{ s.targetClass || '全体' }}</span>
        </div>
        <h3 class="sc-title">{{ s.title }}</h3>
        <div class="sc-progress">
          <el-progress type="circle" :percentage="s.percent" :width="80" :stroke-width="8" :color="s.percent>=80?'#67C23A':s.percent>=40?'#409EFF':'#E6A23C'" />
        </div>
        <div class="sc-numbers">
          <span class="sc-done">{{ s.completed }}</span> / <span class="sc-total">{{ s.totalStudents }}</span>
          <span class="sc-label">已完成 / 应完成</span>
        </div>
        <div class="sc-bar">
          <div class="sc-bar-fill" :style="{width:s.percent+'%'}" :class="s.percent>=80?'bg-green':s.percent>=40?'bg-blue':'bg-orange'"></div>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && stats.length===0" description="暂无下发任务" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../api/request'

const stats = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await request.get('/teacher/task-stats')
    stats.value = res.data || []
  } catch {} finally { loading.value = false }
})
</script>

<style scoped>
.admin-page { padding: 4px 0; }
.admin-page h2 { font-size: 20px; margin-bottom: 20px; }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}
@media (max-width: 1100px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 700px) { .stats-grid { grid-template-columns: 1fr; } }

.stat-card {
  background: #fff; border-radius: 12px; padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  text-align: center;
}
.sc-header { display: flex; justify-content: space-between; margin-bottom: 8px; }
.sc-class { font-size: 11px; color: #909399; }
.sc-title { font-size: 14px; color: #303133; margin: 0 0 14px; line-height: 1.4; min-height: 40px; }
.sc-progress { margin-bottom: 10px; }
.sc-numbers { font-size: 14px; color: #606266; margin-bottom: 12px; }
.sc-done { font-size: 24px; font-weight: 700; color: #409EFF; }
.sc-total { font-size: 18px; color: #909399; }
.sc-label { display: block; font-size: 11px; color: #909399; margin-top: 2px; }

.sc-bar { height: 8px; background: #f0f2f5; border-radius: 4px; overflow: hidden; }
.sc-bar-fill { height: 100%; border-radius: 4px; transition: width 0.6s ease; }
.bg-green { background: #67C23A; }
.bg-blue { background: #409EFF; }
.bg-orange { background: #E6A23C; }
</style>
