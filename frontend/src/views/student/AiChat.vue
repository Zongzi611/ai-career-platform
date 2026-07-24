<template>
  <div class="chat-container">
    <!-- Sidebar -->
    <div class="chat-sidebar">
      <el-button type="primary" @click="handleNewSession" :loading="creating" class="new-chat-btn">
        ＋ 新对话
      </el-button>

      <div class="session-list">
        <div
          v-for="s in chatStore.sessions"
          :key="s.sessionId"
          class="session-item"
          :class="{ active: s.sessionId === chatStore.currentSessionId }"
          @click="handleSelectSession(s)"
        >
          <div class="session-info">
            <span class="session-title">{{ s.title || '新对话' }}</span>
            <span class="session-meta">{{ s.messageCount || 0 }} 条消息</span>
          </div>
          <el-popconfirm
            title="删除此对话？"
            confirm-button-text="删除"
            cancel-button-text="取消"
            @confirm="handleDeleteSession(s.sessionId)"
          >
            <template #reference>
              <span class="delete-btn" @click.stop>✕</span>
            </template>
          </el-popconfirm>
        </div>
        <el-empty v-if="chatStore.sessions.length === 0" description="暂无对话" :image-size="40" />
      </div>
    </div>

    <!-- Main chat area -->
    <div class="chat-main">
      <!-- Empty state -->
      <div v-if="!chatStore.currentSessionId" class="empty-state">
        <div class="empty-icon">🤖</div>
        <h2>AI 职业规划助手</h2>
        <p>我是你的专属职业规划顾问"职小途"，可以帮你：</p>
        <div class="feature-hints">
          <div class="hint" @click="quickAsk('根据我的专业推荐适合的岗位')">💼 岗位推荐</div>
          <div class="hint" @click="quickAsk('计算机专业有哪些发展方向')">📈 行业分析</div>
          <div class="hint" @click="quickAsk('如何规划大学四年的学习路径')">📝 学习规划</div>
          <div class="hint" @click="quickAsk('帮我分析一下我的MBTI结果适合什么职业')">🧠 测评解读</div>
        </div>
      </div>

      <!-- Messages -->
      <div v-else class="messages" ref="msgContainer">
        <div v-for="(m, i) in chatStore.messages" :key="i" :class="['message', m.role === 'user' ? 'user-msg' : 'ai-msg']">
          <div class="msg-avatar">{{ m.role === 'user' ? '👤' : '🤖' }}</div>
          <div class="msg-bubble">
            <span v-if="m.content" v-html="renderMsg(m.content)"></span>
            <span v-else class="typing-dots">●●●</span>
          </div>
        </div>
      </div>

      <!-- Input area -->
      <div v-if="chatStore.currentSessionId" class="input-area">
        <el-input
          v-model="input"
          @keyup.enter="handleSend"
          placeholder="输入你的职业规划问题..."
          size="large"
          clearable
        />
        <el-button type="primary" size="large" :disabled="!input.trim()" @click="handleSend">
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useChatStore } from '../../stores/chat'
import { createSSEConnection } from '../../utils/sse'
import { marked } from 'marked'
import { ElMessage } from 'element-plus'

const chatStore = useChatStore()
const input = ref('')
const msgContainer = ref(null)
const creating = ref(false)

const renderMsg = (text) => marked(text || '')

const scrollToBottom = () => {
  nextTick(() => {
    const el = msgContainer.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

const handleNewSession = async () => {
  creating.value = true
  try {
    await chatStore.createSession()
    await chatStore.loadSessions()
  } finally {
    creating.value = false
  }
}

const handleSelectSession = async (session) => {
  await chatStore.loadHistory(session.sessionId)
  scrollToBottom()
}

const handleDeleteSession = async (sessionId) => {
  await chatStore.deleteSession(sessionId)
  ElMessage.success('已删除')
}

const quickAsk = async (question) => {
  await handleNewSession()
  input.value = question
  handleSend()
}

const handleSend = async () => {
  const msg = input.value.trim()
  if (!msg) return

  chatStore.addMessage('user', msg)
  chatStore.updateSessionTitle(chatStore.currentSessionId, msg)
  input.value = ''
  chatStore.isStreaming = true
  scrollToBottom()

  // ★ 关键修复：先创建 AI 占位消息，后续 chunk 直接更新它
  chatStore.addMessage('assistant', '')
  const aiMsgIndex = chatStore.messages.length - 1
  let aiResponse = ''
  let resolved = false

  // 安全兜底：60 秒超时强制结束，避免永远卡在加载状态
  const watchdog = setTimeout(() => {
    if (!resolved) {
      resolved = true
      chatStore.isStreaming = false
      if (!aiResponse) {
        chatStore.messages[aiMsgIndex] = {
          role: 'assistant',
          content: '[回复超时，请重试]',
          createTime: chatStore.messages[aiMsgIndex].createTime
        }
      }
    }
  }, 60000)

  const finish = () => {
    if (resolved) return
    resolved = true
    clearTimeout(watchdog)
    chatStore.isStreaming = false
    chatStore.messages[aiMsgIndex] = {
      role: 'assistant',
      content: aiResponse || '[未收到回复]',
      createTime: chatStore.messages[aiMsgIndex].createTime
    }
    chatStore.loadSessions()
    scrollToBottom()
  }

  createSSEConnection(
    `sessionId=${chatStore.currentSessionId}&message=${encodeURIComponent(msg)}`,
    (chunk) => {
      aiResponse += chunk
      // 直接替换整个对象触发 Vue 响应式
      chatStore.messages[aiMsgIndex] = {
        role: 'assistant',
        content: aiResponse,
        createTime: chatStore.messages[aiMsgIndex].createTime
      }
      scrollToBottom()
    },
    (err) => {
      if (!aiResponse) {
        chatStore.messages[aiMsgIndex] = {
          role: 'assistant',
          content: '[网络错误，请重试]',
          createTime: chatStore.messages[aiMsgIndex].createTime
        }
      }
      console.error('Chat error:', err)
      finish()
    },
    finish
  )
}

onMounted(() => {
  chatStore.loadSessions()
})
</script>

<style scoped>
.chat-container {
  display: flex;
  height: calc(100vh - 140px);
  gap: 0;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

/* ===== Sidebar ===== */
.chat-sidebar {
  width: 260px;
  background: #fafbfc;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.new-chat-btn {
  margin: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 40px;
  font-size: 14px;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px 16px;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 2px;
  transition: background 0.15s;
}

.session-item:hover { background: #ecf5ff; }
.session-item.active { background: #d9ecff; }

.session-info {
  flex: 1;
  overflow: hidden;
}

.session-title {
  display: block;
  font-size: 13px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-meta {
  font-size: 11px;
  color: #909399;
}

.delete-btn {
  opacity: 0;
  transition: opacity 0.15s;
  font-size: 14px;
  color: #F56C6C;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 4px;
}
.delete-btn:hover { background: #fef0f0; }
.session-item:hover .delete-btn { opacity: 1; }

/* ===== Main Area ===== */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state h2 {
  font-size: 22px;
  color: #303133;
  margin-bottom: 8px;
}

.empty-state p {
  color: #909399;
  margin-bottom: 24px;
}

.feature-hints {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.hint {
  padding: 10px 18px;
  background: #f0f5ff;
  border: 1px solid #d9ecff;
  border-radius: 20px;
  cursor: pointer;
  font-size: 13px;
  color: #409EFF;
  transition: all 0.2s;
}

.hint:hover {
  background: #409EFF;
  color: #fff;
  border-color: #409EFF;
}

/* ===== Messages ===== */
.messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  background: #f7f8fa;
}

.message {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
  background: #fff;
}

.user-msg { flex-direction: row-reverse; }

.msg-bubble {
  max-width: 72%;
  padding: 12px 16px;
  border-radius: 16px;
  line-height: 1.7;
  font-size: 14px;
}

.user-msg .msg-bubble {
  background: #409EFF;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.ai-msg .msg-bubble {
  background: #fff;
  color: #303133;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}

.streaming .typing-dots {
  letter-spacing: 2px;
  animation: blink 1.4s infinite;
}

@keyframes blink {
  0%, 60%, 100% { opacity: 0.3; }
  30% { opacity: 1; }
}

.ai-msg .msg-bubble :deep(p) { margin: 0; }
.ai-msg .msg-bubble :deep(ul),
.ai-msg .msg-bubble :deep(ol) { padding-left: 20px; }
.ai-msg .msg-bubble :deep(strong) { color: #409EFF; }

/* ===== Input ===== */
.input-area {
  display: flex;
  padding: 16px 24px;
  border-top: 1px solid #ebeef5;
  gap: 10px;
  background: #fff;
}

.input-area .el-input {
  flex: 1;
}
</style>
