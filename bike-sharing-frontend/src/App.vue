<template>
  <router-view />
</template>

<script setup>
// 共享单车管理系统根组件
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: var(--text-color);
}

/* 全局修复图片预览层级问题，确保置顶并覆盖背景 */
.el-image-viewer__wrapper {
  z-index: 99999 !important;
}

.el-image-viewer__mask {
  background-color: rgba(0, 0, 0, 0.8) !important;
  opacity: 1 !important;
}
</style>
