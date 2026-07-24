import { defineStore } from 'pinia'
import { createSession as createSessionApi, getSessions as getSessionsApi, getHistory as getHistoryApi, deleteSession as deleteSessionApi } from '../api/chat'

export const useChatStore = defineStore('chat', {
  state: () => ({
    sessions: [],
    currentSessionId: null,
    messages: [],
    isStreaming: false
  }),
  actions: {
    async createSession() {
      const res = await createSessionApi()
      this.currentSessionId = res.data.sessionId
      this.messages = []
      return res.data.sessionId
    },
    async loadSessions() {
      const res = await getSessionsApi()
      this.sessions = res.data || []
    },
    async loadHistory(sessionId) {
      const res = await getHistoryApi(sessionId)
      this.messages = (res.data || []).map(m => ({
        role: m.role,
        content: m.content,
        createTime: m.createTime
      }))
      this.currentSessionId = sessionId
    },
    async deleteSession(sessionId) {
      await deleteSessionApi(sessionId)
      this.sessions = this.sessions.filter(s => s.sessionId !== sessionId)
      if (this.currentSessionId === sessionId) {
        this.currentSessionId = null
        this.messages = []
      }
    },
    addMessage(role, content) {
      this.messages.push({ role, content, createTime: new Date().toISOString() })
    },
    // Update session title in list after first message
    updateSessionTitle(sessionId, title) {
      const s = this.sessions.find(s => s.sessionId === sessionId)
      if (s && s.title === '新对话') {
        s.title = title.length > 20 ? title.substring(0, 20) + '...' : title
      }
    }
  }
})
