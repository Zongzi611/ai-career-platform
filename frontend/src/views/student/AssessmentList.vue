<template>
  <div>
    <h2>职业测评</h2>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="12" v-for="t in types" :key="t.id">
        <el-card>
          <h3>{{ t.name }}</h3>
          <p style="color:#909399; margin:8px 0">{{ t.description?.substring(0,80) }}...</p>
          <el-button type="primary" @click="$router.push(`/student/assessment/${t.id}`)">开始测评</el-button>
        </el-card>
      </el-col>
    </el-row>
    <h3 style="margin-top:24px">我的测评结果</h3>
    <el-table :data="results" stripe>
      <el-table-column prop="typeName" label="测评类型" />
      <el-table-column prop="resultType" label="结果" width="100" />
      <el-table-column label="操作" width="200"><template #default="{row}">
        <el-button size="small" @click="$router.push(`/student/result/${row.id}`)">查看报告</el-button>
        <el-button v-if="!row.isAiGenerated" size="small" type="success" @click="genReport(row.id)">生成AI报告</el-button>
      </template></el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTypes, getMyResults, generateReport } from '../../api/assessment'
import { ElMessage } from 'element-plus'

const types = ref([])
const results = ref([])

onMounted(async () => {
  const [t, r] = await Promise.all([getTypes(), getMyResults()])
  types.value = t.data || []
  results.value = r.data || []
})

const genReport = async (id) => {
  await generateReport(id)
  ElMessage.success('AI报告已生成')
  const r = await getMyResults()
  results.value = r.data || []
}
</script>
