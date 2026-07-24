<template>
  <div class="exam-page">
    <div class="page-header">
      <h2>📝 模拟笔试</h2>
      <p class="subtitle">AI 出题官为你的目标岗位生成 20 道笔试题，在线作答自动判分</p>
    </div>

    <!-- Setup -->
    <div v-if="!state || state==='setup'" class="setup-card">
      <el-card>
        <template #header><strong>新笔试</strong></template>
        <el-form label-width="100px">
          <el-form-item label="笔试类型">
            <el-radio-group v-model="examType">
              <el-radio-button value="technical">💻 技术笔试</el-radio-button>
              <el-radio-button value="general">📋 综合笔试</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="目标岗位">
            <el-input v-model="targetPosition" placeholder="如 Java后端开发工程师" style="max-width:400px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" @click="startExam" :loading="starting">
              {{ starting ? `AI 出题中... 已等待 ${elapsed} 秒 (20题约需1-2分钟)` : '开始笔试 (20题)' }}
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- Exam in progress -->
    <div v-if="state==='exam'" class="exam-body">
      <el-steps :active="currentIdx" align-center style="margin-bottom:20px">
        <el-step v-for="i in questions.length" :key="i" :title="'Q'+i" />
      </el-steps>

      <div v-for="(q, i) in questions" :key="i" v-show="i === currentIdx" class="q-card">
        <div class="q-num">第 {{ i+1 }}/{{ questions.length }} 题</div>
        <h3 class="q-text">{{ q.question }}</h3>
        <el-radio-group v-model="userAnswers[i]" class="q-options">
          <el-radio v-for="(opt, oi) in q.options" :key="oi" :value="opt.substring(0,1)" size="large" class="q-opt">{{ opt }}</el-radio>
        </el-radio-group>
        <div class="q-nav">
          <el-button :disabled="i===0" @click="currentIdx--">上一题</el-button>
          <el-button v-if="i<questions.length-1" type="primary" @click="currentIdx++" :disabled="!userAnswers[i]">下一题</el-button>
          <el-button v-else type="success" size="large" @click="submitExam" :loading="submitting" :disabled="userAnswers.some(a=>!a)">提交答卷</el-button>
        </div>
      </div>
    </div>

    <!-- Result -->
    <div v-if="state==='result'" class="result-card">
      <el-card>
        <template #header><strong>📊 笔试结果</strong></template>
        <div class="score-hero">
          <el-progress type="circle" :percentage="score" :width="120" :stroke-width="8" :color="score>=80?'#67C23A':score>=60?'#409EFF':'#E6A23C'" />
          <div>
            <h3>{{ score>=80?'优秀！':score>=60?'合格':'继续加油' }}</h3>
            <p>答对 {{ correct }} / {{ total }} 题，得分 {{ score }} 分</p>
          </div>
        </div>
        <div class="answer-review">
          <div v-for="(r, i) in results" :key="i" class="ar-item" :class="{correct: r.correct, wrong: !r.correct}">
            <div class="ar-header">
              <span class="ar-num">Q{{ i+1 }}</span>
              <el-tag :type="r.correct?'success':'danger'" size="small">{{ r.correct?'正确':'错误' }}</el-tag>
            </div>
            <p class="ar-q">{{ r.question }}</p>
            <p class="ar-a">你的答案：<strong>{{ r.userAnswer }}</strong> | 正确答案：<strong class="c-green">{{ r.correctAnswer }}</strong></p>
            <p class="ar-e" v-if="r.explanation">💡 {{ r.explanation }}</p>
          </div>
        </div>
        <div style="text-align:center;margin-top:16px">
          <el-button type="primary" @click="resetExam">再来一次</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '../../api/request'
import { ElMessage } from 'element-plus'

const state = ref('setup')
const examType = ref('technical')
const targetPosition = ref('')
const starting = ref(false)
const submitting = ref(false)
const questions = ref([])
const userAnswers = ref([])
const currentIdx = ref(0)
const score = ref(0)
const correct = ref(0)
const total = ref(0)
const results = ref([])
const elapsed = ref(0)
let timer = null

const startExam = async () => {
  starting.value = true
  elapsed.value = 0
  timer = setInterval(() => { elapsed.value++ }, 1000)
  try {
    const res = await request.post('/exam/start', { examType: examType.value, targetPosition: targetPosition.value }, { timeout: 300000 })
    questions.value = res.data?.questions || []
    userAnswers.value = new Array(questions.value.length).fill('')
    currentIdx.value = 0
    state.value = 'exam'
  } catch { ElMessage.error('出题失败') }
  finally { starting.value = false; clearInterval(timer) }
}

const submitExam = async () => {
  submitting.value = true
  try {
    const answers = questions.value.map((q, i) => ({ question: q.question, userAnswer: userAnswers.value[i] || '', correctAnswer: q.answer, explanation: q.explanation }))
    const res = await request.post('/exam/submit', { answers })
    score.value = res.data.score; correct.value = res.data.correct; total.value = res.data.total; results.value = res.data.results
    state.value = 'result'
  } catch { ElMessage.error('提交失败') }
  finally { submitting.value = false }
}

const resetExam = () => { state.value = 'setup'; questions.value = []; userAnswers.value = []; currentIdx.value = 0 }
</script>

<style scoped>
.exam-page { max-width: 800px; margin: 0 auto; padding: 4px 0; }
.page-header { margin-bottom: 20px; }
.page-header h2 { font-size: 22px; color: #303133; margin-bottom: 4px; }
.subtitle { color: #909399; font-size: 13px; }

.setup-card { max-width: 500px; }

.q-card { background: #fff; border-radius: 14px; padding: 28px 32px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.q-num { font-size: 13px; color: #909399; margin-bottom: 8px; }
.q-text { font-size: 18px; color: #303133; line-height: 1.7; margin: 0 0 20px; }
.q-options { display: flex; flex-direction: column; gap: 10px; margin-bottom: 20px; }
.q-opt { padding: 12px 16px; border: 1px solid #e8ecf1; border-radius: 10px; width: 100%; margin: 0; transition: border-color 0.2s; }
.q-opt:hover { border-color: #a0cfff; }
.q-nav { display: flex; justify-content: space-between; }

.result-card { margin-top: 16px; }
.score-hero { display: flex; align-items: center; gap: 24px; padding: 20px 0; justify-content: center; }
.score-hero h3 { font-size: 22px; margin: 0 0 4px; }
.score-hero p { margin: 0; font-size: 14px; color: #909399; }

.answer-review { margin-top: 20px; }
.ar-item { padding: 14px 16px; border-radius: 10px; margin-bottom: 10px; border: 1px solid #ebeef5; }
.ar-item.correct { background: #f0f9eb; border-color: #c2e7b0; }
.ar-item.wrong { background: #fef0f0; border-color: #fbc4c4; }
.ar-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.ar-num { font-weight: 700; font-size: 13px; color: #303133; }
.ar-q { font-size: 14px; color: #303133; margin: 6px 0; }
.ar-a { font-size: 13px; color: #606266; margin: 4px 0; }
.ar-e { font-size: 12px; color: #909399; margin: 4px 0 0; }
.c-green { color: #67C23A; }
</style>
