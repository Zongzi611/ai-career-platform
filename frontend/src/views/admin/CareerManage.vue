<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>职业知识库管理</h2>
      <div>
        <el-button type="success" @click="reindex" :loading="indexing">向量索引重建</el-button>
        <el-button type="primary" @click="openAdd">+ 新增岗位</el-button>
      </div>
    </div>

    <!-- Search -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索岗位名称/行业/技能" clearable @keyup.enter="load" style="width:300px" />
      <el-select v-model="industryFilter" placeholder="行业筛选" clearable @change="load" style="width:200px">
        <el-option v-for="i in industries" :key="i" :label="i" :value="i" />
      </el-select>
      <el-button type="primary" @click="load">搜索</el-button>
    </div>

    <!-- Table -->
    <el-table :data="list" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="positionName" label="岗位名称" width="160" />
      <el-table-column prop="industry" label="行业" width="140" />
      <el-table-column label="薪资(K)" width="100">
        <template #default="{row}">{{ row.salaryMin }}-{{ row.salaryMax }}</template>
      </el-table-column>
      <el-table-column prop="skillsRequired" label="技能要求" min-width="180" show-overflow-tooltip />
      <el-table-column label="需求" width="80">
        <template #default="{row}"><el-tag :type="row.demandLevel==='HIGH'?'danger':'warning'" size="small">{{ row.demandLevel }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{row}">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="del(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" :page-size="size" @current-change="load" layout="prev,pager,next" style="margin-top:16px;justify-content:center" />

    <!-- Dialog -->
    <el-dialog v-model="dialog" :title="isEdit?'编辑岗位':'新增岗位'" width="650px" top="3vh">
      <el-form :model="form" label-width="90px">
        <el-form-item label="岗位名称"><el-input v-model="form.positionName" /></el-form-item>
        <el-form-item label="行业"><el-input v-model="form.industry" /></el-form-item>
        <el-form-item label="匹配专业"><el-input v-model="form.majorMatch" placeholder="逗号分隔" /></el-form-item>
        <el-form-item label="最低薪资(K)"><el-input-number v-model="form.salaryMin" :min="1" :max="99" /></el-form-item>
        <el-form-item label="最高薪资(K)"><el-input-number v-model="form.salaryMax" :min="1" :max="99" /></el-form-item>
        <el-form-item label="技能要求"><el-input v-model="form.skillsRequired" type="textarea" :rows="2" placeholder="逗号分隔" /></el-form-item>
        <el-form-item label="晋升路径"><el-input v-model="form.careerPath" /></el-form-item>
        <el-form-item label="岗位描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="需求等级">
          <el-select v-model="form.demandLevel"><el-option label="高需求" value="HIGH" /><el-option label="中等" value="MEDIUM" /><el-option label="一般" value="LOW" /></el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog=false">取消</el-button>
        <el-button type="primary" @click="save" :loading="saving">保存</el-button>
      </template>
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
const keyword = ref('')
const industryFilter = ref('')
const industries = ref([])
const loading = ref(false)
const indexing = ref(false)
const dialog = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const form = reactive({ positionName:'', industry:'', majorMatch:'', salaryMin:5, salaryMax:15, skillsRequired:'', careerPath:'', description:'', demandLevel:'MEDIUM' })

const load = async () => {
  loading.value = true
  const res = await request.get('/career/list', { params: { page: page.value, size: size.value, keyword: keyword.value, industry: industryFilter.value } })
  list.value = res.data?.records || []
  total.value = res.data?.total || 0
  loading.value = false
}

const reindex = async () => {
  indexing.value = true
  await request.post('/career/reindex')
  ElMessage.success('索引重建已启动')
  indexing.value = false
}

const openAdd = () => {
  isEdit.value = false
  Object.assign(form, { id:null, positionName:'', industry:'', majorMatch:'', salaryMin:5, salaryMax:15, skillsRequired:'', careerPath:'', description:'', demandLevel:'MEDIUM' })
  dialog.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialog.value = true
}

const save = async () => {
  saving.value = true
  if (isEdit.value) {
    await request.put(`/career/${form.id}`, form)
  } else {
    await request.post('/career', form)
  }
  ElMessage.success(isEdit.value ? '已更新' : '已创建')
  dialog.value = false
  saving.value = false
  load()
}

const del = async (id) => {
  try { await ElMessageBox.confirm('删除此岗位？','提示',{type:'warning'}); await request.delete(`/career/${id}`); ElMessage.success('已删除'); load() } catch {}
}

onMounted(async () => {
  await load()
  const r = await request.get('/career/industries')
  industries.value = r.data || []
})
</script>

<style scoped>
.admin-page { padding: 4px 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { font-size: 20px; margin: 0; }
.search-bar { display: flex; gap: 10px; margin-bottom: 16px; }
</style>
