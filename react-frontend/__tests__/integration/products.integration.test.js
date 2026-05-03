import { describe, it, expect } from 'vitest'
import { get } from './helpers.js'

describe('Products Integration', () => {
  it('should fetch available products', async () => {
    const res = await get('/api/products/available')
    expect(res.status).toBe(200)
    expect(Array.isArray(res.data)).toBe(true)
  })

  it('should fetch all products', async () => {
    const res = await get('/api/products')
    expect(res.status).toBe(200)
    expect(Array.isArray(res.data)).toBe(true)
  })

  it('should fetch a single product by ID if any exist', async () => {
    const listRes = await get('/api/products')
    if (listRes.status !== 200 || !listRes.data?.length) {
      console.warn('No products to test single fetch')
      return
    }
    const productId = listRes.data[0].id
    const res = await get(`/api/products/${productId}`)
    expect(res.status).toBe(200)
    expect(res.data).toHaveProperty('name')
    expect(res.data).toHaveProperty('price')
  })

  it('should fetch pending products', async () => {
    const res = await get('/api/products/pending')
    expect(res.status).toBe(200)
    expect(Array.isArray(res.data)).toBe(true)
  })

  it('should fetch reviewing products', async () => {
    const res = await get('/api/products/reviewing')
    expect(res.status).toBe(200)
    expect(Array.isArray(res.data)).toBe(true)
  })
})
