import { Navigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'

export default function ProtectedRoute({ allowedRoles, requireCanSell, children }) {
  const { user, loading, isAuthenticated } = useAuth()

  if (loading) {
    return <p>Loading...</p>
  }

  if (!isAuthenticated) {
    return <Navigate to="/customer-login" replace />
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/" replace />
  }

  if (requireCanSell && !user.canSell) {
    return (
      <div style={{ padding: 40, textAlign: 'center' }}>
        <h3>Access Denied</h3>
        <p>You do not have permission to sell products. Contact a reseller to enable selling.</p>
      </div>
    )
  }

  return children
}
