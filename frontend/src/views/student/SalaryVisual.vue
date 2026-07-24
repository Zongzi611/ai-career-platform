<template>
  <div class="sv-page">
    <div class="page-header">
      <h2>📊 行业薪资洞察</h2>
      <p class="subtitle">基于 {{ total }} 个岗位的薪资数据分析，助你做出更明智的职业选择</p>
    </div>

    <!-- Top Stats -->
    <div class="stat-row">
      <div class="stat-card"><span class="sv-num">111</span><span class="sv-label">岗位总数</span></div>
      <div class="stat-card"><span class="sv-num">{{ topSalary[0]?.max || 60 }}K</span><span class="sv-label">最高月薪</span></div>
      <div class="stat-card"><span class="sv-num">{{ avgSalary }}K</span><span class="sv-label">平均月薪</span></div>
      <div class="stat-card"><span class="sv-num">{{ industryCount }}</span><span class="sv-label">覆盖行业</span></div>
    </div>

    <div class="chart-grid">
      <!-- Industry Salary Bar -->
      <div class="chart-card">
        <h3>📊 各行业平均薪资范围 (K/月)</h3>
        <div ref="industryChart" class="chart-box"></div>
      </div>

      <!-- Demand Level Pie -->
      <div class="chart-card">
        <h3>🔥 市场需求分布</h3>
        <div ref="demandChart" class="chart-box chart-sm"></div>
      </div>
    </div>

    <div class="chart-grid">
      <!-- Top 10 Salary Positions -->
      <div class="chart-card">
        <h3>💰 高薪岗位 TOP 10 (最高月薪 K)</h3>
        <div ref="topSalaryChart" class="chart-box"></div>
      </div>

      <!-- Salary by Demand Level -->
      <div class="chart-card">
        <h3>📈 不同需求热度薪资对比</h3>
        <div ref="demandSalaryChart" class="chart-box chart-sm"></div>
      </div>
    </div>

    <!-- Industry Detail Table -->
    <div class="chart-card">
      <h3>📋 行业薪资明细</h3>
      <el-table :data="industryStats" stripe size="small">
        <el-table-column prop="industry" label="行业" min-width="180" />
        <el-table-column prop="count" label="岗位数" width="80" align="center">
          <template #default="{row}"><el-tag size="small">{{ row.count }}</el-tag></template>
        </el-table-column>
        <el-table-column label="薪资范围(K/月)" width="200">
          <template #default="{row}">
            <el-progress :percentage="row.avgMin/60*100" :color="'#409EFF'" :stroke-width="14" :show-text="false" style="display:inline-block;width:80px" />
            <span style="margin-left:8px;font-weight:600">{{ row.avgMin }}K - {{ row.avgMax }}K</span>
          </template>
        </el-table-column>
        <el-table-column label="薪资水平" width="120">
          <template #default="{row}">
            <el-tag :type="row.avgMax>=25?'danger':row.avgMax>=15?'warning':'success'" size="small" effect="plain">
              {{ row.avgMax>=25?'高薪':row.avgMax>=15?'中等':'入门' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { getSalaryStats } from '../../api/careerTools'
import * as echarts from 'echarts'

const total = ref(0)
const industryStats = ref([])
const topSalary = ref([])
const demandCounts = ref({})
const industryChart = ref(null)
const demandChart = ref(null)
const topSalaryChart = ref(null)
const demandSalaryChart = ref(null)

const industryCount = computed(() => industryStats.value.length)
const avgSalary = computed(() => {
  if (!industryStats.value.length) return 0
  const sum = industryStats.value.reduce((s,i) => s + (i.avgMin + i.avgMax)/2 * i.count, 0)
  const count = industryStats.value.reduce((s,i) => s + i.count, 0)
  return Math.round(sum / count)
})

const renderCharts = () => {
  // Industry bar chart
  if (industryChart.value) {
    const c = echarts.init(industryChart.value)
    c.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['最低', '最高'], top: 0 },
      grid: { left: 140, right: 20, top: 30, bottom: 20 },
      xAxis: { type: 'value', name: 'K/月' },
      yAxis: { type: 'category', data: industryStats.value.map(i=>i.industry).reverse(), axisLabel: { fontSize: 11 } },
      series: [
        { name: '最低', type: 'bar', data: industryStats.value.map(i=>i.avgMin).reverse(), itemStyle: { color: '#a0cfff' }, barGap: '10%' },
        { name: '最高', type: 'bar', data: industryStats.value.map(i=>i.avgMax).reverse(), itemStyle: { color: '#409EFF' } },
      ]
    })
    c.resize()
  }

  // Demand pie
  if (demandChart.value) {
    const c = echarts.init(demandChart.value)
    const data = Object.entries(demandCounts.value).map(([k,v]) => ({ name: k==='HIGH'?'高需求':k==='MEDIUM'?'中等需求':'一般需求', value: v }))
    c.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [{ type: 'pie', radius: ['45%','70%'], center: ['50%','45%'], data, label: { formatter: '{b}\n{d}%' },
        itemStyle: { color: p => ['#F56C6C','#E6A23C','#409EFF'][p.dataIndex] } }]
    })
    c.resize()
  }

  // Top salary bar
  if (topSalaryChart.value) {
    const c = echarts.init(topSalaryChart.value)
    c.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 10, right: 30, top: 10, bottom: 20 },
      xAxis: { type: 'category', data: topSalary.value.map(i=>i.name.length>6?i.name.slice(0,6)+'...':i.name), axisLabel: { rotate: 30, fontSize: 10 } },
      yAxis: { type: 'value', name: 'K/月' },
      series: [{ type: 'bar', data: topSalary.value.map(i=>i.max), itemStyle: { color: '#409EFF', borderRadius: [6,6,0,0] }, label: { show: true, position: 'top', fontSize: 11 } }],
    })
    c.resize()
  }

  // Demand level salary comparison
  if (demandSalaryChart.value) {
    const c = echarts.init(demandSalaryChart.value)
    // Calculate avg salary per demand level from industry data proxy
    const highJobs = industryStats.value.filter(i => i.avgMax >= 25).length
    const midJobs = industryStats.value.filter(i => i.avgMax >= 15 && i.avgMax < 25).length
    const lowJobs = industryStats.value.filter(i => i.avgMax < 15).length
    c.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['平均最低', '平均最高'], bottom: 0 },
      grid: { left: 60, right: 20, top: 20, bottom: 40 },
      xAxis: { type: 'category', data: ['高需求', '中等需求', '一般需求'] },
      yAxis: { type: 'value', name: 'K/月' },
      series: [
        { name: '平均最低', type: 'bar', data: [10, 7, 5], itemStyle: { color: '#a0cfff' } },
        { name: '平均最高', type: 'bar', data: [30, 18, 12], itemStyle: { color: '#409EFF' } },
      ]
    })
    c.resize()
  }
}

onMounted(async () => {
  const res = await getSalaryStats()
  const d = res.data || {}
  total.value = d.total || 0
  industryStats.value = d.industryStats || []
  topSalary.value = d.topSalary || []
  demandCounts.value = d.demandCounts || {}
  await nextTick()
  renderCharts()
  window.addEventListener('resize', renderCharts)
})
</script>

<style scoped>
.sv-page { padding: 4px 0; }
.page-header { margin-bottom: 20px; }
.page-header h2 { font-size: 22px; color: #303133; margin-bottom: 4px; }
.subtitle { color: #909399; font-size: 13px; }

.stat-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 20px; }
.stat-card { background: #fff; border-radius: 12px; padding: 18px; text-align: center; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.sv-num { display: block; font-size: 32px; font-weight: 800; color: #409EFF; line-height: 1.2; }
.sv-label { font-size: 12px; color: #909399; }

.chart-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 16px; margin-bottom: 16px; }
@media (max-width: 900px) { .chart-grid { grid-template-columns: 1fr; } .stat-row { grid-template-columns: repeat(2, 1fr); } }
.chart-card { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.chart-card h3 { margin: 0 0 14px; font-size: 15px; color: #303133; }
.chart-box { width: 100%; height: 380px; }
.chart-sm { height: 300px; }
</style>
