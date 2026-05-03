import { describe, it, expect } from 'vitest'
import { post, get, patch, extractCookie } from './helpers.js'

describe('Inspection Integration', () => {
  let resellerCookie = null

  it('should login as reseller', async () => {
    const res = await post('/api/auth/reseller/login', {
      username: 'reseller',
      password: 'Password123',
    })
    if (res.status === 200) {
      resellerCookie = extractCookie(res.headers)
      expect(resellerCookie).toBeTruthy()
    } else {
      console.warn('Inspection tests skipped: no seed reseller. Status:', res.status)
    }
  })

  it('should review a pending product', async () => {
    if (!resellerCookie) return

    const pendingRes = await get('/api/products/pending', resellerCookie)
    if (!pendingRes.data?.length) {
      console.warn('No pending products to review')
      return
    }

    const claimsRes = await get('/api/auth/reseller/claims', resellerCookie)
    const resellerId = claimsRes.data?.resellerId

    const addressRes = await get('/api/addresses/pawnshop', resellerCookie)
    const pawnshopId = addressRes.data?.[0]?.pawnshopId || 0

    const productId = pendingRes.data[0].id
    const reviewRes = await post(`/api/inspection/product/${productId}`, {
      resellerId,
      pawnshopId,
      comments: 'Integration test review',
      isAccepted: true,
    }, resellerCookie)

    expect([200, 201]).toContain(reviewRes.status)
    if (reviewRes.data) {
      expect(reviewRes.data).toHaveProperty('productId')
    }
  })

  it('should toggle canSell for a customer', async () => {
    if (!resellerCookie) return

    const productsRes = await get('/api/products', resellerCookie)
    if (!productsRes.data?.length) {
      console.warn('No products to find a customer to inspect')
      return
    }

    const productId = productsRes.data[0].id
    const detailRes = await get(`/api/products/${productId}`, resellerCookie)
    if (!detailRes.data?.sellerId) {
      console.warn('No sellerId on product')
      return
    }

    const customerId = detailRes.data.sellerId
    const res = await patch(`/api/customer/inspection/${customerId}`, {
      canSell: true,
    }, resellerCookie)

    expect([200, 204]).toContain(res.status)
  })
})
