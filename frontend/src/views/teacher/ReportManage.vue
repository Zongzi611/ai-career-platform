<template>
  <div class="admin-page">
    <h2>📄 报告管理</h2>

    <el-table :data="reports" stripe v-loading="loading" style="margin-top:16px">
      <el-table-column prop="id" label="ID" width="80">
        <template #default="{row}">{{ row.id > 100000 ? 'A'+(row.id-100000) : row.id }}</template>
      </el-table-column>
      <el-table-column prop="reportTitle" label="报告标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="reportType" label="类型" width="120">
        <template #default="{row}"><el-tag size="small">{{ row.reportType==='ASSESSMENT_SUMMARY'?'测评报告':row.reportType==='TRAINING_SUMMARY'?'实训报告':'综合报告' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="150" />
      <el-table-column label="操作" width="130">
        <template #default="{row}">
          <el-button size="small" type="primary" link @click="view(row)">查看报告</el-button>
          <el-button size="small" type="danger" link @click="del(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="viewVisible" title="" width="750px" top="3vh" class="report-dialog">
      <div v-if="currentReport" class="report-wrapper">
        <div class="report-cover">
          <span class="cover-icon">📊</span>
          <h2>{{ currentReport.reportTitle }}</h2>
          <div class="cover-meta">
            <el-tag size="small">{{ currentReport.reportType==='ASSESSMENT_SUMMARY'?'测评报告':'实训报告' }}</el-tag>
            <span>{{ currentReport.createTime?.substring(0,10) }}</span>
          </div>
        </div>
        <div class="report-body" v-html="renderedContent"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../api/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { marked } from 'marked'

const reports = ref([])
const loading = ref(false)
const viewVisible = ref(false)
const currentReport = ref(null)
const renderedContent = ref('')

const load = async () => {
  loading.value = true
  const res = await request.get('/teacher/reports', { params: { page: 1, size: 50 } })
  reports.value = res.data?.records || []
  loading.value = false
}

const view = async (row) => {
  currentReport.value = row
  try {
    const res = await request.get(`/teacher/reports/${row.id}`)
    const content = res.data?.reportContent || row.reportContent || '暂无内容'
    renderedContent.value = marked(content, { breaks: true })
  } catch {
    renderedContent.value = marked(row.reportContent || '暂无内容', { breaks: true })
  }
  viewVisible.value = true
}

const del = async (id) => {
  try { await ElMessageBox.confirm('删除此报告？','',{type:'warning'}); await request.delete(`/teacher/reports/${id}`); ElMessage.success('已删除'); load() } catch {}
}

onMounted(load)
</script>

<style scoped>
.admin-page { padding: 4px 0; }
.admin-page h2 { font-size: 20px; }
</style>

<style>
.report-dialog .el-dialog__body { padding: 0; }
.report-wrapper { overflow: hidden; }
.report-cover {
  background: linear-gradient(135deg, #1a1a2e, #16213e, #0f3460);
  padding: 32px 36px; color: #fff; text-align: center;
}
.cover-icon { font-size: 40px; display: block; margin-bottom: 8px; }
.report-cover h2 { margin: 0 0 10px; font-size: 20px; font-weight: 600; }
.cover-meta { display: flex; gap: 10px; justify-content: center; align-items: center; font-size: 12px; opacity: 0.7; }

.report-body {
  padding: 28px 40px;
  font-size: 24px;
  line-height: 1.8;
  color: #333;
  font-family: 'PingFang SC', 'Microsoft YaHei', 'Hiragino Sans GB', -apple-system, BlinkMacSystemFont, sans-serif;
  word-break: break-all; overflow-wrap: anywhere;
  max-width: 100%; overflow-x: hidden; overflow-y: auto;
  white-space: normal;
}
.report-body h2 { font-size: 30px; }
.report-body h3 { font-size: 26px; }
.report-body * {
  max-width: 100% !important;
  word-break: break-all !important;
  overflow-wrap: anywhere !important;
  white-space: normal !important;
}
.report-body pre, .report-body code {
  white-space: pre-wrap !important;
  word-break: break-all !important;
}
.report-body table {
  display: block; max-width: 100%; overflow-x: auto;
}
.report-body h2 {
  font-size: 22px; font-weight: 700; color: #1a1a2e;
  margin: 36px 0 16px; padding-bottom: 12px;
  border-bottom: 2px solid #e8ecf1;
}
.report-body h2:first-child { margin-top: 0; }
.report-body h3 {
  font-size: 18px; font-weight: 600; color: #2c3e50;
  margin: 24px 0 10px;
}
.report-body p { margin: 0 0 14px; text-indent: 0; }
.report-body strong { color: #2c3e50; font-weight: 700; }
.report-body ul, .report-body ol { margin: 10px 0; padding-left: 24px; }
.report-body li { margin-bottom: 6px; }
.report-body blockquote {
  margin: 16px 0; padding: 14px 20px;
  background: #fafbfc; border-left: 3px solid #409EFF;
  border-radius: 4px; color: #555; font-size: 15px;
}
.report-body table { width: 100%; border-collapse: collapse; margin: 16px 0; font-size: 14px; }
.report-body th { background: #f5f7fa; padding: 10px 14px; text-align: left; font-weight: 600; color: #333; border-bottom: 2px solid #e8ecf1; }
.report-body td { padding: 10px 14px; border-bottom: 1px solid #f0f2f5; color: #555; }
.report-body hr { border: none; border-top: 1px solid #e8ecf1; margin: 32px 0; }
.report-body pre { background: #f6f8fa; padding: 16px 20px; border-radius: 8px; overflow-x: auto; font-size: 14px; line-height: 1.6; }
.report-body code { background: #f0f2f5; padding: 2px 6px; border-radius: 4px; font-size: 13px; color: #c7254e; }
.report-body pre code { background: none; padding: 0; color: #333; }
</style>
