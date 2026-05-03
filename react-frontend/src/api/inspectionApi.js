import { post, patch } from './apiClient'

export function reviewProduct(productId, dto) {
  return post(`/api/inspection/product/${productId}`, dto)
}

export function verifyProduct(productId, dto) {
  return post(`/api/inspection/product/verify/${productId}`, dto)
}

export function setCanSell(customerId, dto) {
  return patch(`/api/customer/inspection/${customerId}`, dto)
}
