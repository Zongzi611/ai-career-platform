<template>
  <div class="lp-page">
    <div class="page-header">
      <h2>🎯 学习路径生成器</h2>
      <p class="subtitle">选择目标岗位，AI 生成大学四年完整学习路线图</p>
    </div>

    <!-- Select Career -->
    <div class="select-area">
      <el-select v-model="selectedCareerId" placeholder="选择你的目标岗位..." filterable size="large" style="width:440px" @change="generate">
        <el-option v-for="c in careers" :key="c.id" :label="c.positionName + ' · ' + (c.industry||'')" :value="c.id" />
      </el-select>
      <el-button type="primary" size="large" @click="generate" :loading="loading" :disabled="!selectedCareerId">
        🚀 生成学习路径
      </el-button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-state">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      <p>AI 正在为你规划学习路径...</p>
    </div>

    <!-- Result -->
    <div v-if="data && !loading" class="result-area">
      <!-- Summary -->
      <div class="path-summary">
        <div class="summary-icon">🗺️</div>
        <p>{{ data.summary }}</p>
      </div>

      <!-- Timeline Stages -->
      <div class="stages-timeline">
        <div v-for="(stage, i) in data.stages" :key="i" class="stage-card" :class="'stage-' + i">
          <div class="stage-header">
            <span class="stage-icon">{{ stage.icon }}</span>
            <div>
              <h3>{{ stage.year }} <span class="stage-season">· {{ stage.season }}</span></h3>
              <p class="stage-focus">{{ stage.focus }}</p>
            </div>
          </div>
          <div class="stage-body">
            <div class="stage-col">
              <h4>📚 核心课程</h4>
              <div class="tag-list">
                <el-tag v-for="c in stage.courses" :key="c" size="small" effect="plain" type="primary">{{ c }}</el-tag>
              </div>
            </div>
            <div class="stage-col">
              <h4>💻 实践项目</h4>
              <div class="tag-list">
                <el-tag v-for="p in stage.projects" :key="p" size="small" effect="plain" type="success">{{ p }}</el-tag>
              </div>
            </div>
            <div class="stage-col">
              <h4>🎯 阶段里程碑</h4>
              <ul class="milestones">
                <li v-for="m in stage.milestones" :key="m">{{ m }}</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <!-- Skills + Certs -->
      <div class="bottom-grid">
        <div class="info-card">
          <h3>🛠️ 核心技能栈</h3>
          <div class="skill-cloud">
            <el-tag v-for="sk in data.skills" :key="sk" size="large" effect="dark" type="primary" class="skill-tag">{{ sk }}</el-tag>
          </div>
        </div>
        <div class="info-card">
          <h3>📜 推荐证书</h3>
          <div class="cert-list">
            <div v-for="c in data.certifications" :key="c" class="cert-item">
              <span>✅</span> {{ c }}
            </div>
          </div>
        </div>
      </div>

      <!-- Key Advice -->
      <div class="advice-bar" v-if="data.keyAdvice">
        <span>💡</span> {{ data.keyAdvice }}
      </div>
    </div>

    <!-- Empty -->
    <el-empty v-if="!data && !loading" description="选择一个目标岗位，AI 为你生成专属学习路线" :image-size="70">
      <template #image><span style="font-size:56px">🎓</span></template>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getLearningPath } from '../../api/careerTools'
import { getCareerList } from '../../api/career'
import { ElMessage } from 'element-plus'

const careers = ref([])
const selectedCareerId = ref(null)
const data = ref(null)
const loading = ref(false)

const generate = async () => {
  if (!selectedCareerId.value) return
  loading.value = true
  data.value = null
  try {
    const res = await getLearningPath(selectedCareerId.value)
    data.value = res.data
  } catch {
    ElMessage.error('生成失败，请重试')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const res = await getCareerList({ page: 1, size: 200 })
  careers.value = res.data?.records || []
})
</script>

<style scoped>
.lp-page { padding: 4px 0; }
.page-header { margin-bottom: 20px; }
.page-header h2 { font-size: 22px; color: #303133; margin-bottom: 4px; }
.subtitle { color: #909399; font-size: 13px; }

.select-area { display: flex; gap: 12px; margin-bottom: 28px; }

.loading-state { text-align: center; padding: 60px 0; color: #909399; }
.loading-state p { margin-top: 12px; }

.path-summary {
  display: flex; gap: 14px; align-items: flex-start;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 14px; padding: 20px 24px; color: #fff; margin-bottom: 24px;
}
.summary-icon { font-size: 36px; flex-shrink: 0; }
.path-summary p { margin: 0; font-size: 15px; line-height: 1.7; }

/* Stages */
.stages-timeline { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; margin-bottom: 20px; }
@media (max-width: 900px) { .stages-timeline { grid-template-columns: 1fr; } }

.stage-card {
  background: #fff; border-radius: 14px; padding: 20px 22px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  position: relative; overflow: hidden;
}
.stage-card::before {
  content: ''; position: absolute; left: 0; top: 0; width: 4px; height: 100%;
}
.stage-0::before { background: #409EFF; }
.stage-1::before { background: #67C23A; }
.stage-2::before { background: #E6A23C; }
.stage-3::before { background: #F56C6C; }

.stage-header { display: flex; gap: 12px; align-items: center; margin-bottom: 14px; }
.stage-icon { font-size: 32px; }
.stage-header h3 { margin: 0; font-size: 17px; color: #303133; }
.stage-season { font-size: 12px; color: #909399; font-weight: 400; }
.stage-focus { margin: 2px 0 0; font-size: 12px; color: #909399; }

.stage-body { display: flex; flex-direction: column; gap: 12px; }
.stage-col h4 { margin: 0 0 6px; font-size: 12px; color: #909399; }
.tag-list { display: flex; flex-wrap: wrap; gap: 4px; }
.milestones { margin: 0; padding-left: 16px; font-size: 12px; color: #606266; }
.milestones li { margin-bottom: 2px; }

/* Bottom */
.bottom-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 16px; margin-bottom: 20px; }
@media (max-width: 700px) { .bottom-grid { grid-template-columns: 1fr; } }
.info-card { background: #fff; border-radius: 12px; padding: 18px 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.info-card h3 { margin: 0 0 14px; font-size: 15px; }
.skill-cloud { display: flex; flex-wrap: wrap; gap: 8px; }
.skill-tag { font-size: 13px !important; padding: 6px 14px !important; }
.cert-list { display: flex; flex-direction: column; gap: 8px; font-size: 13px; color: #606266; }

.advice-bar {
  background: #fdf6ec; border: 1px solid #faecd8; border-radius: 10px;
  padding: 14px 20px; font-size: 14px; color: #606266;
  display: flex; gap: 8px; align-items: center;
}
</style>
