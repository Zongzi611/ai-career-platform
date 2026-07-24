<template>
  <div class="admin-page">
    <h2>系统配置</h2>

    <el-table :data="configs" stripe v-loading="loading" style="margin-top:16px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="configKey" label="配置键" width="220" />
      <el-table-column prop="configValue" label="配置值" min-width="300">
        <template #default="{row}">
          <span v-if="!row._editing" style="white-space:pre-wrap">{{ row.configValue }}</span>
          <el-input v-else v-model="row._editValue" type="textarea" :rows="3" />
        </template>
      </el-table-column>
      <el-table-column prop="description" label="说明" width="160" show-overflow-tooltip />
      <el-table-column label="状态" width="80">
        <template #default="{row}"><el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'启用':'禁用' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{row}">
          <template v-if="!row._editing">
            <el-button size="small" @click="startEdit(row)">编辑</el-button>
          </template>
          <template v-else>
            <el-button size="small" type="primary" @click="saveEdit(row)">保存</el-button>
            <el-button size="small" @click="cancelEdit(row)">取消</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && configs.length===0" description="暂无配置" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../api/request'
import { ElMessage } from 'element-plus'

const configs = ref([])
const loading = ref(false)

const load = async () => {
  loading.value = true
  const res = await request.get('/admin/configs')
  configs.value = (res.data || []).map(c => ({ ...c, _editing: false, _editValue: c.configValue }))
  loading.value = false
}

const startEdit = (row) => { row._editing = true; row._editValue = row.configValue }
const cancelEdit = (row) => { row._editing = false; row._editValue = row.configValue }

const saveEdit = async (row) => {
  await request.put(`/admin/configs/${row.id}`, { configValue: row._editValue })
  row.configValue = row._editValue
  row._editing = false
  ElMessage.success('已保存')
}

onMounted(load)
</script>

<style scoped>
.admin-page { padding: 4px 0; }
.admin-page h2 { font-size: 20px; }
</style>
