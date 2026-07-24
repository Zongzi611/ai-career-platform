<template>
  <div class="sidebar">
    <div class="logo" @click="$emit('toggle')">
      <span v-if="!isCollapse">🎓 CareerSail 职业规划</span>
      <span v-else>🎓</span>
    </div>
    <el-menu :default-active="activeMenu" router :collapse="isCollapse" background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF">
      <template v-if="isStudent">
        <el-menu-item index="/student/home"><el-icon><HomeFilled /></el-icon><span>首页</span></el-menu-item>
        <el-sub-menu index="sub-career">
          <template #title><el-icon><Search /></el-icon><span>职业探索</span></template>
          <el-menu-item index="/student/career">岗位搜索</el-menu-item>
          <el-menu-item index="/student/salary">薪资洞察</el-menu-item>
          <el-menu-item index="/student/calendar">求职日历</el-menu-item>
          <el-menu-item index="/student/career-tools">收藏对比</el-menu-item>
          <el-menu-item index="/student/learning-path">学习路径</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="sub-grow">
          <template #title><el-icon><Edit /></el-icon><span>能力成长</span></template>
          <el-menu-item index="/student/assessment">职业测评</el-menu-item>
          <el-menu-item index="/student/training">实训任务</el-menu-item>
          <el-menu-item index="/student/interview">AI 模拟面试</el-menu-item>
          <el-menu-item index="/student/exam">模拟笔试</el-menu-item>
          <el-menu-item index="/student/resume">简历优化</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/student/chat"><el-icon><ChatDotRound /></el-icon><span>AI 咨询</span></el-menu-item>
      </template>
      <template v-if="isTeacher">
        <el-menu-item index="/teacher/dashboard"><el-icon><DataAnalysis /></el-icon><span>学情仪表板</span></el-menu-item>
        <el-menu-item index="/teacher/students"><el-icon><User /></el-icon><span>学生管理</span></el-menu-item>
        <el-menu-item index="/teacher/tasks"><el-icon><List /></el-icon><span>任务管理</span></el-menu-item>
        <el-menu-item index="/teacher/reports"><el-icon><Document /></el-icon><span>报告管理</span></el-menu-item>
        <el-menu-item index="/teacher/grades"><el-icon><DataAnalysis /></el-icon><span>成绩总览</span></el-menu-item>
        <el-menu-item index="/teacher/stats"><el-icon><PieChart /></el-icon><span>任务统计</span></el-menu-item>
      </template>
      <template v-if="isAdmin">
        <el-menu-item index="/admin/dashboard"><el-icon><Odometer /></el-icon><span>管理中心</span></el-menu-item>
        <el-menu-item index="/admin/users"><el-icon><UserFilled /></el-icon><span>用户管理</span></el-menu-item>
        <el-menu-item index="/admin/careers"><el-icon><Briefcase /></el-icon><span>职业知识库</span></el-menu-item>
        <el-menu-item index="/admin/training"><el-icon><Trophy /></el-icon><span>实训管理</span></el-menu-item>
        <el-menu-item index="/admin/config"><el-icon><Setting /></el-icon><span>系统配置</span></el-menu-item>
      </template>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user'

defineProps({ isCollapse: Boolean })
defineEmits(['toggle'])

const route = useRoute()
const userStore = useUserStore()
const activeMenu = computed(() => route.path)
const isStudent = computed(() => userStore.isStudent)
const isTeacher = computed(() => userStore.isTeacher)
const isAdmin = computed(() => userStore.isAdmin)
</script>

<style scoped>
.logo { height: 60px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 18px; cursor: pointer; border-bottom: 1px solid rgba(255,255,255,0.1); }
.logo span { white-space: nowrap; }
</style>
