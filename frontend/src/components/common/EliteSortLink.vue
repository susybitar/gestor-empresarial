<template>
  <div 
    class="elite-sort-link" 
    :class="{ 'is-active': active }"
    @click="$emit('toggle')"
    :style="{ justifyContent: align === 'right' ? 'flex-end' : align === 'center' ? 'center' : 'flex-start' }"
  >
    <span class="sort-label-text"><slot>{{ label }}</slot></span>
    
    <div class="sort-icon-wrapper">
      <ChevronUp v-if="active && !sortDesc" class="icon-sort-needle" />
      <ChevronDown v-else-if="active && sortDesc" class="icon-sort-needle" />
      <ChevronDown v-else class="icon-sort-needle-ghost" />
    </div>
  </div>
</template>

<script setup>
import { ChevronUp, ChevronDown } from 'lucide-vue-next';

defineProps({
  label: String,
  active: Boolean,
  sortDesc: Boolean,
  align: {
    type: String,
    default: 'left'
  }
});

defineEmits(['toggle']);
</script>

<style scoped>
.elite-sort-link {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.sort-label-text {
  font-size: 10px;
  font-weight: 800;
  color: #64748B;
  letter-spacing: 0.05em;
  transition: color 0.2s ease;
}

.elite-sort-link:hover .sort-label-text,
.elite-sort-link.is-active .sort-label-text {
  color: #64748B;
}

.sort-icon-wrapper {
  display: flex;
  align-items: center;
}

.icon-sort-needle {
  width: 10px;
  height: 10px;
  stroke-width: 1.5px;
  color: #1E40AF;
}

.icon-sort-needle-ghost {
  width: 10px;
  height: 10px;
  stroke-width: 1.5px;
  color: #94A3B8;
  opacity: 0.3;
}
</style>
