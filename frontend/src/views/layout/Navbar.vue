<template>
  <div class="navbar">
    <div class="nav-left">
      <!-- Dark mode toggle -->
      <span class="dm-toggle" @click="toggleDark" :title="isDark?'切换亮色':'切换暗色'">{{ isDark ? '☀️' : '🌙' }}</span>
    </div>
    <div class="nav-right">
      <!-- Notification Bell -->
      <el-popover placement="bottom" :width="340" trigger="click">
        <template #reference>
          <el-badge :value="unreadCount" :hidden="unreadCount===0" :max="99" class="bell-badge">
            <span class="bell-icon">🔔</span>
          </el-badge>
        </template>
        <div class="notif-list">
          <div class="notif-header">
            <span>通知</span>
            <el-button v-if="unreadCount>0" size="small" text type="primary" @click="readAll">全部已读</el-button>
          </div>
          <div v-if="notifications.length===0" style="text-align:center;padding:20px;color:#909399">暂无通知</div>
          <div v-for="n in notifications" :key="n.id" class="notif-item" :class="{unread: !n.isRead}" @click="readOne(n)">
            <div class="ni-title">{{ n.title }}</div>
            <div class="ni-msg" v-if="n.message">{{ n.message }}</div>
            <div class="ni-time">{{ n.createTime?.substring(0,16) }}</div>
          </div>
        </div>
      </el-popover>

      <!-- User dropdown -->
      <el-dropdown @command="handleCommand">
        <span class="user-info">
          <span class="nav-avatar">{{ (userStore.userInfo?.realName || userStore.userInfo?.username || '?').charAt(0) }}</span>
          <span class="nav-name">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人信息</el-dropdown-item>
            <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import request from '../../api/request'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const notifications = ref([])
const unreadCount = ref(0)
const isDark = ref(localStorage.getItem('careersail-dark') === 'true')

const loadNotifs = async () => {
  try {
    const res = await request.get('/notifications')
    notifications.value = res.data?.list || []
    unreadCount.value = res.data?.unread || 0
  } catch {}
}

const readAll = async () => {
  await request.put('/notifications/read-all')
  notifications.value.forEach(n => n.isRead = 1)
  unreadCount.value = 0
}

const readOne = async (n) => {
  if (!n.isRead) { await request.put(`/notifications/${n.id}/read`); n.isRead = 1; unreadCount.value-- }
}

const toggleDark = () => {
  isDark.value = !isDark.value
  localStorage.setItem('careersail-dark', isDark.value)
  document.documentElement.classList.toggle('dark', isDark.value)
}

const handleCommand = (cmd) => {
  if (cmd === 'profile') router.push('/profile')
  else if (cmd === 'logout') { userStore.logout(); ElMessage.success('已退出'); router.push('/login') }
}

onMounted(() => {
  if (isDark.value) document.documentElement.classList.add('dark')
  loadNotifs()
  setInterval(loadNotifs, 30000)
})
</script>

<style scoped>
.navbar { display: flex; justify-content: space-between; align-items: center; padding: 0 20px; height: 100%; }
.nav-left, .nav-right { display: flex; align-items: center; gap: 16px; }
.dm-toggle { cursor: pointer; font-size: 18px; padding: 4px 8px; border-radius: 6px; transition: background 0.2s; }
.dm-toggle:hover { background: rgba(255,255,255,0.1); }

.user-info { cursor: pointer; display: flex; align-items: center; gap: 8px; color: #fff; }
.nav-avatar { width: 30px; height: 30px; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg,#667eea,#764ba2); color:#fff; font-weight:700; font-size:13px; flex-shrink:0; }
.nav-name { font-size: 14px; }

.bell-icon { font-size: 18px; cursor: pointer; }
.bell-badge :deep(.el-badge__content) { font-size: 10px; height: 16px; line-height: 16px; padding: 0 4px; }

.notif-list { max-height: 400px; overflow-y: auto; }
.notif-header { display: flex; justify-content: space-between; align-items: center; padding-bottom: 8px; border-bottom: 1px solid #ebeef5; margin-bottom: 4px; font-weight: 600; }
.notif-item { padding: 10px 8px; border-radius: 6px; cursor: pointer; border-bottom: 1px solid #f5f5f5; }
.notif-item:hover { background: #f5f7fa; }
.notif-item.unread { background: #ecf5ff; }
.ni-title { font-size: 13px; font-weight: 500; color: #303133; }
.ni-msg { font-size: 12px; color: #909399; margin-top: 2px; }
.ni-time { font-size: 11px; color: #c0c4cc; margin-top: 4px; }
</style>
