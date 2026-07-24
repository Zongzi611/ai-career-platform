<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>实训任务管理</h2>
      <el-button type="primary" @click="openAdd">+ 新增任务</el-button>
    </div>

    <el-table :data="list" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="50" />
      <el-table-column prop="title" label="标题" width="200" show-overflow-tooltip />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="difficulty" label="难度" width="80">
        <template #default="{row}"><el-tag :type="row.difficulty==='HARD'?'danger':row.difficulty==='MEDIUM'?'warning':'success'" size="small">{{ row.difficulty }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{row}">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="del(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" :page-size="size" @current-change="load" layout="prev,pager,next" style="margin-top:16px;justify-content:center" />

    <el-dialog v-model="dialog" :title="isEdit?'编辑任务':'新增任务'" width="650px" top="3vh">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category"><el-option v-for="c in cats" :key="c.value" :label="c.label" :value="c.value" /></el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="form.difficulty"><el-option label="入门" value="EASY" /><el-option label="进阶" value="MEDIUM" /><el-option label="挑战" value="HARD" /></el-select>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="参考答案"><el-input v-model="form.referenceAnswer" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="评分标准"><el-input v-model="form.scoringCriteria" placeholder="格式: 维度(分数):说明;..." /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialog=false">取消</el-button><el-button type="primary" @click="save" :loading="saving">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '../../api/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(15)
const loading = ref(false)
const dialog = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const cats = [{label:'简历优化',value:'resume'},{label:'面试模拟',value:'interview'},{label:'职场沟通',value:'workplace_comm'},{label:'技术实战',value:'tech_practice'},{label:'职业规划',value:'career_plan'},{label:'商业分析',value:'business_analysis'}]
const form = reactive({ title:'',category:'tech_practice',difficulty:'MEDIUM',description:'',referenceAnswer:'',scoringCriteria:'' })

const load = async () => {
  loading.value = true
  const res = await request.get('/training/tasks', { params: { page: page.value, size: size.value } })
  list.value = res.data?.records || []
  total.value = res.data?.total || 0
  loading.value = false
}

const openAdd = () => { isEdit.value = false; Object.assign(form, { title:'',category:'tech_practice',difficulty:'MEDIUM',description:'',referenceAnswer:'',scoringCriteria:'' }); dialog.value = true }
const openEdit = (row) => { isEdit.value = true; Object.assign(form, row); dialog.value = true }

const save = async () => {
  saving.value = true
  if (isEdit.value) { await request.put(`/training/tasks/${form.id}`, form) } else { await request.post('/training/tasks', form) }
  ElMessage.success(isEdit.value?'已更新':'已创建')
  dialog.value = false; saving.value = false; load()
}

const del = async (id) => { try { await ElMessageBox.confirm('删除？','',{type:'warning'}); await request.delete(`/training/tasks/${id}`); ElMessage.success('已删除'); load() } catch {} }

onMounted(load)
</script>

<style scoped>
.admin-page { padding: 4px 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; margin: 0; }
</style>
