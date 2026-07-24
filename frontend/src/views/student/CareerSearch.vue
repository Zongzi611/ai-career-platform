<template>
  <div class="career-page">
    <div class="page-header">
      <h2>职业探索</h2>
      <p class="subtitle">探索 31 个岗位，覆盖 16 个行业，AI 智能匹配你的专业</p>
    </div>

    <div class="career-layout">
      <!-- Left Sidebar -->
      <div class="career-sidebar">
        <!-- Search -->
        <el-input v-model="query.keyword" placeholder="搜索岗位、技能..." clearable @keyup.enter="search" class="side-search" />

        <!-- Quick Industry Filter -->
        <div class="side-section">
          <h4>行业筛选</h4>
          <div class="industry-list">
            <div
              v-for="i in industries" :key="i"
              class="industry-item"
              :class="{ active: query.industry === i }"
              @click="query.industry = query.industry === i ? '' : i; search()"
            >{{ i }}</div>
          </div>
        </div>

        <!-- Difficulty Filter -->
        <div class="side-section">
          <h4>需求热度</h4>
          <el-radio-group v-model="demandFilter" size="small" @change="search">
            <el-radio-button value="">全部</el-radio-button>
            <el-radio-button value="HIGH">高需求</el-radio-button>
            <el-radio-button value="MEDIUM">中等</el-radio-button>
          </el-radio-group>
        </div>

        <!-- AI Recommend Button -->
        <el-button type="success" @click="aiRecommend" :loading="recommendLoading" class="ai-btn">
          AI 智能推荐
        </el-button>
      </div>

      <!-- Right Main Area -->
      <div class="career-main">
        <!-- AI Recommend Cards -->
        <div v-if="mode === 'recommend' && recommendList.length > 0">
          <div class="section-title">
            <span>为你推荐</span>
            <el-tag type="success" size="small" effect="plain">基于专业+测评匹配</el-tag>
            <span class="back-link" @click="search">返回全部岗位</span>
          </div>
          <div class="recommend-cards">
            <div v-for="(r, i) in recommendList" :key="r.id" class="rec-card" @click="showDetail(r)">
              <div class="rec-rank" :class="'rank-' + (i+1)">{{ i + 1 }}</div>
              <div class="rec-body">
                <div class="rec-title-row">
                  <h3>{{ r.positionName }}</h3>
                  <span class="star-btn" :class="{starred: bookmarkIds.has(r.id)}" @click.stop="toggleBookmark(r.id)">{{ bookmarkIds.has(r.id) ? '⭐' : '☆' }}</span>
                </div>
                <p class="rec-industry">{{ r.industry }}</p>
                <div class="rec-tags">
                  <el-tag v-for="sk in (r.skillsRequired||'').split(',').slice(0,3)" :key="sk" size="small" effect="plain">{{ sk.trim() }}</el-tag>
                </div>
                <div class="rec-footer">
                  <el-progress :percentage="Math.round((r.similarityScore || 0) * 100)" :color="'#409EFF'" :stroke-width="5" style="flex:1" />
                  <span class="rec-match">{{ Math.round((r.similarityScore || 0) * 100) }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Search Results Table -->
        <div v-if="mode === 'search'">
          <div class="section-title">
            <span>岗位列表</span>
            <span class="result-count">共 {{ total }} 个</span>
          </div>
          <el-table :data="list" stripe style="width:100%">
            <el-table-column prop="positionName" label="岗位名称" min-width="160" />
            <el-table-column prop="industry" label="行业" min-width="130" />
            <el-table-column label="薪资(K/月)" width="110">
              <template #default="{row}">{{ row.salaryMin }}-{{ row.salaryMax }}K</template>
            </el-table-column>
            <el-table-column label="需求" width="90">
              <template #default="{row}">
                <el-tag :type="row.demandLevel==='HIGH'?'danger':row.demandLevel==='MEDIUM'?'warning':'info'" size="small">
                  {{ row.demandLevel === 'HIGH' ? '高' : row.demandLevel === 'MEDIUM' ? '中' : '一般' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="技能要求" min-width="200" show-overflow-tooltip>
              <template #default="{row}">{{ row.skillsRequired }}</template>
            </el-table-column>
            <el-table-column label="操作" width="130">
              <template #default="{row}">
                <el-button size="small" type="primary" link @click="showDetail(row)">详情</el-button>
                <el-button size="small" :type="bookmarkIds.has(row.id)?'warning':'info'" link @click="toggleBookmark(row.id)">
                  {{ bookmarkIds.has(row.id) ? '⭐' : '☆' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination v-model:current-page="page" :total="total" :page-size="size" @current-change="search" layout="prev,pager,next" style="margin-top:20px;justify-content:center" />
        </div>
      </div>
    </div>

    <!-- Detail Dialog -->
    <el-dialog v-model="dialogVisible" :title="detail?.positionName" width="650px" top="5vh">
      <div v-if="detail" class="detail-content">
        <div class="detail-row"><span class="detail-label">行业</span><el-tag>{{ detail.industry }}</el-tag></div>
        <div class="detail-row"><span class="detail-label">薪资范围</span><span class="detail-value salary">{{ detail.salaryMin }}K - {{ detail.salaryMax }}K / 月</span></div>
        <div class="detail-row"><span class="detail-label">匹配专业</span><span class="detail-value">{{ detail.majorMatch }}</span></div>
        <div class="detail-row"><span class="detail-label">所需技能</span><div class="skill-tags"><el-tag v-for="sk in (detail.skillsRequired||'').split(',')" :key="sk" size="small" effect="plain" style="margin:2px">{{ sk.trim() }}</el-tag></div></div>
        <div class="detail-row"><span class="detail-label">晋升路径</span><span class="detail-value">{{ detail.careerPath }}</span></div>
        <div class="detail-row"><span class="detail-label">岗位描述</span><p class="detail-desc">{{ detail.description }}</p></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCareerList, getRecommend, getIndustries } from '../../api/career'
import { getBookmarkIds, addBookmark, removeBookmark } from '../../api/careerTools'
import { ElMessage } from 'element-plus'

const list = ref([])
const recommendList = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const mode = ref('search')
const query = reactive({ keyword: '', industry: '' })
const demandFilter = ref('')
const industries = ref([])
const dialogVisible = ref(false)
const detail = ref(null)
const recommendLoading = ref(false)

const search = async () => {
  mode.value = 'search'
  try {
    const params = { page: page.value, size: size.value, keyword: query.keyword || '', industry: query.industry || '' }
    if (demandFilter.value) params.demandLevel = demandFilter.value
    const res = await getCareerList(params)
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {}
}

const aiRecommend = async () => {
  mode.value = 'recommend'
  recommendLoading.value = true
  try {
    const userStr = localStorage.getItem('qkc_user')
    let major = ''
    if (userStr) {
      try { const user = JSON.parse(userStr); major = user.major || '' } catch {}
    }
    const res = await getRecommend({ major, topK: 9 })
    recommendList.value = (res.data || []).map(r => ({ ...r, similarityScore: r.similarityScore || 0 }))
    if (recommendList.value.length === 0) ElMessage.info('暂无推荐，请完善专业信息')
  } catch { ElMessage.error('推荐不可用') }
  finally { recommendLoading.value = false }
}

const bookmarkIds = ref(new Set())
const toggleBookmark = async (careerId) => {
  try {
    if (bookmarkIds.value.has(careerId)) {
      await removeBookmark(careerId)
      bookmarkIds.value.delete(careerId)
      ElMessage.success('已取消收藏')
    } else {
      await addBookmark(careerId)
      bookmarkIds.value.add(careerId)
      ElMessage.success('已收藏')
    }
  } catch { ElMessage.error('操作失败') }
}

const showDetail = (row) => { detail.value = row; dialogVisible.value = true }

onMounted(async () => {
  const [r, bm] = await Promise.all([getIndustries(), getBookmarkIds()])
  industries.value = r.data || []
  bookmarkIds.value = new Set(bm.data || [])
  search()
})
</script>

<style scoped>
.career-page { padding: 4px 0; }
.page-header { margin-bottom: 20px; }
.page-header h2 { font-size: 22px; color: #303133; margin-bottom: 4px; }
.subtitle { color: #909399; font-size: 13px; }

/* Layout */
.career-layout { display: flex; gap: 20px; align-items: flex-start; min-height: calc(100vh - 200px); }

/* Sidebar */
.career-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  position: sticky;
  top: 8px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.04);
  align-self: stretch;
}

.side-search { margin-bottom: 16px; }

.side-section { margin-bottom: 16px; }
.side-section h4 { font-size: 13px; color: #909399; margin-bottom: 8px; font-weight: 600; }

.industry-list { display: flex; flex-direction: column; gap: 6px; max-height: none; overflow-y: visible; }
.industry-item {
  font-size: 13px; padding: 9px 12px; border-radius: 6px; cursor: pointer; line-height: 1.6; min-height: 24px;
  color: #606266; transition: all 0.15s; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  display: block;
}
.industry-item:hover { background: #ecf5ff; color: #409EFF; }
.industry-item.active { background: #409EFF; color: #fff; }

.ai-btn { width: 100%; margin-top: 4px; }

/* Main Area */
.career-main { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.career-main > div { flex: 1; display: flex; flex-direction: column; }
.career-main .el-table { flex: 1; }

.section-title {
  display: flex; align-items: center; gap: 10px; margin-bottom: 14px; font-size: 15px; font-weight: 600;
}
.back-link { font-size: 12px; color: #409EFF; cursor: pointer; margin-left: auto; font-weight: 400; }
.result-count { font-size: 13px; color: #909399; font-weight: 400; }

/* Recommend Cards */
.recommend-cards { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
@media (min-width: 1400px) { .recommend-cards { grid-template-columns: repeat(3, 1fr); } }

.rec-card {
  display: flex; gap: 12px; background: #fff; border-radius: 10px; padding: 16px;
  cursor: pointer; transition: all 0.2s; border: 1px solid #ebeef5;
}
.rec-card:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,0.08); border-color: #a0cfff; }

.rec-rank {
  width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center;
  justify-content: center; font-weight: 700; font-size: 15px; flex-shrink: 0;
  background: #f0f2f5; color: #909399;
}
.rec-rank.rank-1 { background: #fff3e0; color: #e65100; }
.rec-rank.rank-2 { background: #e8eaf6; color: #283593; }
.rec-rank.rank-3 { background: #fce4ec; color: #880e4f; }

.rec-body { flex: 1; min-width: 0; }
.rec-title-row { display: flex; justify-content: space-between; align-items: flex-start; }
.rec-body h3 { font-size: 15px; color: #303133; margin-bottom: 3px; flex: 1; }
.star-btn { font-size: 18px; cursor: pointer; transition: transform 0.15s; flex-shrink: 0; }
.star-btn:hover { transform: scale(1.2); }
.star-btn.starred { filter: none; }
.rec-industry { font-size: 12px; color: #909399; margin-bottom: 6px; }
.rec-tags { display: flex; gap: 4px; flex-wrap: wrap; margin-bottom: 8px; }
.rec-footer { display: flex; align-items: center; gap: 8px; }
.rec-match { font-size: 12px; color: #409EFF; font-weight: 500; white-space: nowrap; }

/* Detail */
.detail-content { line-height: 1.8; }
.detail-row { margin-bottom: 16px; }
.detail-label { font-weight: 600; color: #303133; display: block; margin-bottom: 4px; font-size: 13px; }
.detail-value { color: #606266; font-size: 14px; }
.detail-desc { white-space: pre-wrap; color: #606266; font-size: 14px; line-height: 1.8; }
.salary { font-size: 18px; color: #409EFF; font-weight: 700; }
.skill-tags { display: flex; flex-wrap: wrap; gap: 4px; }
</style>
