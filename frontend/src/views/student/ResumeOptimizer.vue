<template>
  <div class="resume-page">
    <div class="page-header">
      <h2>📝 AI 简历优化</h2>
      <p class="subtitle">粘贴你的简历草稿，AI 从 HR 视角帮你改写为专业简历</p>
    </div>

    <div class="resume-layout">
      <!-- Left: Input -->
      <div class="resume-left">
        <el-input v-model="targetPosition" placeholder="目标岗位（如：Java后端开发工程师）" size="large" clearable class="pos-input" />

        <!-- File Upload -->
        <el-upload
          class="upload-area"
          drag
          :auto-upload="false"
          :show-file-list="true"
          :on-change="handleFileChange"
          :limit="1"
          accept=".pdf,.docx,.txt,.md,.java,.py,.html,.css,.js,.json,.xml,.csv"
        >
          <div class="upload-content">
            <div class="upload-icon">📁</div>
            <p>点击或拖拽上传简历文件</p>
            <span>支持 PDF / DOCX / TXT 等格式，最大 10MB</span>
          </div>
        </el-upload>

        <el-input
          v-model="originalText"
          type="textarea"
          :rows="14"
          placeholder="在此粘贴你的简历草稿，或上传文件自动填充...&#10;&#10;可以包含：&#10;- 个人基本信息（姓名、学校、专业）&#10;- 教育背景&#10;- 项目经历&#10;- 技能特长&#10;- 实习/实践经历"
          class="resume-input"
        />

        <el-button type="primary" size="large" @click="handleOptimize" :loading="optimizing" :disabled="!originalText.trim()" class="opt-btn">
          🤖 AI 优化简历
        </el-button>

        <!-- Quick tips -->
        <div class="tips">
          <h4>💡 优化提示</h4>
          <ul>
            <li>填写目标岗位可获得更精准的关键词匹配</li>
            <li>尽可能详细描述项目经历，AI 会用 STAR 法则改写</li>
            <li>包含量化数据（如用户数、性能提升百分比）效果更好</li>
          </ul>
        </div>
      </div>

      <!-- Right: Result -->
      <div class="resume-right">
        <div v-if="!optimizedText && !optimizing" class="empty-result">
          <div class="empty-icon">📄</div>
          <p>AI 优化后的简历将显示在这里</p>
        </div>

        <div v-if="optimizing" class="loading-result">
          <el-icon class="is-loading" :size="40"><Loading /></el-icon>
          <p>AI 正在分析你的简历，根据目标岗位进行优化...</p>
        </div>

        <div v-if="optimizedText && !optimizing" class="result-panel">
          <div class="result-header">
            <span>✨ 优化结果</span>
            <div>
              <el-button size="small" @click="copyResult" :type="copied ? 'success' : 'default'">
                {{ copied ? '已复制' : '复制全文' }}
              </el-button>
              <el-button size="small" @click="downloadPDF">导出 TXT</el-button>
            </div>
          </div>
          <div class="result-content" v-html="renderedResult"></div>
        </div>
      </div>
    </div>

    <!-- History -->
    <div class="history-section" v-if="history.length > 0">
      <h3>📋 历史记录</h3>
      <div class="history-list">
        <div v-for="h in history" :key="h.id" class="history-item" @click="loadHistory(h)">
          <div class="hist-info">
            <span class="hist-title">{{ h.title }}</span>
            <span class="hist-pos" v-if="h.targetPosition">{{ h.targetPosition }}</span>
            <span class="hist-time">{{ h.createTime?.substring(0,16) }}</span>
          </div>
          <el-button size="small" text type="danger" @click.stop="handleDelete(h.id)">删除</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { optimizeResume, getResumeHistory, getResumeDetail, deleteResume, uploadResumeFile } from '../../api/resume'
import { marked } from 'marked'
import { ElMessage, ElMessageBox } from 'element-plus'

const originalText = ref('')
const targetPosition = ref('')
const optimizedText = ref('')
const optimizing = ref(false)
const copied = ref(false)
const history = ref([])
const uploading = ref(false)

const handleFileChange = async (file) => {
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file.raw)
    const res = await uploadResumeFile(formData)
    originalText.value = res.data.text
    // Auto-detect position from filename
    if (!targetPosition.value && res.data.filename) {
      const name = res.data.filename.replace(/\.[^.]+$/, '')
      if (name.length > 2) targetPosition.value = name
    }
    ElMessage.success(`已提取 ${res.data.length} 个字符`)
  } catch {
    ElMessage.error('文件解析失败，请手动粘贴')
  } finally {
    uploading.value = false
  }
}

const renderedResult = ref('')

const handleOptimize = async () => {
  if (!originalText.value.trim()) return
  optimizing.value = true
  optimizedText.value = ''
  try {
    const res = await optimizeResume({
      originalText: originalText.value,
      targetPosition: targetPosition.value,
      title: (targetPosition.value || '通用') + '简历优化'
    })
    optimizedText.value = res.data.optimizedText
    renderedResult.value = marked(res.data.optimizedText)
    ElMessage.success('简历优化完成')
    loadHistory()
  } catch {
    ElMessage.error('优化失败，请稍后重试')
  } finally {
    optimizing.value = false
  }
}

const copyResult = async () => {
  await navigator.clipboard.writeText(optimizedText.value)
  copied.value = true
  ElMessage.success('已复制到剪贴板')
  setTimeout(() => copied.value = false, 2000)
}

const downloadPDF = () => {
  const blob = new Blob([optimizedText.value], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${targetPosition.value || 'resume'}_optimized.txt`
  a.click()
  URL.revokeObjectURL(url)
}

const loadHistory = async () => {
  const res = await getResumeHistory()
  history.value = res.data || []
}

const loadHistoryItem = (item) => {
  originalText.value = item.originalText || ''
  targetPosition.value = item.targetPosition || ''
  optimizedText.value = item.optimizedText || ''
  renderedResult.value = item.optimizedText ? marked(item.optimizedText) : ''
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除此记录？', '提示', { type: 'warning' })
    await deleteResume(id)
    history.value = history.value.filter(h => h.id !== id)
    ElMessage.success('已删除')
  } catch {}
}

onMounted(() => loadHistory())
</script>

<style scoped>
.resume-page { padding: 4px 0; }
.page-header { margin-bottom: 20px; }
.page-header h2 { font-size: 22px; color: #303133; margin-bottom: 4px; }
.subtitle { color: #909399; font-size: 13px; }

.resume-layout {
  display: flex;
  gap: 20px;
  min-height: calc(100vh - 340px);
}

.resume-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.resume-right {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #ebeef5;
  min-height: 400px;
}

.pos-input { max-width: 400px; }

.upload-area { margin-bottom: 4px; }
.upload-content { text-align: center; padding: 8px 0; }
.upload-icon { font-size: 36px; margin-bottom: 6px; }
.upload-content p { margin: 4px 0; font-size: 14px; color: #606266; }
.upload-content span { font-size: 12px; color: #909399; }

.resume-input :deep(textarea) {
  font-family: 'SF Mono', 'Consolas', 'Microsoft YaHei', monospace;
  font-size: 14px;
  line-height: 1.7;
}

.opt-btn { align-self: flex-start; }

.tips {
  background: #f0f9eb;
  border-radius: 8px;
  padding: 14px 18px;
}
.tips h4 { margin: 0 0 8px; font-size: 13px; color: #67C23A; }
.tips ul { margin: 0; padding-left: 18px; font-size: 12px; color: #606266; line-height: 1.8; }

.empty-result, .loading-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 400px;
  color: #909399;
}
.empty-icon { font-size: 48px; margin-bottom: 12px; }

.result-panel { padding: 20px 24px; }
.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  font-weight: 600;
  font-size: 15px;
}
.result-content {
  line-height: 1.9;
  font-size: 15px;
  color: #303133;
  white-space: pre-wrap;
  word-wrap: break-word;
}
.result-content :deep(h2), .result-content :deep(h3) { margin: 14px 0 6px; }
.result-content :deep(ul) { padding-left: 20px; }
.result-content :deep(li) { margin-bottom: 4px; }
.result-content :deep(strong) { color: #409EFF; }

.history-section { margin-top: 28px; }
.history-section h3 { font-size: 16px; margin-bottom: 12px; }
.history-list { display: flex; flex-direction: column; gap: 6px; }
.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: all 0.15s;
}
.history-item:hover { border-color: #a0cfff; background: #fafcff; }
.hist-info { display: flex; gap: 12px; align-items: center; }
.hist-title { font-weight: 500; color: #303133; }
.hist-pos { font-size: 12px; color: #409EFF; background: #ecf5ff; padding: 1px 8px; border-radius: 4px; }
.hist-time { font-size: 12px; color: #909399; }
</style>
