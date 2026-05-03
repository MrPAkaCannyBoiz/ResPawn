import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { getCustomer, updateCustomer } from '../api/customerApi'
import LoadingSpinner from '../components/shared/LoadingSpinner'
import '../styles/customer.css'

export default function UpdateProfilePage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [loaded, setLoaded] = useState(false)
  const [loadError, setLoadError] = useState(null)
  const [form, setForm] = useState({
    firstName: '', lastName: '', email: '', phoneNumber: '',
    streetName: '', secondaryUnit: '', city: '', postalCode: 0,
  })
  const [showConfirm, setShowConfirm] = useState(false)
  const [message, setMessage] = useState('')

  useEffect(() => {
    if (!user?.customerId) return
    getCustomer(user.customerId)
      .then(c => {
        setForm({
          firstName: c.firstName, lastName: c.lastName,
          email: c.email, phoneNumber: c.phoneNumber,
          streetName: c.streetName, secondaryUnit: c.secondaryUnit,
          city: c.city, postalCode: c.postalCode,
        })
        setLoaded(true)
      })
      .catch(e => setLoadError(e.message))
  }, [user?.customerId])

  const update = (field) => (e) => setForm(prev => ({ ...prev, [field]: e.target.value }))

  const handleUpdate = async () => {
    try {
      await updateCustomer(user.customerId, {
        ...form,
        postalCode: Number(form.postalCode) || 0,
      })
      setMessage('Customer updated successfully.')
    } catch (e) {
      setMessage(`Error updating customer: ${e.message}`)
    }
    setShowConfirm(false)
  }

  if (!loaded && !loadError) return <LoadingSpinner />
  if (loadError) return <p>{loadError}</p>

  return (
    <div className="update-container">
      <div className="update-card">
        <h2>Update Customer</h2>

        <div className="form-grid">
          <label>First Name:</label>
          <input className="input" value={form.firstName} onChange={update('firstName')} />

          <label>Last Name:</label>
          <input className="input" value={form.lastName} onChange={update('lastName')} />

          <label>Email:</label>
          <input className="input" value={form.email} onChange={update('email')} />

          <label>Phone Number:</label>
          <input className="input" value={form.phoneNumber} onChange={update('phoneNumber')} />

          <label>Street Name:</label>
          <input className="input" value={form.streetName} onChange={update('streetName')} />

          <label>Secondary Unit:</label>
          <input className="input" value={form.secondaryUnit} onChange={update('secondaryUnit')} />

          <label>City:</label>
          <input className="input" value={form.city} onChange={update('city')} />

          <label>Postal Code:</label>
          <input className="input" value={form.postalCode} onChange={update('postalCode')} />
        </div>

        {showConfirm && (
          <div className="confirm-box">
            <p>Are you sure you want to update?</p>
            <div className="confirm-actions">
              <button className="checkout-btn" onClick={handleUpdate}>Yes</button>
              <button className="remove-btn" onClick={() => setShowConfirm(false)}>No</button>
            </div>
          </div>
        )}

        {message && <p style={{ marginTop: 12, textAlign: 'center' }}>{message}</p>}

        <div className="form-actions">
          <button className="update-btn primary" type="button" onClick={() => setShowConfirm(true)}>
            Update Details
          </button>
          <button className="back-btn secondary" type="button" onClick={() => navigate('/customer/info')}>
            Back to Customer Details
          </button>
        </div>
      </div>
    </div>
  )
}
