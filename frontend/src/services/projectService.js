import api from '@/plugins/axios'

/**
 * Servicio de orquestación para la entidad Proyecto.
 * Centraliza la comunicación con el backend real de Spring Boot.
 */
export const projectService = {
  /**
   * Recupera el listado completo de proyectos.
   * @param {Boolean} includeInactive - Si se deben incluir proyectos con baja lógica.
   * @returns {Promise<Array>} Lista de proyectos.
   */
  async getAll(includeInactive = true) {
    const response = await api.get('/projects', {
      params: { includeInactive, size: 1000 }
    })
    return response.data.content || response.data
  },

  /**
   * Recupera un proyecto por su identificador único.
   * @param {Number} id - Identificador del proyecto.
   * @returns {Promise<Object>} Datos del proyecto.
   */
  async getById(id) {
    const response = await api.get(`/projects/${id}`)
    return response.data
  },

  /**
   * Registra un nuevo proyecto en la cartera estratégica.
   * @param {Object} projectData - DTO del proyecto.
   * @returns {Promise<Object>} Proyecto creado.
   */
  async create(projectData) {
    const response = await api.post('/projects', projectData)
    return response.data
  },

  /**
   * Actualiza los datos de un proyecto existente.
   * @param {Number} id - ID del proyecto.
   * @param {Object} projectData - Nuevos datos.
   * @returns {Promise<Object>} Proyecto actualizado.
   */
  async update(id, projectData) {
    const response = await api.put(`/projects/${id}`, projectData)
    return response.data
  },

  /**
   * Ejecuta la baja lógica del proyecto.
   * @param {Number} id - ID del proyecto.
   * @returns {Promise<void>}
   */
  async delete(id) {
    await api.delete(`/projects/${id}`)
  }
}
