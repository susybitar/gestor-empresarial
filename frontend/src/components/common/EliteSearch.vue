<template>
  <div class="elite-search-wrapper" :class="{ 'is-focused': isFocused, 'has-content': modelValue }">
    <Search class="search-icon" />
    <input
      :value="modelValue"
      @input="$emit('update:modelValue', $event.target.value)"
      @focus="isFocused = true"
      @blur="isFocused = false"
      type="text"
      :placeholder="placeholder"
      class="elite-search-input"
    />
    <button 
      v-if="modelValue" 
      class="clear-btn" 
      @click="$emit('update:modelValue', '')"
    >
      <X class="icon-tiny" />
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Search, X } from 'lucide-vue-next'

defineProps({
  modelValue: String,
  placeholder: { type: String, default: 'Buscar...' }
})

defineEmits(['update:modelValue'])

const isFocused = ref(false)
</script>

<style scoped>
.elite-search-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0px; /* Ultra fino */
  background: transparent;
  border-bottom: 1px solid transparent; 
  width: 180px; /* Más estrecho */
  transition: all 0.4s cubic-bezier(0.2, 0.8, 0.2, 1);
  position: relative;
}

/* Efecto Camaleón en Foco */
.elite-search-wrapper.is-focused {
  width: 240px; /* Crecimiento contenido */
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.02);
  backdrop-filter: blur(4px);
  border-radius: 12px; /* Más suave, alineado con el resto de la app */
  border: 1px solid rgba(30, 64, 175, 0.1);
}

.search-icon {
  width: 12px;
  height: 12px;
  color: #94A3B8;
  transition: all 0.3s;
}

.elite-search-wrapper.is-focused .search-icon {
  color: #1E40AF;
}

.elite-search-input {
  width: 100%;
  background: none;
  border: none;
  outline: none;
  font-size: 12px;
  font-weight: 300;
  color: #475569;
  letter-spacing: 0.01em;
}

.elite-search-input::placeholder {
  color: #94A3B8;
  font-weight: 300;
  letter-spacing: 0.02em;
}

/* Botón X Minimalista SIN CÍRCULO */
.clear-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  background: none; /* Sin círculo */
  border: none;
  padding: 0;
  cursor: pointer;
  color: #94A3B8;
  transition: 0.2s;
}

.clear-btn:hover {
  color: #0F172A;
  transform: scale(1.1);
}

.icon-tiny {
  width: 12px;
  height: 12px;
}
</style>
