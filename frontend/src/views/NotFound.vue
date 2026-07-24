<template>
  <div class="nf-page">
    <div class="nf-code">404</div>
    <h2>页面不存在</h2>
    <p>你访问的页面可能已被移除或地址输入有误</p>
    <el-button type="primary" size="large" @click="$router.push('/')">返回首页</el-button>
  </div>
</template>

<style scoped>
.nf-page { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 60vh; text-align: center; }
.nf-code { font-size: 100px; font-weight: 800; background: linear-gradient(135deg, #667eea, #764ba2); -webkit-background-clip: text; -webkit-text-fill-color: transparent; line-height: 1.2; }
.nf-page h2 { font-size: 24px; color: #303133; margin: 16px 0 8px; }
.nf-page p { color: #909399; margin-bottom: 24px; }
</style>
