<template>
  <div class="tools-page">
    <div class="page-header">
      <h2>📌 我的收藏 & 技能分析</h2>
      <p class="subtitle">收藏心仪岗位，横向对比薪资技能，分析你与目标岗位的差距</p>
    </div>

    <!-- Tabs -->
    <el-tabs v-model="activeTab" class="main-tabs">
      <!-- Tab 1: Bookmarks -->
      <el-tab-pane label="⭐ 岗位收藏" name="bookmarks">
        <div v-if="bookmarks.length > 0">
          <!-- Compare Table -->
          <div class="compare-table-wrapper">
            <table class="compare-table">
              <thead>
                <tr>
                  <th>岗位</th>
                  <th v-for="b in bookmarks" :key="b.id">{{ b.positionName }}</th>
                </tr>
              </thead>
              <tbody>
                <tr><td class="row-label">行业</td><td v-for="b in bookmarks" :key="b.id">{{ b.industry }}</td></tr>
                <tr><td class="row-label">薪资(K/月)</td><td v-for="b in bookmarks" :key="b.id" class="salary-cell">{{ b.salaryMin }}-{{ b.salaryMax }}K</td></tr>
                <tr><td class="row-label">需求热度</td><td v-for="b in bookmarks" :key="b.id"><el-tag :type="b.demandLevel==='HIGH'?'danger':'warning'" size="small">{{ b.demandLevel==='HIGH'?'高需求':'中等' }}</el-tag></td></tr>
                <tr><td class="row-label">匹配专业</td><td v-for="b in bookmarks" :key="b.id" class="text-cell">{{ b.majorMatch }}</td></tr>
                <tr><td class="row-label">所需技能</td><td v-for="b in bookmarks" :key="b.id" class="text-cell"><el-tag v-for="sk in (b.skillsRequired||'').split(',').slice(0,5)" :key="sk" size="small" effect="plain" style="margin:1px">{{ sk.trim() }}</el-tag></td></tr>
                <tr><td class="row-label">晋升路径</td><td v-for="b in bookmarks" :key="b.id" class="text-cell path-cell">{{ b.careerPath }}</td></tr>
                <tr><td class="row-label">操作</td><td v-for="b in bookmarks" :key="b.id">
                  <el-button size="small" type="primary" link @click="analyzeGap(b)">技能差距</el-button>
                  <el-button size="small" type="danger" link @click="removeBookmark(b.id)">取消收藏</el-button>
                </td></tr>
              </tbody>
            </table>
          </div>
        </div>
        <el-empty v-else description="还没有收藏岗位，去职业探索页面添加吧">
          <el-button type="primary" @click="$router.push('/student/career')">去探索</el-button>
        </el-empty>
      </el-tab-pane>

      <!-- Tab 2: Skill Gap -->
      <el-tab-pane label="🎯 技能差距分析" name="skillgap">
        <div class="skillgap-layout">
          <!-- Select target job -->
          <div class="sg-select">
            <el-select v-model="selectedCareerId" placeholder="选择目标岗位" filterable size="large" @change="runAnalysis" style="width:400px">
              <el-option v-for="c in allCareers" :key="c.id" :label="c.positionName + ' (' + c.industry + ')'" :value="c.id" />
            </el-select>
            <el-button type="primary" size="large" @click="runAnalysis" :loading="analyzing" :disabled="!selectedCareerId">开始分析</el-button>
          </div>

          <!-- Result -->
          <div v-if="gapResult" class="sg-result">
            <div class="gap-header">
              <span class="gap-icon">🎯</span>
              <div>
                <h3>{{ gapResult.positionName }}</h3>
                <p class="gap-skills">{{ gapResult.requiredSkills }}</p>
              </div>
            </div>
            <div class="gap-analysis" v-html="renderedAnalysis"></div>
          </div>

          <el-empty v-if="!gapResult && !analyzing" description="选择一个目标岗位，AI 分析你与它的技能差距" :image-size="60" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getBookmarks, getBookmarkIds, addBookmark, removeBookmark, analyzeSkillGap } from '../../api/careerTools'
import { getCareerList } from '../../api/career'
import { marked } from 'marked'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('bookmarks')
const bookmarks = ref([])
const allCareers = ref([])
const selectedCareerId = ref(null)
const gapResult = ref(null)
const analyzing = ref(false)

const renderedAnalysis = ref('')

const loadBookmarks = async () => {
  const res = await getBookmarks()
  bookmarks.value = res.data || []
}

const removeBookmarkHandler = async (careerId) => {
  try {
    await ElMessageBox.confirm('取消收藏此岗位？', '提示', { type: 'warning' })
    await removeBookmark(careerId)
    bookmarks.value = bookmarks.value.filter(b => b.id !== careerId)
    ElMessage.success('已取消')
  } catch {}
}

const analyzeGap = async (career) => {
  activeTab.value = 'skillgap'
  selectedCareerId.value = career.id
  runAnalysis()
}

const runAnalysis = async () => {
  if (!selectedCareerId.value) return
  analyzing.value = true
  gapResult.value = null
  try {
    const res = await analyzeSkillGap(selectedCareerId.value)
    gapResult.value = res.data
    renderedAnalysis.value = marked(res.data.analysis || '')
  } catch {
    ElMessage.error('分析失败')
  } finally {
    analyzing.value = false
  }
}

onMounted(async () => {
  await loadBookmarks()
  const cr = await getCareerList({ page: 1, size: 200 })
  allCareers.value = cr.data?.records || []
})
</script>

<style scoped>
.tools-page { padding: 4px 0; }
.page-header { margin-bottom: 20px; }
.page-header h2 { font-size: 22px; color: #303133; margin-bottom: 4px; }
.subtitle { color: #909399; font-size: 13px; }
.main-tabs { margin-top: 8px; }

/* Compare Table */
.compare-table-wrapper { overflow-x: auto; margin-bottom: 20px; }
.compare-table { width: 100%; border-collapse: collapse; min-width: 600px; }
.compare-table { border: 2px solid #c0c4cc; border-collapse: collapse; }
.compare-table th, .compare-table td { padding: 14px 16px; border: 1px solid #c8ccd4; text-align: center; font-size: 14px; vertical-align: top; }
.compare-table th { background: #e8ecf1; font-weight: 700; color: #303133; min-width: 120px; white-space: nowrap; border-bottom: 2px solid #b0b4bc; }
.compare-table .row-label { background: #f0f2f5; font-weight: 700; color: #303133; text-align: right; white-space: nowrap; width: 90px; border-right: 2px solid #c0c4cc; }
.compare-table .salary-cell { font-size: 16px; font-weight: 700; color: #409EFF; }
.compare-table .text-cell { text-align: left; font-size: 12px; line-height: 1.6; max-width: 200px; }
.compare-table .path-cell { font-size: 11px; color: #909399; }

/* Skill Gap */
.skillgap-layout { max-width: 860px; }
.sg-select { display: flex; gap: 12px; margin-bottom: 20px; }
.sg-result { background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.gap-header { display: flex; align-items: flex-start; gap: 14px; margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid #ebeef5; }
.gap-icon { font-size: 32px; }
.gap-header h3 { margin: 0 0 4px; font-size: 18px; color: #303133; }
.gap-skills { margin: 0; font-size: 12px; color: #909399; }
.gap-analysis { line-height: 1.9; font-size: 15px; color: #303133; }
.gap-analysis :deep(h2), .gap-analysis :deep(h3) { margin: 16px 0 8px; }
.gap-analysis :deep(strong) { color: #409EFF; }
.gap-analysis :deep(ul) { padding-left: 20px; }
.gap-analysis :deep(li) { margin-bottom: 6px; }
</style>
