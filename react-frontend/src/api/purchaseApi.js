import { post } from './apiClient'

export function buyProducts({ customerId, items }) {
  return post('/api/purchases', { customerId, items })
}
