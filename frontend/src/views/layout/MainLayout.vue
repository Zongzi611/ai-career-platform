<template>
  <el-container class="main-container">
    <el-aside :width="isCollapse ? '64px' : '200px'">
      <Sidebar :isCollapse="isCollapse" @toggle="isCollapse = !isCollapse" />
    </el-aside>
    <el-container class="main-content-area">
      <el-header><Navbar /></el-header>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import Sidebar from './Sidebar.vue'
import Navbar from './Navbar.vue'
const isCollapse = ref(false)
</script>

<style scoped>
.main-container { height: 100vh; }
.el-aside { background: #304156; transition: width 0.3s; overflow: hidden; }
.el-header { background: #fff; border-bottom: 1px solid #e6e6e6; display: flex; align-items: center; justify-content: flex-end; padding: 0 20px; }
.el-main { background: #f0f2f5; padding: 20px; }
</style>
