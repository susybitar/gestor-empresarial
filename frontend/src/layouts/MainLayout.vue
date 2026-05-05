<template>
  <v-app class="elite-light-app">
    <div class="layout-container">
      
      <!-- Sidebar Único Inteligente -->
      <aside 
        class="sidebar-wrapper" 
        :class="{ 'is-mobile-open': isMobileOpen }"
        :style="{ width: isCollapsed ? '100px' : '260px' }"
      >
        <Sidebar 
          v-model:collapsed="isCollapsed" 
          @close-mobile="isMobileOpen = false" 
        />
      </aside>

      <!-- Contenido Principal -->
      <div class="main-area">
        <Topbar @toggle-mobile-sidebar="isMobileOpen = !isMobileOpen" />
        
        <main class="f-view-layout">
          <router-view v-slot="{ Component }">
            <Transition name="fade" mode="out-in">
              <component :is="Component" />
            </Transition>
          </router-view>
        </main>
      </div>

      <!-- Overlay para móvil -->
      <div v-if="isMobileOpen" class="mobile-overlay" @click="isMobileOpen = false"></div>

    </div>
  </v-app>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import Sidebar from '@/components/layout/Sidebar.vue'
import Topbar from '@/components/layout/Topbar.vue'

const isCollapsed = ref(false)
const isMobileOpen = ref(false)

const handleResize = () => {
  if (window.innerWidth > 1024) {
    isMobileOpen.value = false
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.elite-light-app {
  background-color: #F4F4F5 !important;
  min-height: 100vh !important;
}

.layout-container {
  display: flex;
  min-height: 100vh;
  width: 100%;
}

.sidebar-wrapper {
  position: sticky;
  top: 16px;
  height: calc(100vh - 32px);
  padding: 16px;
  z-index: 100;
  width: 260px;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  background: transparent !important;
}

.main-area {
  flex: 1;
  min-width: 0;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

@media (max-width: 1024px) {
  .sidebar-wrapper {
    position: fixed;
    left: -320px;
    top: 0;
    height: 100vh;
    width: 280px !important;
    padding: 16px;
    z-index: 1000 !important;
    transition: left 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    background: transparent !important;
  }
  .sidebar-wrapper.is-mobile-open {
    left: 0;
  }
}

.mobile-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(4px);
  z-index: 999 !important;
}

/* Transición suave entre páginas */
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
