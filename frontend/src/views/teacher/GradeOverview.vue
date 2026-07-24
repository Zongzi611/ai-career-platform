<template>
  <div class="admin-page">
    <h2>📊 成绩总览</h2>

    <div class="filter-row">
      <el-select v-model="collegeFilter" placeholder="按学院筛选" clearable @change="load" style="width:200px">
        <el-option v-for="c in collegeList" :key="c" :label="c" :value="c" />
      </el-select>
      <span class="total-text">共 {{ list.length }} 名学生</span>
    </div>

    <el-table :data="list" stripe v-loading="loading" border max-height="calc(100vh - 200px)" style="font-size:15px">
      <el-table-column prop="realName" label="姓名" width="100" fixed="left" />
      <el-table-column prop="college" label="学院" width="200" show-overflow-tooltip />
      <el-table-column prop="major" label="专业" width="170" show-overflow-tooltip />
      <el-table-column prop="className" label="班级" width="160" />
      <el-table-column prop="mbti" label="MBTI" width="90" align="center">
        <template #default="{row}"><el-tag v-if="row.mbti!=='-'" type="primary">{{ row.mbti }}</el-tag><span v-else>-</span></template>
      </el-table-column>
      <el-table-column prop="holland" label="霍兰德" width="100" align="center">
        <template #default="{row}"><el-tag v-if="row.holland!=='-'" type="success">{{ row.holland }}</el-tag><span v-else>-</span></template>
      </el-table-column>
      <el-table-column label="实训进度" width="150" align="center">
        <template #default="{row}">
          <span>{{ row.trainingDone }}/{{ row.trainingTotal }}</span>
          <el-progress :percentage="row.trainingTotal>0?Math.round(row.trainingDone/row.trainingTotal*100):0" :stroke-width="8" :show-text="false" style="width:80px;display:inline-block;margin-left:8px" />
        </template>
      </el-table-column>
      <el-table-column prop="avgScore" label="实训均分" width="100" align="center" sortable>
        <template #default="{row}">
          <span :class="row.avgScore>=80?'c-green':row.avgScore>=60?'c-blue':row.avgScore==='-'?'':'c-red'" style="font-weight:700;font-size:16px">{{ row.avgScore }}</span>
        </template>
      </el-table-column>
      <el-table-column label="任务数" width="90" align="center">
        <template #default="{row}">{{ row.assignedTotal || 0 }}</template>
      </el-table-column>
      <el-table-column label="评价" width="100" align="center">
        <template #default="{row}">
          <el-tag v-if="row.avgScore>=80" type="success">优秀</el-tag>
          <el-tag v-else-if="row.avgScore>=60" type="warning">良好</el-tag>
          <el-tag v-else-if="row.avgScore!=='-'" type="danger">加油</el-tag>
          <span v-else style="color:#909399">未开始</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../api/request'

const list = ref([])
const loading = ref(false)
const collegeFilter = ref('')
const collegeList = ref([])

const load = async () => {
  loading.value = true
  try {
    const params = {}
    if (collegeFilter.value) params.college = collegeFilter.value
    const res = await request.get('/teacher/grade-overview', { params })
    list.value = res.data || []
    collegeList.value = [...new Set(list.value.map(s => s.college).filter(Boolean))].sort()
  } catch {} finally { loading.value = false }
}

onMounted(load)
</script>

<style scoped>
.admin-page { padding: 4px 0; }
.admin-page h2 { font-size: 20px; margin-bottom: 14px; }
.filter-row { display: flex; gap: 10px; align-items: center; margin-bottom: 14px; }
.total-text { font-size: 13px; color: #909399; }
.c-green { color: #67C23A; }
.c-blue { color: #409EFF; }
.c-red { color: #F56C6C; }
</style>
