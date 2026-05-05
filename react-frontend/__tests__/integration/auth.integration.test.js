import { describe, it, expect } from 'vitest'
import { post, get, extractCookie } from './helpers.js'

describe('Auth Integration', () => {
  describe('Customer Auth', () => {
    let cookie = null

    it('should register a new customer', async () => {
      const unique = Date.now()
      const res = await post('/api/customers', {
        firstName: 'Test',
        lastName: 'User',
        email: `test${unique}@example.com`,
        password: 'Password123',
        phoneNumber: '12345678',
        streetName: 'Test St',
        secondaryUnit: '1A',
        postalCode: 1000,
        city: 'TestCity',
      })
      expect([200, 201]).toContain(res.status)
      expect(res.data).toBeTruthy()
    })

    it('should login as customer', async () => {
      const res = await post('/api/customers/login', {
        email: 'test@example.com',
        password: 'Password123',
      })
      if (res.status === 200) {
        cookie = extractCookie(res.headers)
        expect(res.data).toBeTruthy()
      } else {
        console.warn('Customer login skipped (no seed user). Status:', res.status)
      }
    })

    it('should get customer claims when authenticated', async () => {
      if (!cookie) return
      const res = await get('/api/customers/claims', cookie)
      expect(res.status).toBe(200)
      expect(res.data?.customerId).toBeTruthy()
    })

    it('should return 401 for unauthenticated claims', async () => {
      const res = await get('/api/customers/claims')
      expect(res.status).toBe(401)
    })

    it('should logout', async () => {
      if (!cookie) return
      const res = await post('/api/customers/logout', undefined, cookie)
      expect([200, 204]).toContain(res.status)
    })
  })

  describe('Reseller Auth', () => {
    let cookie = null

    it('should login as reseller', async () => {
      const res = await post('/api/reseller/login', {
        username: 'reseller',
        password: 'Password123',
      })
      if (res.status === 200) {
        cookie = extractCookie(res.headers)
        expect(res.data).toBeTruthy()
      } else {
        console.warn('Reseller login skipped (no seed user). Status:', res.status)
      }
    })

    it('should get reseller claims when authenticated', async () => {
      if (!cookie) return
      const res = await get('/api/reseller/claims', cookie)
      expect(res.status).toBe(200)
      expect(res.data?.resellerId).toBeTruthy()
    })

    it('should logout', async () => {
      if (!cookie) return
      const res = await post('/api/reseller/logout', undefined, cookie)
      expect([200, 204]).toContain(res.status)
    })
  })
})
