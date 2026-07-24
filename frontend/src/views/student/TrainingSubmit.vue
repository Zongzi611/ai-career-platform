<template>
  <div class="training-detail" v-loading="loading">
    <!-- Task Info -->
    <div class="task-header">
      <div class="header-left">
        <span class="cat-tag">{{ catIcon }} {{ catLabel }}</span>
        <h2>{{ task.title }}</h2>
      </div>
      <el-tag :type="diffType" size="large" effect="plain">{{ diffLabel }}</el-tag>
    </div>

    <!-- Teacher Assigned Task Banner -->
    <div v-if="teacherTaskTitle" class="teacher-banner">
      <div class="tb-header">
        <span>📋 教师下发任务</span>
        <el-tag size="small" type="warning">来自教师</el-tag>
      </div>
      <h3>{{ teacherTaskTitle }}</h3>
      <p v-if="teacherTaskDesc">{{ teacherTaskDesc }}</p>
    </div>

    <el-card class="desc-card">
      <template #header><strong>📋 任务描述</strong></template>
      <p class="task-desc">{{ task.description }}</p>
    </el-card>

    <!-- Answer Area -->
    <el-card v-if="!scoreResult" class="answer-card">
      <template #header><strong>✍️ 你的回答</strong></template>
      <el-input
        v-model="answer"
        type="textarea"
        :rows="8"
        placeholder="在此输入你的答案...&#10;&#10;提示：仔细阅读任务描述，条理清晰地作答，尽量具体和量化。"
        :disabled="submitted"
      />
      <div class="answer-actions">
        <span class="word-count">{{ answer.length }} / 2000 字</span>
        <el-button type="primary" size="large" @click="handleSubmit" :loading="submitting" :disabled="!answer.trim()">
          提交并获取AI评分
        </el-button>
      </div>
    </el-card>

    <!-- Score Result -->
    <el-card v-if="scoreResult" class="score-card">
      <template #header><strong>🤖 AI 评分结果</strong></template>

      <div class="score-hero">
        <div class="score-circle" :class="scoreLevel">
          <span class="score-num">{{ scoreResult.score }}</span>
          <span class="score-unit">/100</span>
        </div>
        <div class="score-summary">
          <h3>{{ scoreComment }}</h3>
          <p>{{ scoreResult.feedback }}</p>
        </div>
      </div>

      <!-- Dimension Radar -->
      <div v-if="dimensions.length > 0" class="dimension-section">
        <h4>📊 分维度评估</h4>
        <div class="dimension-bars">
          <div v-for="d in dimensions" :key="d.label" class="dim-row">
            <div class="dim-label">
              <span>{{ d.label }}</span>
              <span class="dim-score">{{ d.score }}分</span>
            </div>
            <el-progress :percentage="d.percent" :color="barColor(d.percent)" :stroke-width="10" />
          </div>
        </div>
      </div>

      <div class="score-actions">
        <el-button @click="resetAnswer">重新作答</el-button>
        <el-button type="primary" @click="$router.push('/student/training')">返回任务列表</el-button>
      </div>
    </el-card>

    <!-- Already submitted notice -->
    <el-card v-if="submitted && !scoreResult" class="notice-card">
      <p>✅ 你已提交过此任务的答案</p>
      <el-button type="primary" @click="triggerScore">获取AI评分</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { getTask, submitAnswer, getMyRecords, triggerScoring } from '../../api/training'
import { ElMessage } from 'element-plus'

const route = useRoute()
const task = ref({})
const teacherTaskTitle = ref(route.query.teacherTaskTitle || '')
const teacherTaskDesc = ref(route.query.teacherTaskDesc || '')
const answer = ref('')
const submitting = ref(false)
const submitted = ref(false)
const scoreResult = ref(null)
const loading = ref(true)

const catIcon = computed(() => ({ resume:'📄', interview:'🎤', workplace_comm:'📧', tech_practice:'💻', career_plan:'📝', business_analysis:'📊' }[task.value.category] || '📌'))
const catLabel = computed(() => ({ resume:'简历优化', interview:'面试模拟', workplace_comm:'职场沟通', tech_practice:'技术实战', career_plan:'职业规划', business_analysis:'商业分析' }[task.value.category] || task.value.category))
const diffType = computed(() => task.value.difficulty === 'HARD' ? 'danger' : task.value.difficulty === 'MEDIUM' ? 'warning' : 'success')
const diffLabel = computed(() => task.value.difficulty === 'HARD' ? '⭐⭐⭐ 挑战' : task.value.difficulty === 'MEDIUM' ? '⭐⭐ 进阶' : '⭐ 入门')

const scoreLevel = computed(() => {
  const s = scoreResult.value?.score || 0
  return s >= 85 ? 'excellent' : s >= 70 ? 'good' : s >= 60 ? 'pass' : 'fail'
})

const scoreComment = computed(() => {
  const s = scoreResult.value?.score || 0
  return s >= 85 ? '非常出色！' : s >= 70 ? '表现不错！' : s >= 60 ? '基本合格' : '需要加强'
})

// Parse scoring criteria into dimensions
const dimensions = computed(() => {
  const criteria = task.value.scoringCriteria
  if (!criteria || !scoreResult.value) return []
  try {
    // criteria format: "完整性(30分):描述;匹配度(25分):描述;..."
    return criteria.split(';').map(part => {
      const match = part.match(/(.+)\((\d+)分\)/)
      if (!match) return null
      const label = match[1]
      const maxScore = parseInt(match[2])
      // Estimate dimension score from total score proportionally
      const estimatedScore = Math.round(scoreResult.value.score * maxScore / 100)
      return {
        label,
        score: estimatedScore,
        percent: Math.round(estimatedScore / maxScore * 100) || 0,
        maxScore
      }
    }).filter(Boolean)
  } catch { return [] }
})

const barColor = (p) => p >= 85 ? '#67C23A' : p >= 70 ? '#409EFF' : p >= 60 ? '#E6A23C' : '#F56C6C'

onMounted(async () => {
  loading.value = true
  try {
    const res = await getTask(route.params.id)
    task.value = res.data || {}

    const records = await getMyRecords({ page: 1, size: 100 })
    const existing = (records.data?.records || []).find(r => r.taskId == route.params.id)
    if (existing) {
      answer.value = existing.userAnswer || ''
      submitted.value = true
      if (existing.aiScore != null) {
        scoreResult.value = { score: existing.aiScore, feedback: existing.aiFeedback }
      }
    }
  } finally {
    loading.value = false
  }
})

const handleSubmit = async () => {
  if (!answer.value.trim()) { ElMessage.warning('请输入答案'); return }
  submitting.value = true
  try {
    await submitAnswer({ taskId: route.params.id, userAnswer: answer.value })
    submitted.value = true
    ElMessage.success('提交成功！正在AI评分...')
    await triggerScore()
  } catch (e) {
    ElMessage.error(e.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

const triggerScore = async () => {
  try {
    const records = await getMyRecords({ page: 1, size: 100 })
    const record = (records.data?.records || []).find(r => r.taskId == route.params.id)
    if (record) {
      const res = await triggerScoring(record.id)
      scoreResult.value = res.data
    }
  } catch (e) {
    ElMessage.error('AI评分失败，请稍后重试')
  }
}

const resetAnswer = () => {
  scoreResult.value = null
  submitted.value = false
  answer.value = ''
}
</script>

<style scoped>
.training-detail { max-width: 860px; margin: 0 auto; padding: 4px 0; }

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}
.header-left h2 { font-size: 20px; color: #303133; margin-top: 8px; }
.cat-tag { font-size: 12px; color: #909399; }

.teacher-banner {
  background: linear-gradient(135deg, #fef7e0, #fdf0c8);
  border: 1px solid #f5dab1; border-radius: 12px;
  padding: 16px 20px; margin-bottom: 16px;
}
.tb-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; font-size: 12px; color: #909399; }
.teacher-banner h3 { margin: 0 0 4px; font-size: 17px; color: #303133; }
.teacher-banner p { margin: 0; font-size: 13px; color: #606266; line-height: 1.6; }

.desc-card, .answer-card, .score-card, .notice-card { margin-bottom: 16px; }
.task-desc { white-space: pre-wrap; line-height: 1.8; color: #606266; font-size: 14px; }

.answer-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}
.word-count { font-size: 12px; color: #909399; }

/* Score Hero */
.score-hero {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}

.score-circle {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.score-circle.excellent { background: #f0f9eb; border: 3px solid #67C23A; }
.score-circle.good { background: #ecf5ff; border: 3px solid #409EFF; }
.score-circle.pass { background: #fdf6ec; border: 3px solid #E6A23C; }
.score-circle.fail { background: #fef0f0; border: 3px solid #F56C6C; }

.score-num { font-size: 36px; font-weight: 700; color: #303133; line-height: 1; }
.score-unit { font-size: 13px; color: #909399; }
.score-summary h3 { font-size: 18px; color: #303133; margin-bottom: 6px; }
.score-summary p { color: #606266; line-height: 1.7; font-size: 14px; }

/* Dimension Bars */
.dimension-section { margin-top: 8px; }
.dimension-section h4 { font-size: 15px; color: #303133; margin-bottom: 16px; }
.dim-row { margin-bottom: 14px; }
.dim-label { display: flex; justify-content: space-between; margin-bottom: 4px; font-size: 13px; color: #606266; }
.dim-score { font-weight: 600; color: #303133; }

.score-actions { display: flex; gap: 10px; margin-top: 20px; justify-content: center; }

.notice-card { text-align: center; padding: 30px; }
.notice-card p { font-size: 15px; color: #67C23A; margin-bottom: 12px; }
</style>
