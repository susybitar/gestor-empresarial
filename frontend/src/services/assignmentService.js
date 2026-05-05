import api from '@/plugins/axios'

export const assignmentService = {
  async getByProject(projectId) {
    const res = await api.get(`/projects/${projectId}/employees`)
    return res.data.content || res.data
  },

  async assign(projectId, employeeId) {
    await api.put(`/projects/${projectId}/employees/${employeeId}`)
  },

  async unassign(projectId, employeeId) {
    await api.delete(`/projects/${projectId}/employees/${employeeId}`)
  }
}
