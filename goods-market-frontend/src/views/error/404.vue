<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面不存在</p>
    <router-link to="/">返回首页</router-link>
  </div>
</template>

<script setup>
// 404 页面
</script>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  text-align: center;
}
.not-found h1 {
  font-size: 96px;
  color: #409eff;
  margin-bottom: 16px;
}
.not-found p {
  font-size: 18px;
  color: #909399;
  margin-bottom: 24px;
}
</style>
