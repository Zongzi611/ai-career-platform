<template>
  <div class="dash-page">
    <h2>📊 学情仪表板</h2>

    <!-- Stats -->
    <div class="stat-row" v-loading="loading">
      <div class="stat-card"><span class="sv-num">{{ dash.totalStudents || 0 }}</span><span class="sv-label">学生总数</span></div>
      <div class="stat-card"><span class="sv-num">{{ dash.totalAssessments || 0 }}</span><span class="sv-label">测评次数</span></div>
      <div class="stat-card"><span class="sv-num">{{ dash.totalTrainings || 0 }}</span><span class="sv-label">实训提交</span></div>
      <div class="stat-card"><span class="sv-num">{{ (dash.avgTrainingScore || 0).toFixed(1) }}</span><span class="sv-label">实训均分</span></div>
    </div>

    <!-- Charts -->
    <div class="chart-row">
      <div class="chart-card">
        <h3>🧠 学生 MBTI 分布</h3>
        <div ref="mbtiChart" class="chart-box"></div>
      </div>
      <div class="chart-card">
        <h3>💼 职业需求热度</h3>
        <div ref="demandChart" class="chart-box"></div>
      </div>
    </div>

    <!-- Recent Activity -->
    <div class="chart-card">
      <h3>📋 最近活动</h3>
      <el-table :data="recentActivity" stripe size="small">
        <el-table-column prop="studentName" label="学生" width="100" />
        <el-table-column prop="action" label="操作" min-width="200" />
        <el-table-column prop="time" label="时间" width="160" />
        <el-table-column label="详情" width="80">
          <template #default><el-button size="small" link type="primary">查看</el-button></template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import request from '../../api/request'
import * as echarts from 'echarts'

const dash = ref({})
const loading = ref(false)
const recentActivity = ref([])
const mbtiChart = ref(null)
const demandChart = ref(null)

onMounted(async () => {
  loading.value = true
  try {
    const res = await request.get('/dashboard/overview')
    dash.value = res.data || {}
    // Build recent activity from available data
    if (dash.value.recentActivities) {
      recentActivity.value = dash.value.recentActivities
    }
    await nextTick()
    renderCharts()
  } catch {} finally { loading.value = false }
})

const renderCharts = () => {
  // MBTI pie
  if (mbtiChart.value && dash.value.mbtiDistribution?.length) {
    const chart = echarts.init(mbtiChart.value)
    chart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0, textStyle: { fontSize: 10 } },
      series: [{ type: 'pie', radius: ['40%','70%'], center: ['50%','45%'],
        data: dash.value.mbtiDistribution.map(d => ({ name: d.name, value: d.value })),
        label: { formatter: '{b}\n{d}%' } }]
    })
  }
  // Career demand bar
  if (demandChart.value && dash.value.careerDemand?.length) {
    const chart = echarts.init(demandChart.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: dash.value.careerDemand.map(d => d.name), axisLabel: { rotate: 15, fontSize: 10 } },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: dash.value.careerDemand.map(d => d.value), itemStyle: { color: '#409EFF', borderRadius: [6,6,0,0] } }],
      grid: { left: 40, right: 10, top: 10, bottom: 50 }
    })
  }
}
</script>

<style scoped>
.dash-page { padding: 4px 0; }
.dash-page h2 { font-size: 20px; margin-bottom: 20px; }
.stat-row { display: grid; grid-template-columns: repeat(4,1fr); gap: 14px; margin-bottom: 20px; }
.stat-card { background: #fff; border-radius: 12px; padding: 20px; text-align: center; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.sv-num { display: block; font-size: 32px; font-weight: 800; color: #409EFF; line-height: 1.2; }
.sv-label { font-size: 12px; color: #909399; }
.chart-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px; }
.chart-card { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); margin-bottom: 16px; }
.chart-card h3 { margin: 0 0 14px; font-size: 15px; }
.chart-box { height: 300px; }
@media (max-width: 800px) { .stat-row { grid-template-columns: repeat(2,1fr); } .chart-row { grid-template-columns: 1fr; } }
</style>
