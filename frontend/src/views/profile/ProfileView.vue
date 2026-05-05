<template>
  <div class="f-page-view animate-in">
    <header class="f-header">
      <div class="f-header-content">
        <h1 class="text-title">Mi Perfil</h1>
      </div>
      <div class="f-header-actions">
        <button 
          class="btn-f-base btn-f-text"
          @click="router.push({ name: 'dashboard' })"
        >
          Cancelar
        </button>
        <button
          class="btn-f-base btn-f-text"
          @click="handleSave"
          :disabled="isSaving"
        >
          <template v-if="isSaving">
            <RefreshCw class="icon-xs mr-2 spin" />
            Guardando...
          </template>
          <template v-else>
            <Check class="icon-xs mr-2" />
            Guardar cambios
          </template>
        </button>
      </div>
    </header>

    <div class="profile-grid">
      <aside class="profile-aside">
        <div class="identity-block-minimal">
          <div class="avatar-container">
            <div class="avatar-main">
              <img v-if="avatarUrl" :src="avatarUrl" alt="Avatar" />
              <span v-else class="initials">{{ userInitials }}</span>
            </div>
            <div class="avatar-buttons">
              <v-btn icon variant="text" size="small" color="primary" @click="triggerFileInput">
                <Camera class="icon-xs" />
              </v-btn>
              <v-btn v-if="avatarUrl" icon variant="text" size="small" color="error" @click="removePhoto">
                <Trash2 class="icon-xs" />
              </v-btn>
            </div>
          </div>
          <div class="identity-text text-center">
            <span class="text-label text-uppercase">Configuración de Perfil</span>
          </div>
        </div>
      </aside>

      <main class="profile-main f-card-naked animate-in delay-1 f-form-minimal">
        <div class="settings-section">
          <div class="section-header">
            <h3 class="text-primary font-weight-bold mb-4">Información de Cuenta</h3>
          </div>
          <div class="f-form-grid">
            <CrystalInput 
              v-model="fullNameDisplay" 
              label="Nombre Completo" 
              :icon="User" 
              :error="errors.fullName"
              @update:modelValue="validateForm"
            />
            <CrystalInput :modelValue="fullEmployeeData.email" label="Email Corporativo" :icon="Lock" disabled readonly />
          </div>
        </div>
      </main>
    </div>
    <input type="file" ref="fileInput" style="display: none" accept="image/*" @change="handleFileChange" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/plugins/axios'
import { Camera, Trash2, Check, RefreshCw, User, Lock } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import { toast } from '@/services/toastService'
import { employeeService } from '@/services/employeeService'
import CrystalInput from '@/components/common/CrystalInput.vue'

const router = useRouter()
const authStore = useAuthStore()
const isSaving = ref(false)
const avatarUrl = ref(localStorage.getItem('fsm_user_avatar') || '')
const fileInput = ref(null)
const errors = reactive({ fullName: '' })

const fullEmployeeData = ref({ firstName: '', lastName: '', email: '' })

const fullNameDisplay = computed({
  get: () => `${fullEmployeeData.value.firstName} ${fullEmployeeData.value.lastName}`.trim(),
  set: (val) => {
    const parts = val.trim().split(' ')
    fullEmployeeData.value.firstName = parts[0] || ''
    fullEmployeeData.value.lastName = parts.slice(1).join(' ') || ''
  }
})

const userInitials = computed(() => (fullEmployeeData.value.firstName?.charAt(0) || '') + (fullEmployeeData.value.lastName?.charAt(0) || ''))

const validateForm = () => {
  errors.fullName = !fullNameDisplay.value ? 'El nombre completo es obligatorio' : 
                    fullNameDisplay.value.length < 5 ? 'Introduzca nombre y apellidos' : ''
  return !errors.fullName
}

const triggerFileInput = () => fileInput.value.click()
const handleFileChange = (e) => {
  const file = e.target.files[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (event) => { avatarUrl.value = event.target.result }
    reader.readAsDataURL(file)
  }
}
const removePhoto = () => avatarUrl.value = ''

const handleSave = async () => {
  if (!fullEmployeeData.value.idEmployee) return
  if (!validateForm()) return toast.error('Corrija los errores en el formulario')
  
  isSaving.value = true
  try {
    const dataToSave = { ...fullEmployeeData.value, imageUrl: avatarUrl.value || '' }
    await api.put(`/employees/${fullEmployeeData.value.idEmployee}`, dataToSave)
    localStorage.setItem('fsm_user_name', fullNameDisplay.value)
    if (avatarUrl.value) localStorage.setItem('fsm_user_avatar', avatarUrl.value)
    else localStorage.removeItem('fsm_user_avatar')
    window.dispatchEvent(new CustomEvent('profile-updated'))
    toast.success('Perfil actualizado correctamente')
    router.push({ name: 'dashboard' })
  } catch (error) {
    const status = error.response?.status
    if (status === 400 && error.response?.data?.errors) {
      errors.fullName = 'Error en los datos enviados al servidor'
    }
    if (!status || (status !== 401 && status !== 403 && status < 500)) {
      toast.error('Error al guardar los cambios')
    }
  } finally { isSaving.value = false }
}

onMounted(async () => {
  try {
    const employees = await employeeService.getAll(false)
    const me = employees.find(e => e.email === authStore.user?.email)
    if (!me) {
      toast.error('No se encontró tu perfil')
      return
    }
    fullEmployeeData.value = { ...me }
    if (!avatarUrl.value && me.imageUrl) avatarUrl.value = me.imageUrl
  } catch (e) { toast.error('Error al cargar perfil') }
})
</script>

<style scoped>
.profile-header { display: none; }
.identity-block-minimal { align-items: center; }
.avatar-container { position: relative; margin-bottom: 20px; }
.avatar-main { 
  width: 80px; 
  height: 80px; 
  border-radius: 50%; 
  background: #F8FAFC; 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  overflow: hidden; 
  border: 1px solid #E2E8F0;
}
.avatar-main img { width: 100%; height: 100%; object-fit: cover; }
.initials { font-family: 'Outfit'; font-size: 22px; color: #1E293B; font-weight: 700; }
.avatar-buttons { display: flex; gap: 8px; margin-top: 12px; justify-content: center; }
.settings-section { padding: 8px 0; }
.text-center { text-align: center; }

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
