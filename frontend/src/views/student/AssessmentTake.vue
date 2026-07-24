<template>
  <div>
    <h2>{{ typeName }}</h2>
    <el-progress :percentage="Math.round((current + 1) / questions.length * 100)" style="margin:16px 0" />
    <el-card v-if="questions.length > 0">
      <h3>{{ current + 1 }}. {{ questions[current].questionText }}</h3>
      <el-radio-group v-model="selectedOption" style="display:flex;flex-direction:column;gap:8px;margin:16px 0">
        <el-radio v-for="opt in questions[current].options" :key="opt.id" :value="opt.id" size="large">{{ opt.optionText }}</el-radio>
      </el-radio-group>
      <div style="display:flex;justify-content:space-between">
        <el-button :disabled="current===0" @click="current--">上一题</el-button>
        <el-button type="primary" @click="next" :loading="submitting">{{ current >= questions.length-1 ? '提交' : '下一题' }}</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getQuestions, startAssessment, submitAnswer, completeAssessment } from '../../api/assessment'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const typeId = route.params.typeId
const typeName = ref('')
const questions = ref([])
const current = ref(0)
const selectedOption = ref(null)
const recordId = ref(null)
const submitting = ref(false)

onMounted(async () => {
  const qRes = await getQuestions(typeId)
  questions.value = qRes.data || []
  const sRes = await startAssessment(typeId)
  recordId.value = sRes.data.recordId
})

const next = async () => {
  if (!selectedOption.value) { ElMessage.warning('请选择一个选项'); return }
  submitting.value = true
  await submitAnswer({ recordId: recordId.value, questionId: questions.value[current.value].id, optionId: selectedOption.value })
  if (current.value >= questions.value.length - 1) {
    const res = await completeAssessment(recordId.value)
    ElMessage.success('测评完成')
    router.push(`/student/result/${res.data.id}`)
  } else {
    current.value++
    selectedOption.value = null
  }
  submitting.value = false
}
</script>
