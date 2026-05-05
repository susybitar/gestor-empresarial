<template>
  <div class="f-page-view">
    <div class="aura aura-2"></div>

    <!-- CABECERA ESTRATÉGICA -->
    <header class="f-header">
      <div class="f-header-content">
        <h1 class="text-h4 font-weight-bold">{{ greeting }}, {{ userName }}</h1>
      </div>
      
      <div class="f-header-actions">
        <button 
          class="btn-f-base btn-f-outline"
          @click="$router.push('/employees')"
        >
          <UserPlus class="icon-sm mr-2" />
          Alta Empleados
        </button>
        
        <button 
          class="btn-f-base btn-f-primary"
          @click="$router.push('/projects')"
        >
          <Plus class="icon-sm mr-2" />
          Alta Proyectos
        </button>
      </div>
    </header>

    <!-- MALLA DE INDICADORES (KPIs) -->
    <section class="f-kpi-grid animate-in">
      <StatCard 
        v-for="kpi in kpiMetrics" 
        :key="kpi.label"
        v-bind="kpi"
      />
    </section>

    <!-- BENTO GRID DE VISUALIZACIÓN -->
    <section class="f-bento-grid">
      <ChartArea
        title="Flujo de Proyectos"
        subtitle="Sincronización mensual"
        :data="barData"
        class="animate-in delay-1"
      />

      <DataFeed
        title="Sedes Operativas"
        subtitle="Reparto geográfico"
        :projectsCount="statistics.projects"
        :locations="locations"
        :latestProjects="latestProjects"
        class="animate-in delay-2"
      />
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { fetchDashboardData } from '@/services/dashboardService'
import { UserPlus, Users, Plus, Briefcase, Activity } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { toast } from '@/services/toastService'
import StatCard from '@/components/dashboard/StatCard.vue'
import ChartArea from '@/components/dashboard/ChartArea.vue'
import DataFeed from '@/components/dashboard/DataFeed.vue'

const authStore = useAuthStore()
const storedName = localStorage.getItem('fsm_user_name')
const userName = ref(storedName ? storedName.split(' ')[0] : 'Admin')
const barData = ref([0, 0, 0, 0, 0, 0])
const locations = ref([])
const latestProjects = ref([])

const statistics = reactive({ employees: 0, projects: 0, occupancy: 0, universityRatio: 0 })

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return 'Buenos días'
  if (hour < 20) return 'Buenas tardes'
  return 'Buenas noches'
})

/**
 * Prepara las métricas que vamos a mostrar en las tarjetas (KPIs).
 * Calculamos el porcentaje de universitarios y la ocupación en base a los datos.
 */
const kpiMetrics = computed(() => [
  { label: 'Empleados', value: statistics.employees, accent: '#64748B', icon: Users, trendLabel: `${statistics.universityRatio}% Universitarios` },
  { label: 'Proyectos', value: statistics.projects, accent: '#64748B', icon: Briefcase, trendLabel: 'Vigentes' },
  { label: 'Ocupación', value: statistics.occupancy, suffix: '%', accent: '#64748B', icon: Activity, trendLabel: 'Asignación activa' }
])

/**
 * Carga todos los datos necesarios para el dashboard y calcula las estadísticas.
 */
const sync = async () => {
  try {
    const { employees, projects, assignments } = await fetchDashboardData()

    const activeE = employees.filter(e => !e.terminationDate)
    const activeP = projects.filter(p => !p.terminationDate)

    statistics.employees = activeE.length
    statistics.projects = activeP.length
    
    const assignedEmpIds = new Set(assignments.map(a => a.idEmployee))
    statistics.occupancy = activeE.length > 0 ? Math.round((assignedEmpIds.size / activeE.length) * 100) : 0
    statistics.universityRatio = activeE.length > 0 ? Math.round((activeE.filter(e => e.hasUniversityEducation === 'S').length / activeE.length) * 100) : 0

    // Histórico de proyectos
    const currentMonth = new Date().getMonth()
    const monthlyCounts = new Array(6).fill(0)
    activeP.forEach(p => {
      const pDate = new Date(p.startDate)
      const diff = currentMonth - pDate.getMonth()
      if (diff >= 0 && diff < 6) monthlyCounts[5 - diff]++
    })
    barData.value = monthlyCounts

    // Sedes
    const locMap = {}
    activeP.forEach(p => locMap[p.location] = (locMap[p.location] || 0) + 1)
    locations.value = Object.entries(locMap).map(([name, count]) => ({
      name, pct: Math.round((count / activeP.length) * 100)
    })).sort((a, b) => b.pct - a.pct)

    latestProjects.value = activeP.slice(0, 4).map(p => ({
      idProject: p.idProject, projectName: p.description, location: p.location
    }))
  } catch (e) {
    console.error('[Dashboard] Error Sync:', e)
    toast.error('Error al cargar el panel de control')
  }
}

onMounted(sync)
</script>

<style scoped>
.aura { position: fixed; border-radius: 50%; filter: blur(120px); z-index: -1; opacity: 0.15; pointer-events: none; }
.aura-2 { width: 500px; height: 500px; background: radial-gradient(circle, #10B981, transparent); bottom: -100px; left: -100px; }

.f-kpi-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 24px; margin-bottom: 32px; }
.f-bento-grid { display: grid; grid-template-columns: 1fr; gap: 24px; }

@media (min-width: 1200px) {
  .f-bento-grid { grid-template-columns: 1.8fr 1.2fr; }
}

.animate-in { opacity: 0; transform: translateY(10px); animation: slideUp 0.6s cubic-bezier(0.2, 0.8, 0.2, 1) forwards; }
.delay-1 { animation-delay: 0.1s; }
.delay-2 { animation-delay: 0.2s; }

@keyframes slideUp { to { opacity: 1; transform: translateY(0); } }

.icon-sm { width: 14px; height: 14px; stroke-width: 2px; }
</style>
