import { get, post } from './apiClient'

export function customerLogin({ email, password }) {
  return post('/api/customers/login', { email, password })
}

export function customerLogout() {
  return post('/api/customers/logout')
}

export function getCustomerClaims() {
  return get('/api/customers/claims')
}

export function resellerLogin({ username, password }) {
  return post('/api/reseller/login', { username, password })
}

export function resellerLogout() {
  return post('/api/reseller/logout')
}

export function getResellerClaims() {
  return get('/api/reseller/claims')
}
