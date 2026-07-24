<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>📋 任务管理</h2>
      <el-button type="primary" @click="openAdd">+ 下发任务</el-button>
    </div>

    <el-table :data="tasks" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="50" />
      <el-table-column prop="title" label="标题" width="200" show-overflow-tooltip />
      <el-table-column prop="taskType" label="类型" width="90">
        <template #default="{row}"><el-tag size="small">{{ row.taskType==='ASSESSMENT'?'测评':'实训' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="targetClass" label="目标班级" width="140" />
      <el-table-column prop="deadline" label="截止日期" width="110" />
      <el-table-column label="状态" width="90">
        <template #default="{row}"><el-tag :type="row.status==='ACTIVE'?'success':'info'" size="small">{{ row.status==='ACTIVE'?'进行中':'已关闭' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="完成情况" min-width="150">
        <template #default="{row}"><el-progress :percentage="row.completionRate || 0" :color="'#67C23A'" :stroke-width="10" /></template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{row}">
          <el-button size="small" type="primary" link @click="viewProgress(row)">进度</el-button>
          <el-button size="small" type="danger" link @click="del(row.id)">关闭</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialog" title="下发任务" width="500px" @closed="resetForm">
      <el-form label-width="80px">
        <el-form-item label="标题"><el-input v-model="taskTitle" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="taskDesc" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="taskType"><el-option label="测评任务" value="ASSESSMENT" /><el-option label="实训任务" value="TRAINING" /></el-select></el-form-item>
        <el-form-item label="关联ID"><el-input-number v-model="taskRefId" :min="1" /></el-form-item>
        <el-form-item label="目标班级"><el-input v-model="taskTargetClass" placeholder="输入班级名或 ALL=全体" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog=false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="progressVisible" title="任务进度" width="700px">
      <el-table v-if="progressData.length" :data="progressData" stripe size="small">
        <el-table-column prop="studentName" label="学生" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{row}"><el-tag :type="row.status==='COMPLETED'?'success':'warning'" size="small">{{ row.status==='COMPLETED'?'已完成':'进行中' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="score" label="得分" width="70" />
        <el-table-column prop="submitTime" label="提交时间" width="140" />
      </el-table>
      <el-empty v-else description="暂无进度数据" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../api/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const tasks = ref([])
const loading = ref(false)
const dialog = ref(false)
const creating = ref(false)
const progressVisible = ref(false)
const progressData = ref([])

// Separate refs for form fields
const taskTitle = ref('')
const taskDesc = ref('')
const taskType = ref('ASSESSMENT')
const taskRefId = ref(null)
const taskTargetClass = ref('')

const load = async () => {
  loading.value = true
  try {
    const res = await request.get('/teacher/tasks', { params: { page: 1, size: 50 } })
    tasks.value = res.data?.records || []
  } catch {} finally { loading.value = false }
}

const resetForm = () => {
  taskTitle.value = ''
  taskDesc.value = ''
  taskType.value = 'ASSESSMENT'
  taskRefId.value = null
  taskTargetClass.value = ''
}

const openAdd = () => { resetForm(); dialog.value = true }

const handleCreate = async () => {
  if (!taskTitle.value.trim()) { ElMessage.warning('请输入标题'); return }
  if (!taskTargetClass.value.trim()) { ElMessage.warning('请输入目标班级'); return }
  creating.value = true
  try {
    await request.post('/teacher/tasks', {
      title: taskTitle.value,
      description: taskDesc.value,
      taskType: taskType.value,
      refId: taskRefId.value || null,
      targetClass: taskTargetClass.value
    })
    ElMessage.success('任务已下发')
    dialog.value = false
    load()
  } catch { ElMessage.error('创建失败') }
  finally { creating.value = false }
}

const viewProgress = async (row) => {
  progressVisible.value = true
  try {
    const res = await request.get(`/teacher/tasks/${row.id}/progress`)
    progressData.value = res.data || []
  } catch { progressData.value = [] }
}

const del = async (id) => {
  try { await ElMessageBox.confirm('关闭此任务？','',{type:'warning'}); await request.delete(`/teacher/tasks/${id}`); ElMessage.success('已关闭'); load() } catch {}
}

onMounted(load)
</script>

<style scoped>
.admin-page { padding: 4px 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; margin: 0; }
</style>
