<template>
  <div class="f-page-view">
    <header class="f-header">
      <div class="f-header-content">
        <h1 class="text-title">Asignaciones</h1>
      </div>
      <div class="f-header-actions">
        <button 
          class="btn-f-base btn-f-text"
          @click="$router.push('/projects')"
          style="margin: 0; padding: 4px 12px;"
        >
          Volver
        </button>
      </div>
    </header>

    <div class="profile-grid">
      <!-- PANEL LATERAL DE IDENTIDAD -->
      <aside class="profile-aside">
        <div class="identity-block-minimal">
          <div class="identity-text">
            <span class="text-label text-uppercase">Resumen de Asignación</span>
          </div>
          
          <div class="aside-stats-stack">
            <div class="aside-stat-item">
              <span class="stat-label">Personal Asignado</span>
              <span class="stat-value-aside f-tabular">{{ assignedCount }}</span>
            </div>
            <div class="aside-stat-item">
              <span class="stat-label">Proyecto Seleccionado</span>
              <span class="stat-value-aside" style="font-size: 14px; line-height: 1.2; display: block; margin-top: 4px;">
                {{ selectedProjectLabel || 'Ninguno' }}
              </span>
            </div>
          </div>
        </div>
      </aside>

      <!-- ÁREA PRINCIPAL -->
      <main class="profile-main-list">
        <!-- ÁREA DE CONTROL INTEGRADA -->
        <div class="control-area-elite animate-in delay-1">
          <div class="f-form-grid" style="grid-template-columns: 1.5fr 1fr; gap: 24px;">
            <div class="project-selector-wrapper">
              <div class="f-input-group">
                <label class="text-label mb-1 d-block">Proyectos</label>
                <div class="f-input-wrapper" :class="{ 'is-open': showProjectSelector }" @click="showProjectSelector = !showProjectSelector" v-click-outside="() => showProjectSelector = false">
                  <Briefcase class="input-icon" />
                  <div class="f-select-display" :class="{ 'is-placeholder': !selectedProject }">{{ selectedProjectLabel || 'Seleccionar proyecto' }}</div>
                  <transition name="fade-glass">
                    <div v-if="showProjectSelector" class="f-dropdown-glass">
                      <div class="select-option" @click.stop="clearProjectSelection">Ningún proyecto</div>
                      <div v-for="item in projects" :key="item.idProject" class="select-option" @click.stop="selectProjectItem(item)">{{ item.description }}</div>
                    </div>
                  </transition>
                </div>
              </div>
            </div>

            <!-- FILTRO DE ESTADO COMPACTO -->
            <div class="filter-wrapper" v-if="selectedProject">
              <div class="f-input-group">
                <label class="text-label mb-1 d-block">Filtrar por estado</label>
                <v-select
                  v-model="filterStatus"
                  :items="['Todos', 'Asignados', 'No asignados']"
                  density="compact"
                  variant="plain"
                  hide-details
                  class="f-vselect-custom"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- LISTA NAKED CRYSTAL (Igual que empleados) -->
        <div class="naked-list-container animate-in delay-2">
          <div class="list-header-minimal assignment-grid px-8 pb-4">
            <EliteSortLink 
              label="EMPLEADO" 
              :active="sortBy === 'name'" 
              :sortDesc="sortOrder === 'desc'" 
              @toggle="toggleSort('name')" 
            />
          </div>

          <div v-if="!selectedProject" class="empty-state-elite">
            <div class="empty-icon-wrapper">
              <FileText class="empty-icon" />
            </div>
            <p>Selecciona un proyecto para gestionar el equipo</p>
          </div>

          <div v-else-if="loading" class="empty-state-elite">
            <RefreshCw class="icon-md spin mb-4" />
            <p>Cargando asignaciones...</p>
          </div>

          <div v-else class="list-body">
            <div 
              v-for="item in filteredAssignments" 
              :key="item.idEmployee" 
              class="list-row-naked-crystal assignment-grid align-center px-8 py-3"
            >
              <div class="d-flex flex-column">
                <span class="text-primary">{{ item.firstName }} {{ item.lastName }} {{ item.secondLastName || '' }}</span>
                <span class="text-secondary">{{ item.email }}</span>
              </div>


              <div class="d-flex justify-end align-center pr-5" style="justify-self: end;">
                <button 
                  class="crystal-action-pill" 
                  :class="{ 'is-active': item.assigned, 'is-loading': processingId === item.idEmployee }"
                  @click="toggleAssignment(item, !item.assigned)"
                >
                  <span v-if="item.assigned" class="pill-content">
                    <Check class="pill-icon" /> ASIGNADO
                  </span>
                  <span v-else class="pill-content">
                    <Plus class="pill-icon" /> ASIGNAR
                  </span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from "vue";
import { FileText, Briefcase, Check, Plus, RefreshCw } from 'lucide-vue-next';
import EliteSortLink from '@/components/common/EliteSortLink.vue';
import { projectService } from '@/services/projectService';
import { assignmentService } from '@/services/assignmentService';
import { toast } from '@/services/toastService';

const loading = ref(false);
const sortBy = ref('name');
const sortOrder = ref('asc');
const processingId = ref(null);
const projects = ref([]);
const assignments = ref([]); 
const selectedProject = ref(null);
const showProjectSelector = ref(false);
const filterStatus = ref('Todos');

const toggleSort = (field) => {
  if (sortBy.value === field) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc';
  } else {
    sortBy.value = field;
    sortOrder.value = 'asc';
  }
};

const selectProjectItem = (item) => {
  selectedProject.value = item.idProject;
  showProjectSelector.value = false;
  filterStatus.value = 'Todos';
  fetchAssignments();
};

const clearProjectSelection = () => {
  selectedProject.value = null;
  assignments.value = [];
  showProjectSelector.value = false;
  filterStatus.value = 'Todos';
};

const assignedCount = computed(() => assignments.value.filter(a => a.assigned).length);
const selectedProjectLabel = computed(() => projects.value.find(p => p.idProject === selectedProject.value)?.description);

/**
 * Filtra y ordena la lista de empleados según lo que hayamos seleccionado en los filtros.
 */
const filteredAssignments = computed(() => {
  let result = [...assignments.value];

  // FILTRO DE ESTADO
  if (filterStatus.value === 'Asignados') {
    result = result.filter(a => a.assigned)
  } else if (filterStatus.value === 'No asignados') {
    result = result.filter(a => !a.assigned)
  }

  return result.sort((a, b) => {
    const valA = sortBy.value === 'name' ? `${a.firstName} ${a.lastName}` : a.email;
    const valB = sortBy.value === 'name' ? `${b.firstName} ${b.lastName}` : b.email;
    return sortOrder.value === 'asc' ? valA.localeCompare(valB) : valB.localeCompare(valA);
  });
});

const init = async () => {
  try {
    projects.value = await projectService.getAll(false)
  } catch (e) {
    toast.error("Error al recuperar catálogo de proyectos");
  }
};

/**
 * Trae del servidor los empleados y si están asignados al proyecto que hemos elegido.
 */
const fetchAssignments = async () => {
  if (!selectedProject.value) return;
  loading.value = true;
  try {
    assignments.value = await assignmentService.getByProject(selectedProject.value)
  } catch (e) {
    toast.error("Error al sincronizar estado de asignaciones");
  } finally {
    loading.value = false;
  }
};

/**
 * Asigna o quita a un empleado del proyecto y actualiza la lista.
 */
const toggleAssignment = async (employee, shouldAssign) => {
  processingId.value = employee.idEmployee;
  try {
    if (shouldAssign) {
      await assignmentService.assign(selectedProject.value, employee.idEmployee)
      toast.success("Asignación confirmada");
    } else {
      await assignmentService.unassign(selectedProject.value, employee.idEmployee)
      toast.success("Asignación revocada");
    }
    await fetchAssignments();
  } catch (e) {
    toast.error("Error al procesar la vinculación");
  } finally {
    processingId.value = null;
  }
};

onMounted(init);
</script>

<style scoped>
.list-row-naked-crystal {
  position: relative;
  transition: all 0.2s ease; 
  border-left: 3px solid transparent;
  border-bottom: 1px solid #E2E8F0;
}

.list-row-naked-crystal:hover { 
  background: #F8FAFC;
  border-left-color: rgb(var(--v-theme-primary));
}

.control-area-elite {
  padding: 0 32px;
  margin-bottom: 32px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: relative;
  z-index: 10;
}

.control-area-elite:focus-within, 
.control-area-elite:has(.is-open) {
  z-index: 10001;
}

.project-selector-wrapper {
  margin-top: 8px;
}

.f-tabular { font-variant-numeric: tabular-nums; }
.f-center { display: flex; justify-content: center; }

/* GRID SISTÉMICO PARA ASIGNACIONES */
.assignment-grid {
  display: grid;
  grid-template-columns: 1fr 120px;
  gap: 12px;
  align-items: center;
  width: 100%;
}

/* EMPTY STATE ELITE */
.empty-state-elite {
  padding: 80px 0;
  text-align: center;
  color: rgb(var(--v-theme-on-surface));
  opacity: 0.5;
}
.empty-icon-wrapper {
  width: 64px;
  height: 64px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}
.empty-icon { width: 32px; height: 32px; color: rgb(var(--v-theme-on-surface)); opacity: 0.3; }

.sort-divider { color: #CBD5E1; font-size: 10px; }

/* CRYSTAL ACTION PILL - ESTILIZACIÓN ULTRA-FINA */
.crystal-action-pill {
  padding: 2px 10px;
  border-radius: 40px;
  background: transparent;
  border: 1px solid rgba(15, 23, 42, 0.04);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pill-content {
  display: flex;
  align-items: center;
  gap: 5px;
  font-family: 'Inter', sans-serif;
  font-size: 8.5px;
  font-weight: 500;
  letter-spacing: 0.12em;
  color: #94A3B8;
  text-transform: uppercase;
}

.pill-icon {
  width: 10px;
  height: 10px;
  stroke-width: 2px;
}

.crystal-action-pill:hover {
  border-color: #CBD5E1;
  background: rgba(15, 23, 42, 0.01);
}

.crystal-action-pill.is-active {
  border-color: rgba(30, 64, 175, 0.15);
  background: rgba(30, 64, 175, 0.02);
}

.crystal-action-pill.is-active .pill-content {
  color: #1E40AF;
}

.crystal-action-pill.is-loading {
  opacity: 0.5;
  pointer-events: none;
  animation: pill-pulse 1.5s infinite;
}

@keyframes pill-pulse {
  0% { transform: scale(1); }
  50% { transform: scale(0.97); }
  100% { transform: scale(1); }
}
</style>
