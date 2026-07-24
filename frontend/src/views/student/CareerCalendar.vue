<template>
  <div class="cal-page">
    <div class="page-header">
      <h2>📅 求职日历</h2>
      <p class="subtitle">校园招聘关键节点 + 你的待办清单</p>
    </div>

    <!-- Countdown Banner -->
    <div class="countdown-banner">
      <div class="cd-item" v-for="cd in countdowns" :key="cd.label">
        <div class="cd-num">{{ cd.days }}</div>
        <div class="cd-label">{{ cd.label }}</div>
        <div class="cd-date">{{ cd.date }}</div>
      </div>
    </div>

    <div class="cal-layout">
      <!-- Timeline -->
      <div class="timeline-section">
        <h3>📆 校招关键时间线</h3>
        <div class="timeline">
          <div v-for="(event, i) in timeline" :key="i" class="tl-item" :class="{ past: event.isPast, current: event.isCurrent }">
            <div class="tl-marker" :class="{ active: event.isCurrent }"></div>
            <div class="tl-period">{{ event.period }}</div>
            <div class="tl-card">
              <h4>{{ event.icon }} {{ event.title }}</h4>
              <p>{{ event.desc }}</p>
              <div class="tl-actions">
                <el-tag v-for="a in event.actions" :key="a" size="small" effect="plain">{{ a }}</el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Sidebar: Checklist + Tips -->
      <div class="cal-sidebar">
        <!-- Weekly Checklist -->
        <div class="checklist-card">
          <h3>✅ 本周待办</h3>
          <div v-for="(item, i) in weeklyChecklist" :key="i" class="cl-item">
            <el-checkbox v-model="item.done" :label="item.text" size="large" />
            <span class="cl-tag">{{ item.tag }}</span>
          </div>
          <div class="cl-progress">
            <el-progress :percentage="weeklyProgress" :color="'#67C23A'" :stroke-width="8" />
            <span>{{ doneCount }}/{{ weeklyChecklist.length }}</span>
          </div>
        </div>

        <!-- Quick Tips -->
        <div class="tips-card">
          <h3>💡 求职小贴士</h3>
          <div class="tip-item" v-for="t in tips" :key="t.title">
            <span class="tip-icon">{{ t.icon }}</span>
            <div><strong>{{ t.title }}</strong><p>{{ t.desc }}</p></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const countdowns = computed(() => {
  const now = new Date()
  const events = [
    { label: '春招补录截止', date: '2026-06-30', color: '#E6A23C' },
    { label: '秋招提前批', date: '2026-07-15', color: '#409EFF' },
    { label: '秋招正式批', date: '2026-09-01', color: '#67C23A' },
  ]
  return events.map(e => {
    const target = new Date(e.date)
    const days = Math.max(0, Math.ceil((target - now) / (1000*60*60*24)))
    return { ...e, days: days + '天', date: e.date }
  })
})

const timeline = [
  { period: '3-5月', title: '🌱 春招补录 + 暑期实习', desc: '春招尾声，同时暑期实习招聘启动。抓住末班车机会。', actions: ['投递实习', '准备面试', '刷算法题'], isPast: true },
  { period: '6月', title: '📝 简历打磨 + 暑期实训', desc: '利用实训平台练习，优化简历，为秋招做最后的准备。', actions: ['AI简历优化', '模拟面试', '实训任务'], isPast: false, isCurrent: true },
  { period: '7-8月', title: '🚀 秋招提前批', desc: '大厂提前批开始！面向技术岗，免笔试直通面试。这是进入大厂的最佳窗口。', actions: ['投递提前批', '七天内场面试', '跟进内推'] },
  { period: '9-10月', title: '🔥 秋招正式批(金九银十)', desc: '全年最大规模招聘，几乎所有企业开放校招。密集投递+面试。', actions: ['海投50+企业', '每天3场面试', '及时复盘'] },
  { period: '11-12月', title: '📋 秋招补录 + Offer选择', desc: '未招满的岗位进行补录。对比Offer，做出选择。', actions: ['补录捡漏', 'Offer谈判', '签订三方'] },
  { period: '次年2-4月', title: '🌸 春招(第二波机会)', desc: '考研失利同学加入竞争，岗位数量少于秋招但仍有好机会。', actions: ['春招投递', '考研后衔接', '灵活调整目标'] },
  { period: '次年5-6月', title: '🎓 毕业入职准备', desc: '完成毕业论文，办理入职手续，准备从校园人到职场人的转变。', actions: ['毕设答辩', '租房准备', '入职材料'] },
]

const weeklyChecklist = ref([
  { text: '完成1道实训任务', done: false, tag: '技能' },
  { text: '投递3份简历', done: false, tag: '求职' },
  { text: '浏览AI推荐的岗位', done: false, tag: '探索' },
  { text: '使用模拟面试练习1次', done: false, tag: '面试' },
  { text: '优化简历中1个项目描述', done: false, tag: '简历' },
  { text: '复习1个高频面试知识点', done: false, tag: '学习' },
])

const doneCount = computed(() => weeklyChecklist.value.filter(c => c.done).length)
const weeklyProgress = computed(() => Math.round(doneCount.value / weeklyChecklist.value.length * 100))

const tips = [
  { icon: '📊', title: '海投策略', desc: '秋招建议投递50-100家企业，不要只盯着头部大厂，二三线也有好机会。' },
  { icon: '⏰', title: '黄金时段', desc: '周二到周四上午9-11点是HR查看简历的高峰期，此时投递曝光率最高。' },
  { icon: '🔗', title: '内推优先', desc: '能找到内推一定要走内推，简历直达用人部门，比海投效率高3倍。' },
  { icon: '📝', title: '面后复盘', desc: '每次面试后花10分钟记录被问到的问题和自己的回答，这是最快的成长方式。' },
]
</script>

<style scoped>
.cal-page { padding: 4px 0; }
.page-header { margin-bottom: 20px; }
.page-header h2 { font-size: 22px; color: #303133; margin-bottom: 4px; }
.subtitle { color: #909399; font-size: 13px; }

.countdown-banner { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; margin-bottom: 24px; }
.cd-item { background: #fff; border-radius: 12px; padding: 20px; text-align: center; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.cd-num { font-size: 40px; font-weight: 800; color: #409EFF; line-height: 1.2; }
.cd-label { font-size: 14px; font-weight: 600; color: #303133; margin: 4px 0; }
.cd-date { font-size: 12px; color: #909399; }

.cal-layout { display: grid; grid-template-columns: 1.5fr 1fr; gap: 20px; align-items: start; }
@media (max-width: 900px) { .cal-layout { grid-template-columns: 1fr; } }

/* Timeline */
.timeline-section h3 { font-size: 16px; margin-bottom: 16px; }
.timeline { position: relative; padding-left: 24px; }
.timeline::before { content: ''; position: absolute; left: 8px; top: 0; bottom: 0; width: 2px; background: #e4e7ed; }
.tl-item { position: relative; padding-bottom: 20px; padding-left: 20px; }
.tl-item.past { opacity: 0.5; }
.tl-marker { position: absolute; left: -20px; top: 6px; width: 14px; height: 14px; border-radius: 50%; background: #dcdfe6; border: 3px solid #fff; z-index: 1; }
.tl-marker.active { background: #409EFF; box-shadow: 0 0 0 3px rgba(64,158,255,0.2); }
.tl-period { font-size: 12px; color: #409EFF; font-weight: 700; margin-bottom: 6px; }
.tl-card { background: #fff; border-radius: 10px; padding: 14px 16px; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }
.tl-card h4 { margin: 0 0 6px; font-size: 14px; }
.tl-card p { margin: 0 0 8px; font-size: 12px; color: #909399; line-height: 1.5; }
.tl-actions { display: flex; gap: 4px; flex-wrap: wrap; }

/* Sidebar */
.checklist-card, .tips-card { background: #fff; border-radius: 12px; padding: 18px 20px; margin-bottom: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.checklist-card h3, .tips-card h3 { margin: 0 0 12px; font-size: 15px; }
.cl-item { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.cl-tag { font-size: 10px; background: #ecf5ff; color: #409EFF; padding: 1px 6px; border-radius: 3px; }
.cl-progress { display: flex; align-items: center; gap: 10px; margin-top: 16px; padding-top: 12px; border-top: 1px solid #ebeef5; font-size: 13px; color: #909399; }

.tip-item { display: flex; gap: 10px; padding: 10px 0; border-bottom: 1px solid #f5f5f5; }
.tip-item:last-child { border-bottom: none; }
.tip-icon { font-size: 20px; flex-shrink: 0; }
.tip-item strong { display: block; font-size: 13px; margin-bottom: 2px; }
.tip-item p { margin: 0; font-size: 12px; color: #909399; line-height: 1.5; }
</style>
