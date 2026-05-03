import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { getCustomer } from '../api/customerApi'
import LoadingSpinner from '../components/shared/LoadingSpinner'
import ErrorAlert from '../components/shared/ErrorAlert'
import '../styles/customer.css'

export default function CustomerProfilePage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [customer, setCustomer] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (!user?.customerId) return
    setLoading(true)
    getCustomer(user.customerId)
      .then(setCustomer)
      .catch(e => setError(e.message))
      .finally(() => setLoading(false))
  }, [user?.customerId])

  if (loading) return <LoadingSpinner />
  if (error) return <ErrorAlert message={error} />
  if (!customer) return <div className="alert alert-warning">Not found.</div>

  return (
    <div className="profile-page">
      <div className="profile-card">
        <h2 className="profile-title">Customer Profile</h2>

        <div className="profile-grid">
          <div className="label">Id</div>
          <div className="value">{customer.id}</div>

          <div className="label">FirstName</div>
          <div className="value">{customer.firstName}</div>

          <div className="label">LastName</div>
          <div className="value">{customer.lastName}</div>

          <div className="label">Email</div>
          <div className="value">{customer.email}</div>

          <div className="label">Phone</div>
          <div className="value">{customer.phoneNumber}</div>

          <div className="label">Address</div>
          <div className="value">
            {customer.streetName}, {customer.secondaryUnit}<br />
            {customer.postalCode} {customer.city}
          </div>
        </div>

        <button className="update-btn" onClick={() => navigate('/customer/info/update')}>
          Go To Update Customer
        </button>
      </div>
    </div>
  )
}
