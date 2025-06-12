<template>
  <router-view />
</template>

<script setup>
</script>

<style>

#nprogress .bar {
  position: fixed !important; 
  top: 0 !important;          
  left: 0 !important;
  right: 0 !important;
  height: 3px !important;
  z-index: 99999 !important;  
  background-color: #409EFF !important;
}

html,
body,
#app { 
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
}
</style>