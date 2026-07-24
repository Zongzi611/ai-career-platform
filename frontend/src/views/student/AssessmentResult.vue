<template>
  <div class="report-page" v-loading="loading">
    <!-- Hero -->
    <div class="report-hero" v-if="result.resultType">
      <div class="hero-badge">{{ result.typeCode === 'MBTI' ? '🧠' : '🎯' }}</div>
      <div>
        <h2>{{ result.typeName }}</h2>
        <div class="hero-result">
          <span class="result-type">{{ result.resultType }}</span>
          <el-tag v-if="result.isAiGenerated" type="success" effect="plain" size="large">AI 深度分析</el-tag>
          <el-tag v-else type="info" effect="plain" size="large">基础报告</el-tag>
        </div>
      </div>
    </div>

    <!-- Dimension Scores -->
    <div v-if="result.dimensionScores" class="score-section">
      <h3>📊 维度得分</h3>
      <div class="score-grid">
        <div v-for="(v, k) in result.dimensionScores" :key="k" class="score-item">
          <div class="score-bar-wrapper">
            <div class="score-label">{{ k }}</div>
            <el-progress :percentage="Math.round(v / maxScore * 100)" :color="barColor(v)" :stroke-width="14">
              <span class="score-value">{{ v }}分</span>
            </el-progress>
          </div>
        </div>
      </div>
    </div>

    <!-- Report Content -->
    <div v-if="result.reportText" class="report-body">
      <div class="report-content" v-html="renderedReport"></div>
    </div>

    <!-- Actions -->
    <div class="report-actions" v-if="result.id">
      <el-button v-if="!result.isAiGenerated" type="primary" size="large" @click="genReport" :loading="genLoading">
        🤖 生成 AI 深度分析报告
      </el-button>
      <el-button v-else type="primary" size="large" plain @click="genReport" :loading="genLoading">
        🔄 重新生成报告
      </el-button>
      <el-button size="large" @click="$router.push('/student/assessment')">返回测评列表</el-button>
      <el-button v-if="result.reportText" size="large" type="success" @click="printReport">🖨️ 导出PDF</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getResult, generateReport } from '../../api/assessment'
import { marked } from 'marked'
import { ElMessage } from 'element-plus'

const route = useRoute()
const result = ref({})
const genLoading = ref(false)
const loading = ref(true)

const renderedReport = computed(() => {
  if (!result.value.reportText) return ''
  return marked(result.value.reportText, { breaks: true })
})

const maxScore = computed(() => {
  if (!result.value.dimensionScores) return 20
  return Math.max(...Object.values(result.value.dimensionScores), 20)
})

const barColor = (v) => {
  const pct = v / maxScore.value
  return pct >= 0.8 ? '#67C23A' : pct >= 0.6 ? '#409EFF' : pct >= 0.4 ? '#E6A23C' : '#F56C6C'
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getResult(route.params.resultId)
    result.value = res.data || {}
  } finally {
    loading.value = false
  }
})

const printReport = () => { window.print() }

const genReport = async () => {
  genLoading.value = true
  try {
    const res = await generateReport(route.params.resultId)
    result.value = res.data
    ElMessage.success('AI 深度分析报告已生成')
  } catch {
    ElMessage.error('报告生成失败，请稍后重试')
  } finally {
    genLoading.value = false
  }
}
</script>

<style scoped>
.report-page { max-width: 860px; margin: 0 auto; padding: 8px 0; }

/* Hero */
.report-hero {
  display: flex;
  align-items: center;
  gap: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 28px 32px;
  color: #fff;
  margin-bottom: 24px;
}
.hero-badge { font-size: 48px; }
.report-hero h2 { font-size: 22px; margin: 0 0 8px; font-weight: 600; }
.hero-result { display: flex; align-items: center; gap: 12px; }
.result-type {
  font-size: 36px;
  font-weight: 800;
  letter-spacing: 4px;
  font-family: 'Courier New', monospace;
}

/* Scores */
.score-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.score-section h3 { margin: 0 0 20px; font-size: 17px; color: #303133; }
.score-grid { display: flex; flex-direction: column; gap: 14px; }
.score-bar-wrapper { display: flex; align-items: center; gap: 12px; }
.score-label { width: 32px; font-size: 15px; font-weight: 700; color: #303133; text-align: right; flex-shrink: 0; }
.score-value { font-size: 12px; font-weight: 600; }

/* Report Body */
.report-body {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.report-content {
  line-height: 2;
  color: #2c3e50;
  font-size: 18px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', 'Hiragino Sans GB', sans-serif;
  word-wrap: break-word;
  overflow-wrap: break-word;
  word-break: break-all;
}
.report-content :deep(*) { max-width: 100%; }
.report-content :deep(h1) { font-size: 28px; margin: 28px 0 14px; color: #1a1a2e; border-bottom: 2px solid #409EFF; padding-bottom: 10px; line-height: 1.4; }
.report-content :deep(h2) { font-size: 23px; margin: 24px 0 12px; color: #1a1a2e; line-height: 1.4; }
.report-content :deep(h3) { font-size: 19px; margin: 18px 0 10px; color: #3a3a5c; line-height: 1.4; }
.report-content :deep(p) { margin: 0 0 12px; }
.report-content :deep(strong) { color: #337ecc; font-weight: 600; }
.report-content :deep(blockquote) { border-left: 4px solid #409EFF; padding: 10px 18px; margin: 14px 0; background: #f4f8fc; border-radius: 0 8px 8px 0; color: #5a6a7e; overflow-wrap: break-word; }
.report-content :deep(ul), .report-content :deep(ol) { padding-left: 24px; margin: 10px 0; }
.report-content :deep(li) { margin-bottom: 6px; line-height: 1.8; }
.report-content :deep(code) { background: #f0f2f5; padding: 2px 6px; border-radius: 4px; font-size: 13px; font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace; color: #e74c3c; word-break: break-all; }
.report-content :deep(pre) { background: #f6f8fa; padding: 14px 18px; border-radius: 8px; overflow-x: auto; white-space: pre-wrap; word-wrap: break-word; font-size: 13px; line-height: 1.6; }
.report-content :deep(pre code) { background: none; padding: 0; color: #2c3e50; }
.report-content :deep(table) { width: 100%; border-collapse: collapse; margin: 16px 0; font-size: 14px; display: block; overflow-x: auto; }
.report-content :deep(th) { background: #f0f3f8; padding: 10px 14px; text-align: left; font-weight: 600; border-bottom: 2px solid #dcdfe6; white-space: nowrap; color: #1a1a2e; }
.report-content :deep(td) { padding: 10px 14px; border-bottom: 1px solid #ebeef5; color: #3a3a5c; }
.report-content :deep(hr) { border: none; border-top: 1px solid #e8ecf1; margin: 28px 0; }
.report-content :deep(img) { max-width: 100%; height: auto; border-radius: 6px; }

/* Actions */
.report-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  padding: 20px 0;
}
</style>
