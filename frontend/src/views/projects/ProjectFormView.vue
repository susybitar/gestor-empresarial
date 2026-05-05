<template>
  <div class="f-page-view animate-in">
    <header class="f-header">
      <div class="f-header-content">
        <h1 class="text-title">{{ isEdit ? 'Editar Proyecto' : 'Nuevo Proyecto' }}</h1>
      </div>
      <div class="f-header-actions">
        <button class="btn-f-base btn-f-text" @click="router.back()">
          Cancelar
        </button>
        <button class="btn-f-base btn-f-text" @click="save" :disabled="isSaving">
          <template v-if="isSaving">
            <RefreshCw class="icon-xs mr-2 spin" />
            Guardando...
          </template>
          <template v-else>
            <Check class="icon-xs mr-2" />
            Guardar proyecto
          </template>
        </button>
      </div>
    </header>

    <div class="profile-grid">
      <aside class="profile-aside">
        <div class="identity-block-minimal">
          <div class="avatar-minimal">
            <span class="initials">{{ projectInitials }}</span>
          </div>
          <div class="identity-text">
            <h2 class="text-primary font-weight-bold">{{ form.description || 'Nuevo Proyecto' }}</h2>
            <span class="text-label text-uppercase">Ficha de Proyecto Operativo</span>
          </div>
        </div>
      </aside>

      <main class="profile-main f-card-naked animate-in delay-1 f-form-minimal">
        <div class="settings-section">
          <div class="section-header">
            <h3 class="text-primary font-weight-bold mb-4">Datos del Proyecto</h3>
          </div>

          <div class="f-form-grid">
            <div class="f-span-2">
              <CrystalInput 
                v-model="form.description" 
                label="Nombre del Proyecto" 
                :icon="Briefcase" 
                :error="errors.description" 
                maxlength="125" 
                @update:modelValue="validateForm(true)"
              />
            </div>
            
            <CrystalInput 
              v-model="form.location" 
              label="Sede / Ubicación" 
              :icon="MapPin" 
              :error="errors.location" 
              maxlength="30"
              @update:modelValue="validateForm(true)"
            />

            <div class="f-input-group">
              <label class="text-label mb-1 d-block">Fecha de Inicio</label>
              <div class="f-input-wrapper" :class="{ 'has-error': errors.startDate, 'is-open': showStartPicker && !isEdit }" @click="!isEdit && (showStartPicker = !showStartPicker)" v-click-outside="() => showStartPicker = false">
                <Calendar class="input-icon" />
                <div class="f-select-display">{{ formatDisplayDate(form.startDate) || 'Seleccionar fecha' }}</div>
                <transition name="fade-glass">
                  <div v-if="showStartPicker" class="f-dropdown-glass p-4" @click.stop>
                    <v-date-picker 
                      v-model="form.startDate" 
                      color="primary" 
                      flat 
                      hide-header 
                      @update:modelValue="onStartDateSelected"
                    />
                  </div>
                </transition>
              </div>
              <span v-if="errors.startDate" class="f-error-message">{{ errors.startDate }}</span>
            </div>

            <div class="f-input-group">
              <label class="text-label mb-1 d-block">Fecha de Cierre (Prevista)</label>
              <div class="f-input-wrapper" :class="{ 'has-error': errors.endDate, 'is-open': showEndPicker }" @click="showEndPicker = !showEndPicker" v-click-outside="() => showEndPicker = false">
                <Calendar class="input-icon" />
                <div class="f-select-display">{{ formatDisplayDate(form.endDate) || 'Seleccionar fecha' }}</div>
                <transition name="fade-glass">
                  <div v-if="showEndPicker" class="f-dropdown-glass p-4" @click.stop>
                    <v-date-picker 
                      v-model="form.endDate" 
                      color="primary" 
                      flat 
                      hide-header 
                      @update:modelValue="onEndDateSelected"
                    />
                  </div>
                </transition>
              </div>
              <span v-if="errors.endDate" class="f-error-message">{{ errors.endDate }}</span>
            </div>
          </div>
        </div>

        <div class="section-divider"></div>

        <div class="settings-section">
          <div class="f-form-grid">
            <div class="f-span-2">
              <label class="text-label mb-1 d-block">Observaciones Estratégicas</label>
              <div class="f-textarea-minimal">
                <v-textarea
                  v-model="form.observations"
                  variant="plain"
                  maxlength="300"
                  counter
                  placeholder="Describa los puntos clave del proyecto..."
                  :error-messages="errors.observations"
                  hide-details="auto"
                  rows="4"
                  @update:modelValue="validateForm(true)"
                />
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Check, RefreshCw, Briefcase, MapPin, Calendar } from 'lucide-vue-next'
import { projectService } from '@/services/projectService'
import { toast } from '@/services/toastService'
import CrystalInput from '@/components/common/CrystalInput.vue'

const route = useRoute()
const router = useRouter()
const isEdit = Boolean(route.params.id)
const isSaving = ref(false)
const showStartPicker = ref(false)
const showEndPicker = ref(false)
const errors = reactive({
  description: '', location: '', startDate: '', endDate: '', observations: ''
})

const form = reactive({ idProject: null, description: '', startDate: null, endDate: null, location: '', observations: '' })

watch(() => form.description, () => { errors.description = '' })
watch(() => form.location, () => { errors.location = '' })
watch(() => form.startDate, () => { errors.startDate = '' })
watch(() => form.observations, () => { errors.observations = '' })

/**
 * Revisa que todos los campos del proyecto estén rellenos y que las fechas tengan sentido.
 * @param {Boolean} silent - Si es true, no muestra los mensajes de error en la pantalla.
 */
const validateForm = (silent = false) => {
  const localErrors = {
    description: !form.description ? 'El nombre del proyecto es obligatorio' : form.description.length > 125 ? 'Máximo 125 caracteres' : '',
    location: !form.location ? 'La ubicación es obligatoria' : form.location.length > 30 ? 'Máximo 30 caracteres' : '',
    startDate: !form.startDate ? 'La fecha de inicio es obligatoria' : '',
    observations: !form.observations ? 'Las observaciones son obligatorias' : form.observations.length > 300 ? 'Máximo 300 caracteres' : ''
  }

  if (!form.endDate) {
    localErrors.endDate = 'La fecha de cierre es obligatoria'
  } else if (form.startDate && new Date(form.endDate) <= new Date(form.startDate)) {
    localErrors.endDate = 'La fecha de cierre debe ser posterior a la de inicio'
  } else {
    localErrors.endDate = ''
  }

  if (!silent) {
    Object.assign(errors, localErrors)
  }

  return !Object.values(localErrors).some(v => v !== '')
}

const onStartDateSelected = () => {
  showStartPicker.value = false
  validateForm(true)
}

const onEndDateSelected = () => {
  showEndPicker.value = false
  validateForm(true)
}

const formatDisplayDate = (date) => date ? new Date(date).toLocaleDateString('es-ES') : ''
const projectInitials = computed(() => form.description ? form.description.substring(0, 2).toUpperCase() : 'P')

/**
 * Guarda los cambios del proyecto (crear uno nuevo o editar).
 * Ajustamos el formato de las fechas antes de enviarlas para evitar líos con las zonas horarias.
 */
const save = async () => {
  if (!validateForm()) return toast.error('Revise los campos marcados en rojo')
  
  isSaving.value = true
  // Limpiar errores previos
  Object.keys(errors).forEach(key => errors[key] = '')

  try {
    const payload = { ...form }
    const normalize = (date) => {
      if (!date) return null
      const d = new Date(date)
      d.setMinutes(d.getMinutes() - d.getTimezoneOffset())
      return d.toISOString().split('T')[0]
    }
    payload.startDate = normalize(payload.startDate)
    payload.endDate = normalize(payload.endDate)

    if (isEdit) await projectService.update(form.idProject, payload)
    else await projectService.create(payload)
    toast.success('Proyecto guardado correctamente')
    router.push({ name: 'projects' })
  } catch (e) {
    if (e.response?.status === 400 && e.response?.data?.errors) {
      const backendErrors = e.response.data.errors
      Object.keys(backendErrors).forEach(field => {
        if (field in errors) errors[field] = backendErrors[field]
      })
      toast.error('Existen errores en los datos enviados')
    } else {
      const status = e.response?.status
      if (!status || (status !== 401 && status !== 403 && status < 500)) {
        toast.error(e.response?.data?.message || 'Error al guardar el proyecto')
      }
    }
  } finally { isSaving.value = false }
}

/**
 * Carga los datos del proyecto cuando entramos en modo edición.
 */
onMounted(async () => {
  if (isEdit) {
    try {
      Object.assign(form, await projectService.getById(route.params.id))
    } catch {
      toast.error('No se pudo cargar el proyecto')
      router.push({ name: 'projects' })
    }
  }
})
</script>

<style scoped>
.avatar-minimal {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #F8FAFC;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  border: 1px solid #E2E8F0;
}

.initials { font-family: 'Outfit'; font-size: 22px; color: #1E293B; font-weight: 700; }
.settings-section { padding: 12px 0; }
.section-divider { height: 1px; background: #E2E8F0; margin: 12px 0; }
.f-textarea-minimal { 
  background: transparent; 
  border: none; 
  border-bottom: 1px solid #CBD5E1; 
  border-radius: 0; 
  padding: 8px 0; 
  transition: all 0.2s ease;
}
.f-textarea-minimal:focus-within {
  border-bottom-color: #1E40AF;
}
.p-4 { padding: 16px; }

.spin { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.icon-xs { width: 14px; height: 14px; }

/* DISEÑO MINIMALISTA DE LÍNEA FINA */
.f-form-minimal :deep(.f-input-wrapper) {
  background: transparent !important;
  border: none !important;
  border-bottom: 1px solid #CBD5E1 !important;
  border-radius: 0 !important;
  padding-left: 0 !important;
  padding-right: 0 !important;
  box-shadow: none !important;
  backdrop-filter: none !important;
  transition: border-color 0.3s ease;
}

.f-form-minimal :deep(.f-input-wrapper:focus-within) {
  border-bottom-color: #1E40AF !important;
}

.f-form-minimal :deep(.f-input-wrapper.has-error) {
  border-bottom-color: #EF4444 !important;
}

.f-form-minimal :deep(.f-error-message) {
  font-weight: 400;
  font-size: 11px;
  margin-top: 4px;
}

.f-form-minimal :deep(.input-icon) {
  color: #94A3B8;
  width: 16px;
  margin-right: 8px;
}

.f-form-minimal :deep(input),
.f-form-minimal :deep(textarea) {
  font-family: var(--font-main) !important;
  font-size: 14px !important;
  color: #1E293B !important;
}

.f-form-minimal :deep(textarea::placeholder) {
  font-size: 14px !important;
  opacity: 0.5;
}
</style>
