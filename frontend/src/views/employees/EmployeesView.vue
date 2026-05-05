<template>
  <div class="f-page-view">
    <header class="f-header">
      <div class="f-header-content">
        <h1 class="text-title">Empleados</h1>
      </div>
      <div class="f-header-actions">
        <button 
          class="btn-f-base btn-f-text"
          @click="router.push({ name: 'employee-new' })"
        >
          <UserPlus class="icon-sm mr-2" />
          Alta Empleado
        </button>
      </div>
    </header>

    <div class="profile-grid">
      <aside class="profile-aside">
        <div class="identity-block-minimal">
          <div class="identity-text">
            <span class="text-label text-uppercase">Resumen de Plantilla</span>
          </div>
          
          <div class="aside-stats-stack">
            <div class="aside-stat-item">
              <span class="stat-label">Personal Activo</span>
              <span class="stat-value-aside f-tabular">{{ activeEmployeesCount }}</span>
            </div>
            <div class="aside-stat-item">
              <span class="stat-label">Proyectos Activos</span>
              <span class="stat-value-aside f-tabular">{{ activeProjectsCount }}</span>
            </div>
          </div>
        </div>
      </aside>

      <main class="profile-main-list">
        <!-- SEARCH INTEGRADO -->
        <div class="search-area-naked">
          <EliteSearch v-model="search" placeholder="Buscar empleado..." />
        </div>

        <!-- NAKED LIST CONTAINER -->
        <div class="naked-list-container animate-in delay-1">
          <div class="list-header-minimal employee-grid px-8 pb-4">
            <EliteSortLink 
              label="EMPLEADO" 
              :active="sortBy === 'firstName'" 
              :sortDesc="sortDesc" 
              @toggle="toggleSort('firstName')" 
            />
            
            <EliteSortLink 
              label="NIF / NIE" 
              align="right"
              :active="sortBy === 'nif'" 
              :sortDesc="sortDesc" 
              @toggle="toggleSort('nif')" 
            />
            
            <EliteSortLink 
              label="F. NACIMIENTO" 
              align="right"
              :active="sortBy === 'birthDate'" 
              :sortDesc="sortDesc" 
              @toggle="toggleSort('birthDate')" 
            />
            
            <EliteSortLink 
              label="TELÉFONO" 
              align="right"
              :active="sortBy === 'phone1'" 
              :sortDesc="sortDesc" 
              @toggle="toggleSort('phone1')" 
            />
            
            <EliteSortLink 
              label="EST. CIVIL" 
              align="center"
              :active="sortBy === 'maritalStatus'" 
              :sortDesc="sortDesc" 
              @toggle="toggleSort('maritalStatus')" 
            />
            
            <EliteSortLink 
              label="FORMACIÓN" 
              align="center"
              :active="sortBy === 'hasUniversityEducation'" 
              :sortDesc="sortDesc" 
              @toggle="toggleSort('hasUniversityEducation')" 
            />
            <div class="text-label"></div>
          </div>

          <div v-if="loading" class="empty-state-elite">
            <RefreshCw class="icon-md spin mb-4" />
            <p>Sincronizando base de datos de capital humano...</p>
          </div>

          <div v-else class="list-body">
            <div v-for="item in displayedEmployees" :key="item.idEmployee" class="list-row-naked-crystal employee-grid align-center px-8 py-3">
              <!-- IZQUIERDA -->
              <div class="d-flex flex-column" style="min-width: 0;">
                <span class="text-primary text-truncate">{{ item.firstName }} {{ item.lastName }} {{ item.secondLastName || '' }}</span>
                <span class="text-secondary text-truncate" style="font-size: 11px;">{{ item.email }}</span>
              </div>

              <!-- DERECHA -->
              <div class="text-secondary text-right f-tabular">{{ item.nif }}</div>
              <div class="text-secondary text-right f-tabular">{{ formatDate(item.birthDate) }}</div>
              <div class="text-secondary text-right f-tabular">{{ item.phone1 || '—' }}</div>
              <div class="text-secondary text-center">{{ item.maritalStatus === 'S' ? 'Soltero' : 'Casado' }}</div>
              <div class="text-secondary text-center">{{ item.hasUniversityEducation === 'S' ? 'Si' : 'No' }}</div>

              <div class="col-actions">
                <div class="row-actions-floating">
                  <button 
                    class="btn-f-base btn-f-text"
                    @click="router.push({ name: 'employee-edit', params: { id: item.idEmployee } })"
                  >
                    <Edit3 class="icon-xs" />
                  </button>
                  
                  <button 
                    class="btn-f-base btn-f-text"
                    style="color: #DC2626 !important;"
                    @click="confirmDelete(item)"
                  >
                    <UserMinus class="icon-xs" />
                  </button>
                </div>
              </div>
            </div>

            <!-- CARGADOR ELITE -->
            <div v-if="hasMore" class="text-center py-8">
              <button 
                class="btn-f-base btn-f-text"
                @click="loadMore"
              >
                Cargar más empleados
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- DIÁLOGO DE CESE ELITE -->
    <v-dialog v-model="confirmDialog.show" max-width="360" persistent scrim="rgba(15, 23, 42, 0.4)">
      <CrystalCard class="pa-6" :hoverable="false">
        <h2 class="text-h6 font-weight-bold mb-6">Eliminar empleado</h2>
        
        <p class="mb-4" style="font-size: 14px; color: #475569;">
          Esta acción eliminará permanentemente a <strong>{{ confirmDialog.employeeName }}</strong>.
        </p>

        <div class="f-toast-error mb-8">
          <AlertTriangle :size="14" />
          <span>Esta acción no se puede deshacer.</span>
        </div>

        <div class="d-flex justify-end gap-2">
          <v-btn 
            variant="text" 
            class="btn-f-base btn-confirm text-secondary" 
            @click="confirmDialog.show = false"
          >
            Cancelar
          </v-btn>
          <v-btn 
            variant="flat" 
            class="btn-destructive-tonal btn-confirm" 
            @click="handleTerminate"
          >
            Eliminar
          </v-btn>
        </div>
      </CrystalCard>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { UserPlus, Edit3, UserMinus, RefreshCw, AlertTriangle } from 'lucide-vue-next'
import CrystalCard from '@/components/common/CrystalCard.vue'
import EliteSearch from '@/components/common/EliteSearch.vue'
import EliteSortLink from '@/components/common/EliteSortLink.vue'
import { employeeService } from '@/services/employeeService'
import { projectService } from '@/services/projectService'
import { toast } from '@/services/toastService';

const router = useRouter()
const employees = ref([])
const activeEmployeesCount = ref(0)
const activeProjectsCount = ref(0)
const search = ref('')
const sortBy = ref('firstName')
const sortDesc = ref(false)
const loading = ref(true)
const visibleCount = ref(10)

// Estado del Diálogo de Confirmación
const confirmDialog = reactive({
  show: false,
  employeeId: null,
  employeeName: ''
})

/**
 * Lógica de filtrado y ordenación alfabética humana (A-Z por Nombre completo)
 */
const filteredEmployees = computed(() => {
  let list = employees.value
  if (search.value) {
    const q = search.value.toLowerCase()
    list = list.filter(e => 
      `${e.firstName} ${e.lastName}`.toLowerCase().includes(q) ||
      e.nif.toLowerCase().includes(q)
    )
  }
  return list.sort((a, b) => {
    let fieldA = (a[sortBy.value] || '').toString().toLowerCase()
    let fieldB = (b[sortBy.value] || '').toString().toLowerCase()
    
    if (sortBy.value === 'firstName') {
      fieldA = `${a.firstName} ${a.lastName}`.toLowerCase()
      fieldB = `${b.firstName} ${b.lastName}`.toLowerCase()
    }
    
    let mod = sortDesc.value ? -1 : 1
    return fieldA < fieldB ? -1 * mod : (fieldA > fieldB ? 1 * mod : 0)
  })
})

// Solo mostramos un trozo para no saturar la UI
const displayedEmployees = computed(() => {
  return filteredEmployees.value.slice(0, visibleCount.value)
})

const hasMore = computed(() => {
  return visibleCount.value < filteredEmployees.value.length
})

const loadMore = () => {
  visibleCount.value += 10
}

watch(search, () => { visibleCount.value = 10 })

const toggleSort = (key) => {
  if (sortBy.value === key) sortDesc.value = !sortDesc.value
  else { sortBy.value = key; sortDesc.value = false }
}

const confirmDelete = (item) => {
  confirmDialog.employeeId = item.idEmployee
  confirmDialog.employeeName = `${item.firstName} ${item.lastName}`
  confirmDialog.show = true
}

const handleTerminate = async () => {
  try {
    await employeeService.delete(confirmDialog.employeeId)
    confirmDialog.show = false
    const allActive = await employeeService.getActive()
    employees.value = allActive
    activeEmployeesCount.value = allActive.length
    toast.success('Cese tramitado correctamente')
  } catch (e) {
    confirmDialog.show = false
    toast.error(e.response?.data?.message || 'Error al procesar el cese')
  }
}

const formatDate = (date) => {
  if (!date) return '—'
  return new Date(date).toLocaleDateString('es-ES')
}

onMounted(async () => {
  try {
    loading.value = true
    const [allActive, projData] = await Promise.all([
      employeeService.getActive(),
      projectService.getAll(false)
    ])
    employees.value = allActive
    activeEmployeesCount.value = allActive.length
    activeProjectsCount.value = projData.length
  } catch (e) {
    console.error('Error al cargar datos:', e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
/* Layout estandarizado vía f-page-view */
.header-stats-minimal { display: flex; align-items: center; gap: 12px; margin-top: 8px; }
.stat-pill { background: #F1F5F9; padding: 2px 10px; border-radius: 99px; font-weight: 800; font-size: 12px; color: #64748B; }

/* Botón "Nuevo empleado" CLONADO EXACTO de ProfileView */
.btn-elite-outline {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none !important;
  color: rgb(var(--v-theme-primary));
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  padding: 0;
  transition: opacity 0.2s ease;
}

.btn-elite-outline:hover {
  opacity: 0.65;
}

.btn-elite-outline .icon-sm {
  width: 14px; /* Ajustado al estándar de icono de botón de texto */
  height: 14px;
  stroke-width: 1.75px;
}


.identity-text h2 {
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
  margin: 0;
  color: #1E293B;
  letter-spacing: -0.04em;
  line-height: 1;
}

.identity-text .role-text {
  font-size: 10px;
  color: #64748B;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-top: 4px;
  display: block;
}

.f-tabular { font-variant-numeric: tabular-nums; }

.profile-main-list { display: flex; flex-direction: column; }

.search-area-naked { margin-bottom: 24px; padding: 0 32px; }

.list-row-naked-crystal {
  position: relative;
  transition: all 0.2s ease; 
  border-left: 3px solid transparent;
  border-bottom: 1px solid #E2E8F0;
}

.list-header-minimal {
  border-left: 3px solid transparent;
}

.list-row-naked-crystal:hover { 
  background: #F8FAFC;
  border-left-color: rgb(var(--v-theme-primary));
}

.f-center { display: flex; justify-content: center; text-align: center; }

.row-actions-floating { 
  opacity: 0; 
  transform: translateX(10px); 
  transition: 0.3s; 
  display: flex; 
  gap: 8px; 
  justify-content: flex-end;
}
.list-row-naked-crystal:hover .row-actions-floating { opacity: 1; transform: translateX(0); }

/* GRID SISTÉMICO PARA EMPLEADOS */
.employee-grid {
  display: grid;
  grid-template-columns: minmax(0, 2.5fr) minmax(0, 1fr) minmax(0, 1.2fr) minmax(0, 1.1fr) minmax(0, 0.9fr) minmax(0, 0.9fr) minmax(0, 0.8fr);
  gap: 12px;
  align-items: center;
  width: 100%;
}
</style>
