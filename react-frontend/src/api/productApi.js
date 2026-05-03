import { get, post } from './apiClient'

export function getAllProducts() {
  return get('/api/products')
}

export function getProduct(id) {
  return get(`/api/products/${id}`)
}

export function getAvailableProducts() {
  return get('/api/products/available')
}

export function getPendingProducts() {
  return get('/api/products/pending')
}

export function getReviewingProducts() {
  return get('/api/products/reviewing')
}

export function getCustomerInspectionProducts(customerId) {
  return get(`/api/products/inspection/customer/${customerId}`)
}

export function uploadProduct(customerId, dto) {
  return post(`/api/products/customers/${customerId}`, dto)
}
