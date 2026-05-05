import api from '@/plugins/axios'

/**
 * Servicio de orquestación para la entidad Empleado.
 * Centraliza la comunicación con el backend real de Spring Boot.
 */
export const employeeService = {
  /**
   * Recupera el listado completo de empleados.
   * @param {Boolean} includeInactive - Si se deben incluir empleados con baja lógica.
   * @returns {Promise<Array>} Lista de empleados.
   */
  async getAll(includeInactive = true) {
    const response = await api.get('/employees', {
      params: { includeInactive, size: 1000 }
    })
    return response.data.content || response.data
  },

  async getActive() {
    const response = await api.get('/employees', { params: { includeInactive: false, size: 1000 } })
    return response.data.content || response.data
  },

  /**
   * Recupera un empleado por su identificador único.
   * @param {Number} id - Identificador del empleado.
   * @returns {Promise<Object>} Datos del empleado.
   */
  async getById(id) {
    const response = await api.get(`/employees/${id}`)
    return response.data
  },

  /**
   * Registra un nuevo empleado en el sistema.
   * @param {Object} employeeData - DTO del empleado.
   * @returns {Promise<Object>} Empleado creado.
   */
  async create(employeeData) {
    const response = await api.post('/employees', employeeData)
    return response.data
  },

  /**
   * Actualiza los datos de un empleado existente.
   * @param {Number} id - ID del empleado.
   * @param {Object} employeeData - Nuevos datos.
   * @returns {Promise<Object>} Empleado actualizado.
   */
  async update(id, employeeData) {
    const response = await api.put(`/employees/${id}`, employeeData)
    return response.data
  },

  /**
   * Elimina un empleado (borrado físico o lógico según el backend).
   * @param {Number} id - ID del empleado.
   * @returns {Promise<void>}
   */
  async delete(id) {
    await api.delete(`/employees/${id}`)
  }
}
