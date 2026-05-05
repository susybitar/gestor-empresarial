<template>
  <div class="f-page-view animate-in">
    <header class="f-header">
      <div class="f-header-content">
        <h1 class="text-title">{{ isEdit ? 'Editar Empleado' : 'Nuevo Empleado' }}</h1>
      </div>
      <div class="f-header-actions">
        <button class="btn-f-base btn-f-text" @click="router.back()">
          Cancelar
        </button>
        <button
          class="btn-f-base btn-f-text"
          @click="save"
          :disabled="isSaving"
        >
          <template v-if="isSaving">
            <RefreshCw class="icon-xs mr-2 spin" />
            Guardando...
          </template>
          <template v-else>
            <Check class="icon-xs mr-2" />
            Guardar empleado
          </template>
        </button>
      </div>
    </header>

    <div class="profile-grid">
      <aside class="profile-aside">
        <div class="identity-block-minimal">
          <div class="avatar-minimal">
            <span class="initials">{{ userInitials }}</span>
          </div>
          <div class="identity-text">
            <h2 class="text-primary font-weight-bold">{{ form.firstName || 'Nuevo' }} {{ form.lastName || 'Empleado' }}</h2>
            <span class="text-label text-uppercase">Ficha de empleado</span>
          </div>
        </div>
      </aside>

      <main class="profile-main f-card-naked animate-in delay-1 f-form-minimal">
        <!-- SECCIÓN 1: DATOS PERSONALES -->
        <div class="settings-section">
          <div class="section-header">
            <h3 class="text-primary font-weight-bold mb-4">Datos Personales</h3>
          </div>

          <div class="f-form-grid">
            <CrystalInput 
              v-model="form.firstName" 
              label="Nombre" 
              :icon="User" 
              :error="errors.firstName" 
              @update:modelValue="validateForm(true)"
            />
            <CrystalInput 
              v-model="form.lastName" 
              label="Primer Apellido" 
              :icon="User" 
              :error="errors.lastName" 
              @update:modelValue="validateForm(true)"
            />
            <CrystalInput 
              v-model="form.secondLastName" 
              label="Segundo Apellido" 
              :icon="User" 
              :error="errors.secondLastName" 
              @update:modelValue="validateForm(true)"
            />
            <CrystalInput
              v-model="form.nif"
              label="NIF / NIE"
              placeholder="01234567J"
              :icon="CreditCard"
              :error="errors.nif"
              maxlength="9"
              :readonly="isEdit"
              @update:modelValue="validateForm(true)"
            />
            
            <!-- Selector de Fecha Estandarizado -->
            <div class="f-input-group">
              <label class="text-label mb-1 d-block">Fecha de Nacimiento</label>
              <div class="f-input-wrapper" :class="{ 'has-error': errors.birthDate, 'is-open': showDatePicker }" @click="showDatePicker = !showDatePicker" v-click-outside="() => showDatePicker = false">
                <Calendar class="input-icon" />
                <div class="f-select-display">{{ formatDisplayDate(form.birthDate) || 'Seleccionar fecha' }}</div>
                <transition name="fade-glass">
                  <div v-if="showDatePicker" class="f-dropdown-glass p-4" @click.stop>
                    <v-date-picker 
                      v-model="form.birthDate" 
                      color="primary" 
                      flat 
                      hide-header 
                      @update:modelValue="onDateSelected"
                    />
                  </div>
                </transition>
              </div>
              <span v-if="errors.birthDate" class="f-error-message">{{ errors.birthDate }}</span>
            </div>

            <!-- Selector de Estado Civil Estandarizado -->
            <div class="f-input-group">
              <label class="text-label mb-1 d-block">Estado Civil</label>
              <div class="f-input-wrapper" :class="{ 'is-open': activeSelect === 'marital' }" @click="toggleSelect('marital')" v-click-outside="() => closeSelect('marital')">
                <Users class="input-icon" />
                <div class="f-select-display">{{ getLabel(form.maritalStatus, maritalOptions) }}</div>
                <transition name="fade-glass">
                  <div v-if="activeSelect === 'marital'" class="f-dropdown-glass">
                    <div v-for="opt in maritalOptions" :key="opt.value" class="select-option" @click.stop="selectOpt('marital', opt.value)">
                      {{ opt.label }}
                    </div>
                  </div>
                </transition>
              </div>
            </div>
          </div>
        </div>

        <div class="section-divider"></div>

        <!-- SECCIÓN 2: INFORMACIÓN CORPORATIVA -->
        <div class="settings-section">
          <div class="section-header">
            <h3 class="text-primary font-weight-bold mb-4">Información Corporativa</h3>
          </div>
          <div class="f-form-grid">
            <CrystalInput 
              v-model="form.email" 
              label="Email Corporativo" 
              type="email" 
              :icon="Mail" 
              :error="errors.email" 
              @update:modelValue="validateForm(true)"
            />
            <CrystalInput 
              v-model="form.phone1" 
              label="Teléfono Principal" 
              :icon="Phone" 
              :error="errors.phone1" 
              @update:modelValue="validateForm(true)"
            />
            <CrystalInput 
              v-model="form.phone2" 
              label="Teléfono Secundario" 
              :icon="Phone" 
              :error="errors.phone2" 
              @update:modelValue="validateForm(true)"
            />
            <CrystalInput :modelValue="form.hireDate" label="Fecha de Alta" :icon="Lock" disabled readonly />
          </div>
        </div>

        <div class="section-divider"></div>

        <!-- SECCIÓN 3: NIVEL ACADÉMICO -->
        <div class="settings-section">
          <div class="section-header">
            <h3 class="text-primary font-weight-bold mb-4">Nivel Académico</h3>
          </div>
          <div class="f-form-grid">
            <div class="f-input-group f-span-2">
              <label class="text-label mb-1 d-block">Formación</label>
              <div class="f-input-wrapper" :class="{ 'is-open': activeSelect === 'education' }" @click="toggleSelect('education')" v-click-outside="() => closeSelect('education')">
                <GraduationCap class="input-icon" />
                <div class="f-select-display">{{ getLabel(form.hasUniversityEducation, educationOptions) }}</div>
                <transition name="fade-glass">
                  <div v-if="activeSelect === 'education'" class="f-dropdown-glass">
                    <div v-for="opt in educationOptions" :key="opt.value" class="select-option" @click.stop="selectOpt('education', opt.value)">
                      {{ opt.label }}
                    </div>
                  </div>
                </transition>
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
import { User, Mail, Phone, Calendar, GraduationCap, CreditCard, Lock, Users, Check, RefreshCw } from 'lucide-vue-next'
import { employeeService } from '@/services/employeeService'
import { toast } from '@/services/toastService'
import CrystalInput from '@/components/common/CrystalInput.vue'

const route = useRoute()
const router = useRouter()
const isEdit = Boolean(route.params.id)
const isSaving = ref(false)
const showDatePicker = ref(false)
const activeSelect = ref(null)
const errors = reactive({
  firstName: '', lastName: '', secondLastName: '',
  nif: '', email: '', phone1: '', phone2: '', birthDate: ''
})

const form = reactive({
  idEmployee: null, nif: '', firstName: '', lastName: '', secondLastName: '',
  birthDate: null, phone1: '', phone2: '', email: '',
  hireDate: new Date().toISOString().split('T')[0],
  hasUniversityEducation: 'N', maritalStatus: 'S'
})

// 2. VALIDACIONES REACTIVAS
watch(() => form.firstName, () => { errors.firstName = '' })
watch(() => form.lastName, () => { errors.lastName = '' })
watch(() => form.nif, () => { errors.nif = '' })
watch(() => form.email, () => { errors.email = '' })
watch(() => form.phone1, () => { errors.phone1 = '' })
watch(() => form.phone2, () => { errors.phone2 = '' })
watch(() => form.birthDate, () => { errors.birthDate = '' })
watch(() => form.secondLastName, () => { errors.secondLastName = '' })

const maritalOptions = [{ label: 'Soltero / a', value: 'S' }, { label: 'Casado / a', value: 'C' }]
const educationOptions = [{ label: 'Título Universitario / Grado Superior', value: 'S' }, { label: 'No tiene título universitario', value: 'N' }]

/**
 * Valida que los datos del empleado sean correctos (NIF, email, teléfonos, etc).
 * @param {Boolean} silent - Si es true, valida pero no pinta los errores en el formulario.
 */
const validateForm = (silent = false) => {
  const localErrors = {
    firstName: !form.firstName ? 'El nombre es obligatorio' : form.firstName.length < 2 ? 'Mínimo 2 caracteres' : '',
    lastName: !form.lastName ? 'El apellido es obligatorio' : form.lastName.length < 2 ? 'Mínimo 2 caracteres' : '',
    secondLastName: !form.secondLastName ? 'El segundo apellido es obligatorio' : form.secondLastName.length < 2 ? 'Mínimo 2 caracteres' : '',
    nif: !form.nif ? 'El NIF es obligatorio' : !/^[0-9]{8}[A-Za-z]$/.test(form.nif) ? 'Formato: 8 números y una letra final' : '',
    email: !form.email ? 'El email es obligatorio' : !/.+@.+\..+/.test(form.email) ? 'Introduzca un email válido' : '',
    phone1: !form.phone1 ? 'El teléfono es obligatorio' : !/^[0-9]{9,12}$/.test(form.phone1) ? 'Debe tener entre 9 y 12 dígitos' : '',
    phone2: !form.phone2 ? 'El teléfono secundario es obligatorio' : !/^[0-9]{9,12}$/.test(form.phone2) ? 'Debe tener entre 9 y 12 dígitos' : '',
    birthDate: !form.birthDate ? 'La fecha de nacimiento es obligatoria' : new Date(form.birthDate) > new Date() ? 'La fecha no puede ser futura' : ''
  }
  
  if (!silent) {
    Object.assign(errors, localErrors)
  }
  
  return !Object.values(localErrors).some(v => v !== '')
}

const onDateSelected = () => {
  showDatePicker.value = false
  validateForm(true)
}

const formatDisplayDate = (date) => date ? new Date(date).toLocaleDateString('es-ES') : ''
const toggleSelect = (type) => activeSelect.value = activeSelect.value === type ? null : type
const closeSelect = (type) => { if (activeSelect.value === type) activeSelect.value = null }
const selectOpt = (type, val) => {
  if (type === 'marital') form.maritalStatus = val
  else form.hasUniversityEducation = val
  activeSelect.value = null
  validateForm(true)
}
const getLabel = (val, opts) => opts.find(o => o.value === val)?.label || ''

const userInitials = computed(() => {
  const initials = (form.firstName?.charAt(0) || '') + (form.lastName?.charAt(0) || '')
  return initials.toUpperCase() || 'E'
})

/**
 * Envía los datos del empleado al servidor para crear uno nuevo o actualizarlo.
 * También gestiona los errores que nos pueda devolver la API.
 */
const save = async () => {
  if (!validateForm()) return toast.error('Revise los campos marcados en rojo')
  
  isSaving.value = true
  // Limpiar errores previos antes de intentar guardar
  Object.keys(errors).forEach(key => errors[key] = '')

  try {
    if (isEdit) await employeeService.update(form.idEmployee, form)
    else await employeeService.create(form)
    toast.success('Empleado guardado correctamente')
    router.push({ name: 'employees' })
  } catch (e) {
    if (e.response?.status === 400 && e.response?.data?.errors) {
      // Mapeo automático de errores del Backend a los campos del Frontend
      const backendErrors = e.response.data.errors
      Object.keys(backendErrors).forEach(field => {
        if (field in errors) {
          errors[field] = backendErrors[field]
        }
      })
      toast.error('Existen errores en los datos enviados')
    } else {
      const status = e.response?.status
      if (!status || (status !== 401 && status !== 403 && status < 500)) {
        toast.error(e.response?.data?.message || 'Error al guardar el empleado')
      }
    }
  } finally { isSaving.value = false }
}

/**
 * Carga la información del empleado si estamos editando uno existente.
 */
onMounted(async () => {
  if (isEdit) {
    try {
      Object.assign(form, await employeeService.getById(route.params.id))
    } catch {
      toast.error('No se pudo cargar el empleado')
      router.push({ name: 'employees' })
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
.role-text { font-size: 10px; color: #64748B; font-weight: 500; text-transform: uppercase; letter-spacing: 0.05em; }
.settings-section { padding: 12px 0; }
.section-divider { height: 1px; background: #E2E8F0; margin: 12px 0; }
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
  font-weight: 400; /* Peso regular para legibilidad */
  font-size: 11px;
  margin-top: 4px;
}

.f-form-minimal :deep(.input-icon) {
  color: #94A3B8;
  width: 16px;
  margin-right: 8px;
}

.f-form-minimal :deep(input) {
  font-size: 14px;
  color: #1E293B;
}
</style>
