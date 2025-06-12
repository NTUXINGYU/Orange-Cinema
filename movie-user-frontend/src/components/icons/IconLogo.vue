<template>
    <router-link :to="{ name: 'About' }">
      <div class="flex items-center">
        <img src="@/assets/orange.png" alt="Logo" class="w-10 h-10 mr-3" />
        <h1 class="text-2xl font-bold">OrangeCinema</h1>
      </div>
    </router-link>
  </template>

<script setup>
  import { RouterLink } from 'vue-router';
</script>
