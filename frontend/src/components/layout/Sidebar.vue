<template>
  <CrystalCard tag="aside" :customClass="'sidebar ' + (collapsed ? 'is-collapsed' : '')" :hoverable="false">
    <!-- Botón de plegado circular Crystal (Escritorio) -->
    <button class="collapse-toggle" @click="toggleCollapse" aria-label="Alternar menú">
      <ChevronLeft v-if="!collapsed" class="toggle-icon" />
      <ChevronRight v-else class="toggle-icon" />
    </button>

    <!-- Botón de cierre X (Móvil) -->
    <button class="mobile-close" @click="$emit('close-mobile')" aria-label="Cerrar menú">
      <X class="icon-md" />
    </button>

    <!-- Brand -->
    <div class="brand">
      <div class="brand-main">
        <img src="@/assets/img/logos/svg/26_Símbolo.svg" alt="Future Space" class="brand-symbol" />
      </div>
    </div>

    <!-- Navegación -->
    <nav class="nav">
      <template v-for="group in navGroups" :key="group.title">
        <div v-if="!collapsed" class="nav-section-label f-label">{{ group.title }}</div>
        <div class="nav-group">
          <router-link 
            v-for="item in group.items" 
            :key="item.to"
            :to="item.to" 
            class="nav-item" 
            :class="{ 'is-active': isActive(item.to) }" 
            :data-tooltip="collapsed ? item.name : null"
          >
            <component :is="item.icon" class="nav-icon" />
            <span v-if="!collapsed" class="nav-label">{{ item.name }}</span>
          </router-link>
        </div>
      </template>
    </nav>

    <!-- Perfil de Usuario -->
    <div class="user-profile-wrapper">
      <button 
        class="user-profile" 
        @click.stop="isUserMenuOpen = !isUserMenuOpen"
      >
        <div class="avatar-circle">
          <img v-if="userAvatar" :src="userAvatar" alt="Avatar" class="avatar-img" />
          <span v-else>{{ userInitials }}</span>
        </div>
        <div v-if="!collapsed" class="user-info">
          <h2 class="user-name f-display-name">{{ userName }}</h2>
          <span class="user-role role-text">Administrador</span>
        </div>
      </button>
      
      <Transition name="fade-right">
        <CrystalCard v-if="isUserMenuOpen" customClass="user-dropdown-crystal">
          <button class="dropdown-item" @click="goToProfile">
            <User class="dropdown-icon" />
            <span>Perfil</span>
          </button>
          <div class="dropdown-divider"></div>
          <button class="dropdown-item" @click="handleLogout">
            <LogOut class="dropdown-icon" />
            <span>Salir</span>
          </button>
        </CrystalCard>
      </Transition>
    </div>
  </CrystalCard>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { employeeService } from '@/services/employeeService'
import CrystalCard from '@/components/common/CrystalCard.vue'
import { 
  LayoutDashboard, Users, Briefcase, FileText, User, LogOut,
  ChevronLeft, ChevronRight, X
} from 'lucide-vue-next'

const props = defineProps({ collapsed: Boolean })
const emit = defineEmits(['update:collapsed', 'close-mobile'])
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isUserMenuOpen = ref(false)
const storedName = localStorage.getItem('fsm_user_name')
const userName = ref(storedName || authStore.user?.email?.split('@')[0] || 'Usuario')
const userAvatar = ref(localStorage.getItem('fsm_user_avatar') || '')

const navGroups = [
  {
    title: 'GENERAL',
    items: [{ name: 'Panel', to: '/dashboard', icon: LayoutDashboard }]
  },
  {
    title: 'GESTIÓN',
    items: [
      { name: 'Empleados', to: '/employees', icon: Users },
      { name: 'Proyectos', to: '/projects', icon: Briefcase },
      { name: 'Asignaciones', to: '/assignments', icon: FileText }
    ]
  }
]

const toggleCollapse = () => emit('update:collapsed', !props.collapsed)
const isActive = (path) => route.path.startsWith(path)

const userInitials = computed(() => {
  const parts = userName.value.split(' ')
  return parts.length > 1 ? (parts[0][0] + parts[1][0]).toUpperCase() : userName.value.slice(0, 2).toUpperCase()
})

const loadProfile = async () => {
  const stored = localStorage.getItem('fsm_user_name')
  if (stored) userName.value = stored
  userAvatar.value = localStorage.getItem('fsm_user_avatar') || ''

  try {
    const employees = await employeeService.getAll(false)
    const me = employees.find(e => e.email === authStore.user?.email)
    if (me) {
      userName.value = `${me.firstName} ${me.lastName}`
      localStorage.setItem('fsm_user_name', userName.value)
    }
  } catch (e) {
    console.warn('Sidebar: Usando datos locales')
  }
}

const handleClickOutside = (e) => {
  if (isUserMenuOpen.value && !e.target.closest('.user-profile-wrapper')) {
    isUserMenuOpen.value = false
  }
}

const goToProfile = () => { isUserMenuOpen.value = false; router.push('/profile') }
const handleLogout = () => { isUserMenuOpen.value = false; authStore.logout(); router.push('/login') }

onMounted(() => {
  loadProfile()
  window.addEventListener('profile-updated', loadProfile)
  window.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  window.removeEventListener('profile-updated', loadProfile)
  window.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.sidebar {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  z-index: 100;
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.sidebar:hover {
  background: rgba(255, 255, 255, 0.35) !important;
  border-color: rgba(255, 255, 255, 0.4) !important;
}

.sidebar.is-collapsed { width: 85px; }

.brand { padding: 32px 24px; display: flex; justify-content: center; }
.brand-symbol { height: 36px; filter: drop-shadow(0 4px 10px rgba(30, 64, 175, 0.15)); }

/* BOTÓN COLAPSO CIRCULAR CRYSTAL */
.collapse-toggle {
  position: absolute;
  right: -13px;
  top: 38px;
  width: 26px;
  height: 26px;
  background: rgba(255, 255, 255, 0.6) !important;
  backdrop-filter: blur(8px);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.5);
  color: #1E40AF;
  cursor: pointer;
  transition: all 0.3s;
  z-index: 100;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.collapse-toggle:hover {
  background: #1E40AF !important;
  color: white;
  transform: scale(1.1);
}

@media (max-width: 1024px) {
  .collapse-toggle {
    display: none !important;
  }
}

.toggle-icon { width: 16px; height: 16px; }

.mobile-close {
  display: none;
  position: absolute;
  top: 15px;
  right: 15px;
  width: 40px;
  height: 40px;
  background: transparent;
  border: none;
  display: none; /* Se activa en media query */
  align-items: center;
  justify-content: center;
  color: #1E40AF;
  cursor: pointer;
  z-index: 101;
}

@media (max-width: 1024px) {
  .sidebar {
    width: 280px !important; /* Forzamos ancho en móvil */
  }
  .mobile-close {
    display: flex !important;
  }
}

.nav { padding: 0 16px; flex: 1; overflow-y: auto; scrollbar-width: none; }
.nav::-webkit-scrollbar { display: none; }

.nav-section-label {
  padding: 24px 12px 12px;
}

.nav-item {
  display: flex; align-items: center; gap: 14px; padding: 12px 16px; border-radius: 14px;
  color: #475569; text-decoration: none; font-size: 14px; font-weight: 500; transition: all 0.3s;
  margin-bottom: 4px;
}
.nav-item:hover { background: rgba(30, 64, 175, 0.04); color: #1E40AF; transform: translateX(4px); }
.nav-item.is-active { 
  background: linear-gradient(90deg, rgba(30, 64, 175, 0.08) 0%, rgba(255, 255, 255, 0) 100%);
  color: #1E40AF; font-weight: 700;
  border: none !important;
}
.nav-icon { width: 18px; height: 18px; }

.user-profile-wrapper { margin-top: auto; padding: 24px 16px; position: relative; }
.user-profile {
  width: 100%; display: flex; align-items: center; gap: 10px; background: transparent !important; 
  border: none !important; box-shadow: none !important; padding: 10px; border-radius: 16px; cursor: pointer;
  transition: all 0.3s;
}
.user-profile:hover { background: white; box-shadow: 0 10px 25px rgba(0,0,0,0.05); }

.avatar-circle {
  width: 40px; height: 40px; border-radius: 50%;
  background: #F8FAFC;
  display: flex; align-items: center; justify-content: center; overflow: hidden;
  color: #1E293B; font-weight: 800; font-size: 14px;
  border: 1px solid #E2E8F0;
}
.avatar-img { width: 100%; height: 100%; object-fit: cover; }

.user-info { 
  text-align: center; 
  overflow: hidden; 
  display: flex; 
  flex-direction: column; 
  justify-content: center;
  align-items: center;
}

.user-name, .f-display-name { 
  display: block; 
  font-family: 'Outfit', sans-serif;
  font-size: 18px; 
  font-weight: 700; 
  color: #1E293B; 

  line-height: 2;
  letter-spacing: -0.04em;
}

.user-role, .role-text { 
  display: block;
  font-family: 'Inter', sans-serif;
  font-size: 10px;
  font-weight: 500;
  color: #64748B;
  letter-spacing: -0.03em;
  line-height: 1;
}

/* --- CRYSTAL DROPDOWN LATERAL --- */
.user-dropdown-crystal {
  position: absolute;
  left: calc(100% + 15px);
  bottom: 0;
  width: 180px;
  padding: 8px !important;
  z-index: 1000;
}

.dropdown-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  width: 100%;
  padding: 12px;
  border-radius: 14px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  color: #475569;
  transition: all 0.2s;
  white-space: nowrap;
}

.dropdown-item:hover {
  background: rgba(30, 64, 175, 0.05);
  color: #1E40AF;
}

.dropdown-icon { width: 18px; height: 18px; flex-shrink: 0; }
.dropdown-divider { height: 1px; background: rgba(0, 0, 0, 0.03); margin: 6px 8px; }

/* ANIMACIÓN FADE-RIGHT */
.fade-right-enter-active { transition: all 0.4s cubic-bezier(0.18, 0.89, 0.32, 1.28); }
.fade-right-leave-active { transition: all 0.3s cubic-bezier(0.6, -0.28, 0.735, 0.045); }
.fade-right-enter-from { opacity: 0; transform: translateX(-20px) scale(0.95); }
.fade-right-leave-to { opacity: 0; transform: translateX(-10px) scale(0.95); }

.is-collapsed .nav-item { justify-content: center; padding: 12px 0; }
.is-collapsed .user-profile { justify-content: center; padding: 8px 0; }
</style>
