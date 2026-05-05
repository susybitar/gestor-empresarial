import { defineStore } from 'pinia';
import api from '@/plugins/axios';

/**
 * STORE DE AUTENTICACIÓN (Pinia)
 * Gestiona el estado de sesión del usuario en el Frontend de forma global.
 * Permite que cualquier componente sepa si hay un usuario logueado.
 */
export const useAuthStore = defineStore('auth', {
  state: () => ({
    // Recuperamos el usuario de localStorage por si el usuario recargó la página (F5)
    user: JSON.parse(localStorage.getItem('fsm_user')) || null,
    loading: false,
    error: null,
  }),
  
  getters: {
    // Un getter reactivo para saber si hay sesión activa
    isAuthenticated: (state) => !!state.user,
  },
  
  actions: {
    /**
     * Llama al API para validar credenciales y guarda el usuario si hay éxito.
     */
    async login(email, password) {
      this.loading = true;
      this.error = null;
      try {
        const response = await api.post('/auth/login', { email, password });
        
        // Guardamos solo los campos necesarios del AuthResponseDTO
        this.user = {
          idUser: response.data.idUser,
          email: response.data.email,
          role: response.data.role
        };
        localStorage.setItem('fsm_user', JSON.stringify(this.user));
        return true;
      } catch (err) {
        // Capturamos el 400 Bad Request que escupe el GlobalExceptionHandler de Spring
        this.error = err.response?.data?.message || 'Error al conectar con el servidor';
        return false;
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * Limpia la sesión.
     */
    logout() {
      this.user = null;
      localStorage.removeItem('fsm_user');
      localStorage.removeItem('fsm_user_name');
      localStorage.removeItem('fsm_user_avatar');
    }
  }
});