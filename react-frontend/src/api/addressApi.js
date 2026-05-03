import { get } from './apiClient'

export function getPawnshopAddresses() {
  return get('/api/addresses/pawnshop')
}
