<template>
  <header class="topbar">
    <div class="topbar-left">
      <button class="mobile-toggle" @click="$emit('toggle-mobile-sidebar')">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M4 8H20M4 16H14" stroke="#1E40AF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
    </div>
    <div class="topbar-right">
    </div>
  </header>
</template>

<script setup>
defineEmits(['toggle-mobile-sidebar'])
</script>

<style scoped>
.topbar {
  position: sticky;
  top: 0;
  left: 0;
  width: 100%;
  height: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 64px;
  z-index: 100;
  background: transparent;
}

.mobile-toggle {
  display: none;
  background: transparent;
  border: none;
  padding: 8px;
  color: #1E40AF;
  cursor: pointer;
}

@media (max-width: 1024px) {
  .topbar {
    padding: 16px 24px 0; /* Añadimos 16px arriba para que no esté pegado */
    height: 80px !important; 
    background: rgba(244, 244, 245, 0.9) !important;
    backdrop-filter: blur(12px) !important;
    border-bottom: none !important;
  }
  .mobile-toggle {
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>
