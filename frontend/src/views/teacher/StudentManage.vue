<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>👨‍🎓 学生管理</h2>
      <div style="display:flex;gap:8px">
        <el-button @click="exportStudents">导出 Excel</el-button>
        <el-button type="primary" @click="importVisible = true">批量导入</el-button>
      </div>
    </div>

    <!-- Filters -->
    <div class="filter-row">
      <el-input v-model="className" placeholder="按班级筛选" clearable @keyup.enter="search" style="width:200px" />
      <el-select v-model="collegeFilter" placeholder="按学院筛选" clearable @change="search" style="width:200px">
        <el-option v-for="c in collegeList" :key="c" :label="c" :value="c" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <span class="total-text">共 {{ list.length }} 名学生</span>
    </div>

    <!-- Card Grid -->
    <div v-loading="loading" class="card-grid">
      <div v-for="row in list" :key="row.id" class="student-card" @click="viewDetail(row)">
        <div class="sc-avatar">{{ row.realName?.charAt(0) }}</div>
        <div class="sc-body">
          <div class="sc-name">{{ row.realName }}</div>
          <div class="sc-info">{{ row.college || '未设置学院' }}</div>
          <div class="sc-info">{{ row.major || '未设置专业' }} · {{ row.className || '' }}</div>
          <div class="sc-no" v-if="row.studentNo">学号: {{ row.studentNo }}</div>
        </div>
        <span class="sc-arrow">→</span>
      </div>
    </div>

    <el-empty v-if="!loading && list.length===0" description="暂无学生" />

    <!-- Import Dialog -->
    <el-dialog v-model="importVisible" title="批量导入学生" width="500px">
      <p style="margin-bottom:12px;color:#909399">上传 Excel 文件（.xlsx），表头顺序：姓名 | 学号 | 学院 | 专业 | 班级 | 年级</p>
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :limit="1"
        accept=".xlsx"
        :on-change="handleFileChange"
        :file-list="fileList"
        drag
      >
        <div style="padding:20px;text-align:center">
          <div style="font-size:32px;margin-bottom:8px">📁</div>
          <p>拖拽或点击上传 Excel 文件</p>
          <span style="font-size:12px;color:#909399">默认密码为学号后6位</span>
        </div>
      </el-upload>
      <template #footer>
        <el-button @click="importVisible=false">取消</el-button>
        <el-button type="primary" @click="doImport" :loading="importing" :disabled="!uploadFile">开始导入</el-button>
      </template>
    </el-dialog>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" :title="detailStudent?.realName + ' 的学习档案'" width="800px" top="3vh">
      <div v-if="detailStudent" v-loading="detailLoading">
        <el-descriptions :column="4" border size="small">
          <el-descriptions-item label="姓名">{{ detailStudent.realName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ detailStudent.studentNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学院">{{ detailStudent.college || '-' }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ detailStudent.major || '-' }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ detailStudent.className || '-' }}</el-descriptions-item>
          <el-descriptions-item label="年级">{{ detailStudent.grade || '-' }}</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin-top:20px">🧠 测评记录</h4>
        <el-table v-if="assessments.length" :data="assessments" size="small" stripe>
          <el-table-column prop="typeName" label="测评类型" width="140" />
          <el-table-column prop="resultType" label="结果" width="80" />
          <el-table-column label="AI报告" width="80">
            <template #default="{row}"><el-tag :type="row.isAiGenerated?'success':'info'" size="small">{{ row.isAiGenerated?'已生成':'待生成' }}</el-tag></template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{row}"><el-button size="small" link type="primary" @click="viewReport(row)">查看报告</el-button></template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无测评记录" :image-size="40" />

        <h4 style="margin-top:20px">🏋️ 实训记录</h4>
        <el-table v-if="trainings.length" :data="trainings" size="small" stripe>
          <el-table-column label="任务" show-overflow-tooltip><template #default="{row}">{{ row.taskTitle || '任务#'+row.taskId }}</template></el-table-column>
          <el-table-column prop="aiScore" label="得分" width="70" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{row}"><el-tag :type="row.status==='SCORED'?'success':'warning'" size="small">{{ row.status==='SCORED'?'已评分':'已提交' }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="submitTime" label="提交时间" width="140" />
        </el-table>
        <el-empty v-else description="暂无实训记录" :image-size="40" />

        <el-dialog v-model="reportVisible" title="测评报告" width="700px" append-to-body>
          <div class="report-content" v-html="reportContent"></div>
        </el-dialog>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '../../api/request'
import axios from 'axios'
import { getToken } from '../../utils/auth'
import { marked } from 'marked'
import { ElMessage } from 'element-plus'

const list = ref([])
const className = ref('')
const collegeFilter = ref('')
const loading = ref(false)
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailStudent = ref(null)
const assessments = ref([])
const trainings = ref([])
const reportVisible = ref(false)
const reportContent = ref('')
const importVisible = ref(false)
const uploading = ref(false)
const importing = ref(false)
const uploadFile = ref(null)
const fileList = ref([])

const handleFileChange = (file) => { uploadFile.value = file.raw; fileList.value = [file] }

const doImport = async () => {
  if (!uploadFile.value) { ElMessage.warning('请选择文件'); return }
  importing.value = true
  try {
    const formData = new FormData()
    formData.append('file', uploadFile.value)
    const res = await axios.post('/api/teacher/import-students', formData, {
      headers: { Authorization: `Bearer ${getToken()}`, 'Content-Type': 'multipart/form-data' }
    })
    ElMessage.success(res.data?.data?.message || '导入成功')
    importVisible.value = false; fileList.value = []; uploadFile.value = null
    search()
  } catch { ElMessage.error('导入失败，请检查文件格式') }
  finally { importing.value = false }
}

const collegeList = computed(() => [...new Set(list.value.map(s => s.college).filter(Boolean))].sort())

const search = async () => {
  loading.value = true
  try {
    const params = { page: 1, size: 200 }
    if (className.value) params.className = className.value
    const res = await request.get('/teacher/students', { params })
    let students = res.data?.records || []
    if (collegeFilter.value) students = students.filter(s => s.college === collegeFilter.value)
    list.value = students
  } catch {} finally { loading.value = false }
}

const viewDetail = async (row) => {
  detailStudent.value = row
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await request.get(`/teacher/student-detail/${row.id}`)
    assessments.value = res.data?.assessments || []
    trainings.value = res.data?.trainings || []
  } catch {} finally { detailLoading.value = false }
}

const viewReport = (row) => {
  if (row.reportText) { reportContent.value = marked(row.reportText); reportVisible.value = true }
  else { ElMessage.info('该测评尚未生成AI报告') }
}

const exportStudents = async () => {
  try {
    const token = getToken()
    const res = await axios.get('/api/teacher/export/students', { params: { className: className.value }, responseType: 'blob', headers: { Authorization: `Bearer ${token}` } })
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a'); a.href = url; a.download = `学生名单_${new Date().toISOString().slice(0,10)}.xlsx`; a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch { ElMessage.error('导出失败') }
}

onMounted(search)
</script>

<style scoped>
.admin-page { padding: 4px 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.page-header h2 { font-size: 20px; margin: 0; }
.filter-row { display: flex; gap: 10px; align-items: center; margin-bottom: 16px; }
.total-text { font-size: 13px; color: #909399; }

.card-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}
@media (max-width: 1400px) { .card-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 1000px) { .card-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 600px) { .card-grid { grid-template-columns: 1fr; } }

.student-card {
  display: flex; align-items: center; gap: 12px;
  background: #fff; border-radius: 10px; padding: 14px 16px;
  cursor: pointer; transition: all 0.15s;
  border: 1px solid #ebeef5;
}
.student-card:hover { border-color: #a0cfff; background: #fafcff; transform: translateY(-1px); box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.sc-avatar {
  width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; font-weight: 700; font-size: 16px; flex-shrink: 0;
}
.sc-body { flex: 1; min-width: 0; }
.sc-name { font-weight: 600; font-size: 14px; color: #303133; }
.sc-info { font-size: 12px; color: #909399; margin-top: 1px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.sc-no { font-size: 11px; color: #c0c4cc; margin-top: 2px; }
.sc-arrow { font-size: 16px; color: #c0c4cc; flex-shrink: 0; }

.report-content { line-height: 1.8; font-size: 14px; max-height: 500px; overflow-y: auto; }
</style>
