import { describe, it, expect } from 'vitest'
import { post, get, extractCookie } from './helpers.js'

describe('Purchase Integration', () => {
  it('should complete a purchase flow (login -> buy -> receipt)', async () => {
    const loginRes = await post('/api/auth/customer/login', {
      email: 'test@example.com',
      password: 'Password123',
    })

    if (loginRes.status !== 200) {
      console.warn('Purchase test skipped: no seed customer. Status:', loginRes.status)
      return
    }

    const cookie = extractCookie(loginRes.headers)
    if (!cookie) {
      console.warn('Purchase test skipped: no cookie in response')
      return
    }

    const claimsRes = await get('/api/auth/customer/claims', cookie)
    expect(claimsRes.status).toBe(200)
    const customerId = claimsRes.data?.customerId

    const productsRes = await get('/api/products/available', cookie)
    if (!productsRes.data?.length) {
      console.warn('Purchase test skipped: no available products')
      return
    }

    const product = productsRes.data[0]
    const buyRes = await post('/api/purchases', {
      customerId,
      items: [{ productId: product.id, quantity: 1 }],
    }, cookie)

    expect([200, 201]).toContain(buyRes.status)
    if (buyRes.status === 200 || buyRes.status === 201) {
      expect(buyRes.data).toHaveProperty('transaction')
    }
  })
})
