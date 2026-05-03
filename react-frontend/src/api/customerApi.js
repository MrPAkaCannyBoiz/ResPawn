import { get, post, patch } from './apiClient'

export function registerCustomer(dto) {
  return post('/api/customers', dto)
}

export function getCustomer(id) {
  return get(`/api/customers/${id}`)
}

export function updateCustomer(id, dto) {
  return patch(`/api/customers/${id}`, dto)
}
