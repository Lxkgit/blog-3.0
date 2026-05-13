<template>
  <div style="display: flex;">
    <div style="flex: 1; width: 40%;">
      <CpuChart />
    </div>
    <div style="flex: 1; width: 40%;">
      <MemoryChart />
    </div>
  </div>
</template>

<script setup lang="ts">
import CpuChart from '@/components/admin/device/CpuChart.vue'
import MemoryChart from '@/components/admin/device/MemoryChart.vue'
</script>

<style scoped lang="css"></style>
