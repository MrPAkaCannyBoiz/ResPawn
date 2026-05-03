import { createContext, useState, useEffect, useCallback } from 'react'
import {
  customerLogin as apiCustomerLogin,
  customerLogout as apiCustomerLogout,
  getCustomerClaims,
  resellerLogin as apiResellerLogin,
  resellerLogout as apiResellerLogout,
  getResellerClaims,
} from '../api/authApi'

export const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  const fetchAuth = useCallback(async () => {
    setLoading(true)
    try {
      const [customerResult, resellerResult] = await Promise.allSettled([
        getCustomerClaims(),
        getResellerClaims(),
      ])

      if (customerResult.status === 'fulfilled' && customerResult.value?.customerId) {
        const c = customerResult.value
        setUser({
          customerId: c.customerId,
          firstName: c.firstName,
          lastName: c.lastName,
          email: c.email,
          phoneNumber: c.phoneNumber,
          canSell: c.canSell === true || c.canSell === 'true',
          role: 'Customer',
        })
        return
      }

      if (resellerResult.status === 'fulfilled' && resellerResult.value?.resellerId) {
        const r = resellerResult.value
        setUser({
          resellerId: r.resellerId,
          username: r.username,
          role: 'Reseller',
        })
        return
      }

      setUser(null)
    } catch {
      setUser(null)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchAuth()
  }, [fetchAuth])

  const loginCustomer = async (email, password) => {
    await apiCustomerLogin({ email, password })
    const claims = await getCustomerClaims()
    setUser({
      customerId: claims.customerId,
      firstName: claims.firstName,
      lastName: claims.lastName,
      email: claims.email,
      phoneNumber: claims.phoneNumber,
      canSell: claims.canSell === true || claims.canSell === 'true',
      role: 'Customer',
    })
  }

  const loginReseller = async (username, password) => {
    await apiResellerLogin({ username, password })
    const claims = await getResellerClaims()
    setUser({
      resellerId: claims.resellerId,
      username: claims.username,
      role: 'Reseller',
    })
  }

  const logout = async () => {
    try { await apiCustomerLogout() } catch { /* ignore */ }
    try { await apiResellerLogout() } catch { /* ignore */ }
    setUser(null)
  }

  const value = {
    user,
    loading,
    loginCustomer,
    loginReseller,
    logout,
    refreshAuth: fetchAuth,
    isAuthenticated: user !== null,
    isCustomer: user?.role === 'Customer',
    isReseller: user?.role === 'Reseller',
    canSell: user?.canSell === true,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
