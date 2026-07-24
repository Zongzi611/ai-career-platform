<template>
  <div class="dashboard">
    <!-- Hero Banner -->
    <div class="hero-banner">
      <div class="hero-left">
        <div class="hero-avatar">{{ avatarEmoji }}</div>
        <div>
          <h2>{{ greetingText }}</h2>
          <p class="hero-sub">
            {{ userStore.userInfo?.major || '请完善专业信息' }}
            <span v-if="userStore.userInfo?.grade"> · {{ userStore.userInfo.grade }}</span>
          </p>
        </div>
      </div>
      <div class="hero-right">
        <div class="hero-stat" v-for="s in heroStats" :key="s.label">
          <span class="hs-val">{{ s.value }}</span>
          <span class="hs-label">{{ s.label }}</span>
        </div>
      </div>
    </div>

    <!-- Three Cards Row -->
    <div class="top-row">
      <!-- Assessment Card -->
      <div class="glass-card assessment">
        <div class="card-head">
          <span class="card-icon">🧠</span>
          <h3>测评画像</h3>
          <el-button size="small" text type="primary" @click="$router.push('/student/assessment')">查看全部</el-button>
        </div>
        <div v-if="latestAssessment" class="card-body">
          <div class="mbti-badge" v-if="latestAssessment.typeCode === 'MBTI'">
            <span v-for="ch in latestAssessment.resultType" :key="ch" class="mbti-char">{{ ch }}</span>
          </div>
          <div class="holland-badge" v-else>
            <el-tag v-for="ch in latestAssessment.resultType" :key="ch" size="large" effect="dark" type="success">{{ ch }}</el-tag>
          </div>
          <div class="mini-dims">
            <div v-for="(v,k) in Object.entries(latestAssessment.dimensionScores||{}).slice(0,4)" :key="k" class="mini-dim">
              <span class="md-label">{{ k }}</span>
              <el-progress :percentage="Math.round(v/10*100)" :color="v>=7?'#67C23A':v>=4?'#409EFF':'#E6A23C'" :stroke-width="6" :show-text="false" />
              <span class="md-val">{{ v }}</span>
            </div>
          </div>
        </div>
        <div v-else class="card-empty">
          <p>完成测评，发现你的职业人格</p>
          <el-button type="primary" size="small" @click="$router.push('/student/assessment')">开始测评</el-button>
        </div>
      </div>

      <!-- Career Card -->
      <div class="glass-card career">
        <div class="card-head">
          <span class="card-icon">💼</span>
          <h3>职业匹配</h3>
          <el-button size="small" text type="primary" @click="$router.push('/student/career')">AI 推荐</el-button>
        </div>
        <div v-if="careerMatches.length" class="card-body">
          <div v-for="(c,i) in careerMatches.slice(0,3)" :key="c.id" class="match-row">
            <span class="match-num">{{ i+1 }}</span>
            <div class="match-info">
              <span class="match-name">{{ c.positionName }}</span>
              <span class="match-ind">{{ c.industry }}</span>
            </div>
            <span class="match-pct">{{ Math.round((c.similarityScore||0)*100) }}%</span>
          </div>
        </div>
        <div v-else class="card-empty">
          <p>AI 分析你的专业和测评</p>
          <el-button type="primary" size="small" @click="$router.push('/student/career')">智能推荐</el-button>
        </div>
      </div>

      <!-- Training Card -->
      <div class="glass-card training">
        <div class="card-head">
          <span class="card-icon">🏋️</span>
          <h3>实训成长</h3>
          <el-button size="small" text type="primary" @click="$router.push('/student/training')">全部任务</el-button>
        </div>
        <div class="card-body">
          <div class="score-ring-wrapper">
            <el-progress type="dashboard" :percentage="avgScorePercent" :color="scoreColor" :width="100">
              <template #default><span class="ring-score">{{ avgScore }}</span></template>
            </el-progress>
            <span class="ring-label">实训均分</span>
          </div>
          <div class="training-meta">
            <div class="tm-item">
              <span class="tm-num done">{{ trainingDone }}</span>
              <span class="tm-lbl">已完成</span>
            </div>
            <div class="tm-item">
              <span class="tm-num pending">{{ trainingTotal - trainingDone }}</span>
              <span class="tm-lbl">待挑战</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom: Growth Suggestions + Quick Entry -->
    <div class="bottom-row">
      <div class="glass-card suggestions">
        <div class="card-head">
          <span class="card-icon">💡</span>
          <h3>成长建议</h3>
        </div>
        <div class="sugg-list">
          <div v-for="s in suggestions" :key="s.text" class="sugg-item" @click="s.action && $router.push(s.action)">
            <span class="sugg-icon">{{ s.icon }}</span>
            <span class="sugg-text">{{ s.text }}</span>
            <span class="sugg-arrow">→</span>
          </div>
        </div>
      </div>
      <div class="glass-card shortcuts">
        <div class="card-head"><span class="card-icon">⚡</span><h3>快捷入口</h3></div>
        <div class="shortcut-grid">
          <div class="sc-item" @click="$router.push('/student/chat')">
            <span class="sc-icon">🤖</span><span>AI 咨询</span>
          </div>
          <div class="sc-item" @click="$router.push('/student/resume')">
            <span class="sc-icon">📝</span><span>简历优化</span>
          </div>
          <div class="sc-item" @click="$router.push('/student/assessment')">
            <span class="sc-icon">📋</span><span>新测评</span>
          </div>
          <div class="sc-item" @click="$router.push('/student/career')">
            <span class="sc-icon">🔍</span><span>职业搜索</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { getMyResults } from '../../api/assessment'
import { getRecommend } from '../../api/career'
import { getMyRecords, getTasks } from '../../api/training'

const userStore = useUserStore()

const assessmentResults = ref([])
const careerMatches = ref([])
const trainingRecords = ref([])
const allTasks = ref([])

const latestAssessment = computed(() => assessmentResults.value.find(r => r.resultType) || null)
const trainingDone = computed(() => trainingRecords.value.filter(r => r.aiScore != null).length)
const trainingTotal = computed(() => allTasks.value.length || trainingRecords.value.length || 0)
const avgScore = computed(() => {
  const scored = trainingRecords.value.filter(r => r.aiScore != null)
  if (!scored.length) return '--'
  return Math.round(scored.reduce((s,r) => s + r.aiScore, 0) / scored.length)
})
const avgScorePercent = computed(() => typeof avgScore.value === 'number' ? avgScore.value : 0)
const scoreColor = computed(() => avgScorePercent.value >= 80 ? '#67C23A' : avgScorePercent.value >= 60 ? '#409EFF' : '#E6A23C')

const heroStats = computed(() => [
  { value: assessmentResults.value.length, label: '测评报告' },
  { value: trainingDone.value, label: '实训完成' },
  { value: careerMatches.value.length, label: '推荐岗位' },
])

const avatarEmoji = computed(() => {
  const name = userStore.userInfo?.realName || ''
  if (!name) return '🎓'
  const hour = new Date().getHours()
  if (hour < 9) return '🌅'
  if (hour < 18) return '☀️'
  return '🌙'
})

const greetingText = computed(() => {
  const name = userStore.userInfo?.realName || '同学'
  const hour = new Date().getHours()
  if (hour < 9) return `早安，${name}`
  if (hour < 12) return `上午好，${name}`
  if (hour < 14) return `中午好，${name}`
  if (hour < 18) return `下午好，${name}`
  return `晚上好，${name}`
})

const suggestions = computed(() => {
  const list = []
  if (!latestAssessment.value) {
    list.push({ icon: '📋', text: '完成MBTI测评，了解你的人格类型', action: '/student/assessment' })
  } else if (latestAssessment.value.typeCode === 'MBTI' && !assessmentResults.value.find(r => r.typeCode === 'HOLLAND')) {
    list.push({ icon: '🎯', text: '再做一个霍兰德测评，发现职业兴趣', action: '/student/assessment' })
  }
  if (trainingDone.value === 0) {
    list.push({ icon: '🏆', text: '开始第一个实训任务，获得AI评分', action: '/student/training' })
  }
  if (!careerMatches.value.length) {
    list.push({ icon: '🔍', text: '获取AI智能岗位推荐', action: '/student/career' })
  }
  if (avgScore.value !== '--' && avgScore.value < 60) {
    list.push({ icon: '📈', text: '你的实训均分偏低，多做几个任务来提升吧', action: '/student/training' })
  }
  if (latestAssessment.value && avgScore.value !== '--') {
    list.push({ icon: '🤖', text: '和AI导师聊聊你的职业规划', action: '/student/chat' })
  }
  list.push({ icon: '📝', text: '用AI优化你的简历，准备求职', action: '/student/resume' })
  return list.slice(0, 4)
})

onMounted(async () => {
  try {
    const [ar, tr, tk] = await Promise.all([
      getMyResults(),
      getMyRecords({ page: 1, size: 50 }),
      getTasks({ page: 1, size: 50 })
    ])
    assessmentResults.value = ar.data || []
    trainingRecords.value = tr.data?.records || []
    allTasks.value = tk.data?.records || []

    // Get career recommendations based on user major
    const major = userStore.userInfo?.major || ''
    const cr = await getRecommend({ major, topK: 4 })
    careerMatches.value = cr.data || []
  } catch {}
})
</script>

<style scoped>
.dashboard { padding: 4px 0; max-width: 1200px; margin: 0 auto; }

/* Hero */
.hero-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28px 32px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  border-radius: 16px;
  color: #fff;
  margin-bottom: 20px;
}
.hero-left { display: flex; align-items: center; gap: 16px; }
.hero-avatar { font-size: 42px; width: 60px; height: 60px; display: flex; align-items: center; justify-content: center; background: rgba(255,255,255,0.1); border-radius: 50%; }
.hero-left h2 { margin: 0 0 2px; font-size: 22px; font-weight: 600; }
.hero-sub { margin: 0; font-size: 13px; opacity: 0.7; }
.hero-right { display: flex; gap: 24px; }
.hero-stat { text-align: center; }
.hs-val { display: block; font-size: 28px; font-weight: 700; line-height: 1.2; }
.hs-label { font-size: 11px; opacity: 0.7; }

/* Cards */
.top-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; margin-bottom: 16px; }
.bottom-row { display: grid; grid-template-columns: 2fr 1fr; gap: 16px; }
@media (max-width: 1100px) { .top-row, .bottom-row { grid-template-columns: 1fr; } }

.glass-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px 22px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  border: 1px solid #f0f0f0;
}

.card-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}
.card-head h3 { margin: 0; font-size: 15px; color: #303133; flex: 1; }
.card-icon { font-size: 20px; }

.card-empty { text-align: center; padding: 20px 0; color: #909399; }
.card-empty p { margin-bottom: 10px; font-size: 13px; }

/* Assessment */
.mbti-badge { display: flex; gap: 6px; justify-content: center; margin-bottom: 14px; }
.mbti-char {
  width: 40px; height: 40px; display: flex; align-items: center; justify-content: center;
  font-size: 18px; font-weight: 800; border-radius: 10px;
  background: linear-gradient(135deg, #667eea, #764ba2); color: #fff;
  font-family: 'Courier New', monospace;
}
.holland-badge { display: flex; gap: 6px; justify-content: center; margin-bottom: 14px; }
.mini-dims { display: flex; flex-direction: column; gap: 6px; }
.mini-dim { display: flex; align-items: center; gap: 6px; }
.md-label { width: 20px; font-weight: 700; font-size: 12px; color: #303133; text-align: right; flex-shrink: 0; }
.md-val { width: 18px; font-size: 11px; color: #909399; flex-shrink: 0; }

/* Career */
.match-row {
  display: flex; align-items: center; gap: 10px; padding: 10px 8px;
  border-radius: 8px; cursor: pointer; transition: background 0.15s;
}
.match-row:hover { background: #f5f7fa; }
.match-num {
  width: 24px; height: 24px; border-radius: 50%; display: flex; align-items: center;
  justify-content: center; font-size: 11px; font-weight: 700; flex-shrink: 0;
  background: #f0f2f5; color: #909399;
}
.match-row:nth-child(1) .match-num { background: #fff3e0; color: #e65100; }
.match-row:nth-child(2) .match-num { background: #e8eaf6; color: #283593; }
.match-row:nth-child(3) .match-num { background: #fce4ec; color: #880e4f; }
.match-info { flex: 1; display: flex; flex-direction: column; min-width: 0; }
.match-name { font-size: 13px; font-weight: 500; color: #303133; }
.match-ind { font-size: 11px; color: #909399; }
.match-pct { font-size: 13px; font-weight: 700; color: #409EFF; }

/* Training */
.score-ring-wrapper { text-align: center; margin-bottom: 12px; }
.ring-score { font-size: 20px; font-weight: 700; color: #303133; }
.ring-label { display: block; font-size: 11px; color: #909399; margin-top: 4px; }
.training-meta { display: flex; justify-content: center; gap: 32px; }
.tm-item { text-align: center; }
.tm-num { display: block; font-size: 22px; font-weight: 700; }
.tm-num.done { color: #67C23A; }
.tm-num.pending { color: #909399; }
.tm-lbl { font-size: 11px; color: #909399; }

/* Suggestions */
.sugg-list { display: flex; flex-direction: column; gap: 2px; }
.sugg-item {
  display: flex; align-items: center; gap: 10px; padding: 10px 8px;
  border-radius: 8px; cursor: pointer; transition: background 0.15s;
}
.sugg-item:hover { background: #f5f7fa; }
.sugg-icon { font-size: 18px; flex-shrink: 0; }
.sugg-text { flex: 1; font-size: 13px; color: #606266; }
.sugg-arrow { font-size: 14px; color: #c0c4cc; }

/* Shortcuts */
.shortcut-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }
.sc-item {
  display: flex; align-items: center; gap: 6px; padding: 12px;
  border-radius: 10px; cursor: pointer; transition: all 0.15s;
  background: #fafbfc; font-size: 13px; color: #606266;
}
.sc-item:hover { background: #ecf5ff; color: #409EFF; }
.sc-icon { font-size: 20px; }
</style>
