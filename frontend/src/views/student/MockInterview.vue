<template>
  <div class="interview-page">
    <div class="page-header">
      <h2>🎤 AI 模拟面试</h2>
      <p class="subtitle">AI 扮演面试官，模拟真实面试场景，实时点评打分。共5题，约10分钟。</p>
    </div>

    <!-- Setup -->
    <div v-if="!state || state === 'setup'" class="setup-area">
      <!-- Type cards -->
      <div class="type-cards">
        <div class="type-card" :class="{active: interviewType==='technical'}" @click="interviewType='technical'">
          <div class="tc-icon">💻</div>
          <h3>技术面试</h3>
          <p>考察专业知识、算法思维、系统设计能力。适合工程师、开发岗位。</p>
          <ul>
            <li>编程语言基础</li>
            <li>系统设计思路</li>
            <li>项目经验深挖</li>
          </ul>
        </div>
        <div class="type-card" :class="{active: interviewType==='behavioral'}" @click="interviewType='behavioral'">
          <div class="tc-icon">🗣️</div>
          <h3>行为面试</h3>
          <p>考察沟通能力、团队协作、领导力。用STAR法则回答效果最佳。</p>
          <ul>
            <li>团队冲突处理</li>
            <li>失败经历复盘</li>
            <li>职业规划阐述</li>
          </ul>
        </div>
        <div class="type-card" :class="{active: interviewType==='group'}" @click="interviewType='group'">
          <div class="tc-icon">👥</div>
          <h3>群面模拟</h3>
          <p>无领导小组讨论场景。考察分析框架、表达逻辑、团队角色意识。</p>
          <ul>
            <li>案例框架分析</li>
            <li>角色定位策略</li>
            <li>总结陈词技巧</li>
          </ul>
        </div>
      </div>

      <!-- Config -->
      <div class="setup-form">
        <el-input v-model="targetPosition" placeholder="目标岗位，如 Java后端开发工程师" size="large" style="max-width:440px" />
        <div class="setup-options">
          <el-radio-group v-model="questionCount" size="small">
            <el-radio-button :value="3">快速3题</el-radio-button>
            <el-radio-button :value="5">标准5题</el-radio-button>
            <el-radio-button :value="8">深度8题</el-radio-button>
          </el-radio-group>
          <el-button type="primary" size="large" @click="startInterview" :loading="starting" class="start-btn">
            开始面试
          </el-button>
        </div>
      </div>

      <!-- Prep tips -->
      <div class="prep-tips">
        <h3>📋 准备清单</h3>
        <div class="prep-grid">
          <div class="prep-item"><span class="prep-num">1</span> 确保环境安静，代入真实面试场景</div>
          <div class="prep-item"><span class="prep-num">2</span> 每题回答建议 100-300 字，条理清晰</div>
          <div class="prep-item"><span class="prep-num">3</span> 使用具体案例，量化成果（如提升了30%）</div>
          <div class="prep-item"><span class="prep-num">4</span> 回答后查看 AI 点评，针对性改进</div>
        </div>
        <div class="prep-dosdonts">
          <div class="pd-do">
            <h4>✅ 加分项</h4>
            <p>STAR法则 · 量化数据 · 逻辑分层 · 真实案例 · 自信表达</p>
          </div>
          <div class="pd-dont">
            <h4>❌ 扣分项</h4>
            <p>泛泛而谈 · 背诵痕迹 · 答非所问 · 过度谦虚 · 贬低他人</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Active Interview -->
    <div v-if="state === 'qa'" class="qa-layout">
      <!-- Progress -->
      <div class="qa-progress">
        <el-steps :active="roundNum - 1" align-center>
          <template v-for="n in questionCount" :key="n">
            <el-step :title="'Q'+n" :description="qaHistory[n-1] ? qaHistory[n-1].score+'分' : '待答'" />
          </template>
        </el-steps>
      </div>

      <!-- Question -->
      <div class="question-bubble">
        <div class="qb-avatar">🧑‍💼</div>
        <div class="qb-content">
          <div class="qb-meta">
            <span>面试官</span>
            <span class="qb-round">第 {{ roundNum }}/{{ questionCount }} 题</span>
          </div>
          <p class="qb-text">{{ currentQuestion }}</p>
        </div>
      </div>

      <!-- Answer -->
      <div class="answer-area" v-if="!answered">
        <div class="answer-header">
          <span>✍️ 你的回答</span>
          <span class="word-guide" :class="answerQuality">{{ answerTip }}</span>
        </div>
        <el-input v-model="userAnswer" type="textarea" :rows="5" placeholder="输入你的回答...&#10;&#10;提示：条理清晰、具体量化、用真实案例。按 Ctrl+Enter 提交。" @keyup.ctrl.enter="submitAnswer" />
        <div class="answer-row">
          <span class="word-count">{{ userAnswer.length }} 字</span>
          <el-button type="primary" size="large" @click="submitAnswer" :loading="submitting" :disabled="!userAnswer.trim()">
            提交回答 (Ctrl+Enter)
          </el-button>
        </div>
      </div>

      <!-- Feedback -->
      <div v-if="feedback" class="feedback-card" :class="fbClass">
        <div class="fb-score-big">{{ lastScore }}</div>
        <div class="fb-body">
          <div class="fb-level">{{ scoreLabel(lastScore) }}</div>
          <p class="fb-text">{{ feedback }}</p>
        </div>
      </div>

      <!-- Next or Complete -->
      <div v-if="answered && !isComplete" style="text-align:center;margin-top:16px">
        <el-button v-if="nextQuestion" type="primary" size="large" @click="goNext" :loading="submitting">
          下一题 ({{ roundNum }}/{{ questionCount }})
        </el-button>
      </div>
    </div>

    <!-- Complete -->
    <div v-if="state === 'complete'" class="complete-card">
      <el-card>
        <template #header><strong>🏆 面试完成</strong></template>
        <div class="final-score">
          <el-progress type="circle" :percentage="totalScore" :width="130" :color="scoreColor" :stroke-width="8" />
          <div class="final-text">
            <h3>{{ scoreText }}</h3>
            <p>综合评分 {{ totalScore }} 分 · {{ questionCount }} 题 · {{ typeLabel(sessionType) }}</p>
          </div>
        </div>
        <div class="round-breakdown">
          <div v-for="(qa,i) in qaHistory" :key="i" class="rb-item">
            <div class="rb-num">Q{{ qa.roundNum }}</div>
            <div class="rb-body">
              <div class="rb-q">{{ qa.question?.substring(0,60) }}...</div>
              <el-progress :percentage="qa.score" :color="qa.score>=80?'#67C23A':qa.score>=60?'#409EFF':'#E6A23C'" :stroke-width="6" />
            </div>
            <span class="rb-score" :class="qa.score>=80?'c-green':qa.score>=60?'c-blue':'c-orange'">{{ qa.score }}</span>
          </div>
        </div>
        <div v-if="summary" class="summary-box" v-html="summaryHtml"></div>
        <div class="complete-actions">
          <el-button type="primary" @click="resetState">再来一次</el-button>
          <el-button @click="$router.push('/student/training')">去实训提升</el-button>
        </div>
      </el-card>
    </div>

    <!-- History -->
    <div class="history-section" v-if="sessions.length > 0">
      <h3>📋 面试记录</h3>
      <div class="history-list">
        <div v-for="s in sessions" :key="s.id" class="hist-item" @click="loadSession(s.id)">
          <div class="hist-info">
            <el-tag :type="s.interviewType==='technical'?'':'info'" size="small">{{ typeLabel(s.interviewType) }}</el-tag>
            <span class="hist-title">{{ s.title }}</span>
            <span class="hist-time">{{ s.createTime?.substring(0,16) }}</span>
            <span v-if="s.totalScore" class="hist-score" :class="s.totalScore>=80?'c-green':s.totalScore>=60?'c-blue':'c-orange'">{{ s.totalScore }}分</span>
          </div>
          <el-button size="small" text type="danger" @click.stop="delSession(s.id)">删除</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { startInterview as startInterviewApi, submitAnswer as submitAnswerApi, getSessions, getDetail, deleteSession } from '../../api/interview'
import { marked } from 'marked'
import { ElMessage, ElMessageBox } from 'element-plus'

const state = ref('setup')
const interviewType = ref('technical')
const targetPosition = ref('')
const questionCount = ref(5)
const starting = ref(false)
const submitting = ref(false)
const sessionId = ref(null)
const sessionType = ref('')
const roundNum = ref(1)
const currentQuestion = ref('')
const userAnswer = ref('')
const feedback = ref('')
const lastScore = ref(0)
const nextQuestion = ref('')
const answered = ref(false)
const isComplete = ref(false)
const totalScore = ref(0)
const summary = ref('')
const qaHistory = ref([])
const sessions = ref([])

const fbClass = computed(() => lastScore.value >= 80 ? 'fb-great' : lastScore.value >= 60 ? 'fb-good' : 'fb-poor')
const scoreColor = computed(() => totalScore.value >= 80 ? '#67C23A' : totalScore.value >= 60 ? '#409EFF' : '#E6A23C')
const scoreText = computed(() => totalScore.value >= 85 ? '优秀！你已具备较强的面试能力' : totalScore.value >= 70 ? '不错！还有提升空间' : totalScore.value >= 60 ? '合格，建议针对性练习' : '继续加油，多练习会更好')
const summaryHtml = computed(() => summary.value ? marked(summary.value) : '')
const answerQuality = computed(() => {
  const l = userAnswer.value.length
  if (l < 30) return 'too-short'
  if (l > 400) return 'too-long'
  if (l >= 80) return 'good'
  return 'normal'
})
const answerTip = computed(() => {
  const l = userAnswer.value.length
  if (l < 30) return '再写详细些，至少50字'
  if (l > 400) return '可以精简一些'
  if (l >= 80) return '长度适中 👍'
  return '继续写，100-300字最佳'
})

const typeLabel = (t) => ({ technical: '技术面', behavioral: '行为面', group: '群面' }[t] || t)
const scoreLabel = (s) => s >= 85 ? '出色' : s >= 70 ? '良好' : s >= 60 ? '合格' : '需改进'

const startInterview = async () => {
  starting.value = true
  try {
    const res = await startInterviewApi({ interviewType: interviewType.value, targetPosition: targetPosition.value })
    sessionId.value = res.data.sessionId
    sessionType.value = interviewType.value
    roundNum.value = 1
    currentQuestion.value = res.data.question
    state.value = 'qa'
    answered.value = false
    feedback.value = ''
    userAnswer.value = ''
    isComplete.value = false
    qaHistory.value = []
  } catch { ElMessage.error('启动失败') }
  finally { starting.value = false }
}

const submitAnswer = async () => {
  if (!userAnswer.value.trim()) return
  submitting.value = true
  try {
    const res = await submitAnswerApi({ sessionId: sessionId.value, userAnswer: userAnswer.value })
    feedback.value = res.data.feedback
    lastScore.value = res.data.score
    answered.value = true
    qaHistory.value.push({ roundNum: roundNum.value, question: currentQuestion.value, score: res.data.score, feedback: res.data.feedback })

    if (res.data.isComplete || roundNum.value >= questionCount.value) {
      isComplete.value = true
      totalScore.value = res.data.totalScore || Math.round(qaHistory.value.reduce((s,q) => s+q.score, 0) / qaHistory.value.length)
      summary.value = res.data.summary
      state.value = 'complete'
      loadSessions()
    } else {
      nextQuestion.value = res.data.question
    }
  } catch { ElMessage.error('提交失败') }
  finally { submitting.value = false }
}

const goNext = () => {
  currentQuestion.value = nextQuestion.value
  nextQuestion.value = ''
  userAnswer.value = ''
  feedback.value = ''
  answered.value = false
  roundNum.value++
}

const resetState = () => { state.value = 'setup'; answered.value = false; feedback.value = ''; isComplete.value = false }

const loadSessions = async () => {
  const res = await getSessions()
  sessions.value = res.data || []
}

const loadSession = async (id) => {
  const res = await getDetail(id)
  sessionId.value = id
  state.value = 'complete'
  totalScore.value = res.data.session.totalScore
  summary.value = res.data.session.summaryReport
  qaHistory.value = res.data.qas || []
}

const delSession = async (id) => {
  try { await ElMessageBox.confirm('删除？', '', { type: 'warning' }); await deleteSession(id); sessions.value = sessions.value.filter(s => s.id !== id) } catch {}
}

onMounted(() => loadSessions())
</script>

<style scoped>
.interview-page { max-width: 860px; margin: 0 auto; padding: 4px 0; }
.page-header { margin-bottom: 20px; }
.page-header h2 { font-size: 22px; color: #303133; margin-bottom: 4px; }
.subtitle { color: #909399; font-size: 13px; }

/* Type Cards */
.type-cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; margin-bottom: 20px; }
@media (max-width: 700px) { .type-cards { grid-template-columns: 1fr; } }
.type-card { background: #fff; border-radius: 12px; padding: 20px; cursor: pointer; border: 2px solid #ebeef5; transition: all 0.2s; }
.type-card:hover { border-color: #a0cfff; }
.type-card.active { border-color: #409EFF; background: #ecf5ff; }
.tc-icon { font-size: 36px; margin-bottom: 8px; }
.type-card h3 { margin: 0 0 6px; font-size: 16px; }
.type-card p { font-size: 12px; color: #909399; margin: 0 0 8px; line-height: 1.5; }
.type-card ul { margin: 0; padding-left: 16px; font-size: 12px; color: #606266; }
.type-card.active ul { color: #409EFF; }

/* Setup Form */
.setup-form { background: #fff; border-radius: 12px; padding: 20px 24px; margin-bottom: 20px; }
.setup-options { display: flex; justify-content: space-between; align-items: center; margin-top: 14px; }

/* Prep Tips */
.prep-tips { background: #fff; border-radius: 12px; padding: 22px 24px; }
.prep-tips h3 { margin: 0 0 14px; font-size: 16px; }
.prep-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 16px; }
.prep-item { font-size: 13px; color: #606266; display: flex; align-items: center; gap: 8px; }
.prep-num { width: 22px; height: 22px; border-radius: 50%; background: #ecf5ff; color: #409EFF; display: flex; align-items: center; justify-content: center; font-size: 11px; font-weight: 700; flex-shrink: 0; }
.prep-dosdonts { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.pd-do, .pd-dont { border-radius: 8px; padding: 14px 16px; }
.pd-do { background: #f0f9eb; }
.pd-dont { background: #fef0f0; }
.pd-do h4, .pd-dont h4 { margin: 0 0 4px; font-size: 13px; }
.pd-do h4 { color: #67C23A; }
.pd-dont h4 { color: #F56C6C; }
.pd-do p, .pd-dont p { margin: 0; font-size: 12px; color: #606266; }

/* QA */
.qa-progress { margin-bottom: 20px; background: #fff; border-radius: 10px; padding: 16px 8px; }
.question-bubble { display: flex; gap: 14px; background: #fff; border-radius: 14px; padding: 20px 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); margin-bottom: 20px; }
.qb-avatar { font-size: 40px; flex-shrink: 0; }
.qb-meta { display: flex; justify-content: space-between; font-size: 12px; color: #909399; margin-bottom: 8px; }
.qb-round { color: #409EFF; font-weight: 600; }
.qb-text { font-size: 17px; color: #303133; line-height: 1.8; margin: 0; }

.answer-area { background: #fff; border-radius: 14px; padding: 20px 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); margin-bottom: 20px; }
.answer-header { display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 13px; font-weight: 600; color: #303133; }
.word-guide { font-weight: 400; font-size: 12px; }
.word-guide.too-short { color: #E6A23C; }
.word-guide.good { color: #67C23A; }
.word-guide.too-long { color: #F56C6C; }
.answer-row { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; }
.word-count { font-size: 12px; color: #909399; }

.feedback-card { display: flex; gap: 16px; border-radius: 14px; padding: 20px 24px; margin-bottom: 20px; align-items: center; }
.fb-great { background: #f0f9eb; border: 1px solid #b3e19d; }
.fb-good { background: #ecf5ff; border: 1px solid #a0cfff; }
.fb-poor { background: #fef0f0; border: 1px solid #fab6b6; }
.fb-score-big { font-size: 48px; font-weight: 800; color: #409EFF; flex-shrink: 0; width: 80px; text-align: center; }
.fb-great .fb-score-big { color: #67C23A; }
.fb-poor .fb-score-big { color: #E6A23C; }
.fb-level { font-size: 14px; font-weight: 600; margin-bottom: 4px; }
.fb-text { color: #606266; line-height: 1.7; margin: 0; font-size: 13px; }

/* Complete */
.complete-card { margin-bottom: 16px; }
.final-score { display: flex; align-items: center; gap: 24px; padding: 20px 0; justify-content: center; }
.final-text h3 { margin: 0 0 4px; font-size: 20px; }
.final-text p { margin: 0; font-size: 13px; color: #909399; }
.round-breakdown { margin: 16px 0; }
.rb-item { display: flex; align-items: center; gap: 12px; padding: 10px 0; }
.rb-num { width: 36px; font-weight: 700; font-size: 13px; color: #303133; flex-shrink: 0; }
.rb-body { flex: 1; }
.rb-q { font-size: 12px; color: #909399; margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.rb-score { font-size: 20px; font-weight: 700; width: 36px; text-align: right; flex-shrink: 0; }
.c-green { color: #67C23A; }
.c-blue { color: #409EFF; }
.c-orange { color: #E6A23C; }

.summary-box { background: #fafbfc; border-radius: 8px; padding: 16px 20px; line-height: 1.8; font-size: 14px; color: #303133; margin-bottom: 16px; }
.summary-box :deep(h2), .summary-box :deep(h3) { margin: 10px 0 6px; }
.complete-actions { display: flex; gap: 10px; justify-content: center; }

.history-section { margin-top: 28px; }
.history-section h3 { font-size: 16px; margin-bottom: 12px; }
.history-list { display: flex; flex-direction: column; gap: 6px; }
.hist-item { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: #fff; border-radius: 8px; border: 1px solid #ebeef5; cursor: pointer; transition: all 0.15s; }
.hist-item:hover { border-color: #a0cfff; }
.hist-info { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.hist-title { font-weight: 500; color: #303133; }
.hist-time { font-size: 12px; color: #909399; }
.hist-score { font-size: 15px; font-weight: 700; }
</style>
