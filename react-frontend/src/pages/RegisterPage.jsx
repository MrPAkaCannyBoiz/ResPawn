import { useState } from 'react'
import { registerCustomer } from '../api/customerApi'
import '../styles/register.css'
import '../styles/login.css'

const INITIAL_FORM = {
  firstName: '', lastName: '', email: '', password: '',
  phoneNumber: '', streetName: '', city: '', postalCode: '',
  secondaryUnit: '',
}

export default function RegisterPage() {
  const [form, setForm] = useState(INITIAL_FORM)
  const [confirmPassword, setConfirmPassword] = useState('')
  const [message, setMessage] = useState(null)
  const [success, setSuccess] = useState(false)
  const [busy, setBusy] = useState(false)

  const update = (field) => (e) => setForm(prev => ({ ...prev, [field]: e.target.value }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setBusy(true)
    setMessage(null)
    setSuccess(false)

    if (form.password !== confirmPassword) {
      setSuccess(false)
      setMessage('Passwords do not match.')
      setBusy(false)
      return
    }

    try {
      const created = await registerCustomer({
        ...form,
        postalCode: Number(form.postalCode) || 0,
      })
      setSuccess(true)
      setMessage(`Customer '${created.firstName} ${created.lastName}' created successfully!`)
      setForm(INITIAL_FORM)
      setConfirmPassword('')
    } catch (err) {
      setSuccess(false)
      setMessage(`Error: ${err.message}`)
    } finally {
      setBusy(false)
    }
  }

  const handleReset = () => {
    setForm(INITIAL_FORM)
    setConfirmPassword('')
    setMessage(null)
  }

  return (
    <div className="register-shell">
      <div className="register-card-2col">
        <div className="register-left">
          <div className="brand-badge">ReSpawnMarket</div>
          <h2 className="left-title">Buy safer. Sell smarter.</h2>
          <p className="left-subtitle">
            Join a trusted second-hand marketplace where safety, transparency
            and convenience come first.
          </p>
          <ul className="left-feature-list">
            <li>&#10004; Verified customer accounts</li>
            <li>&#10004; Secure transactions</li>
            <li>&#10004; Easy access from any device</li>
          </ul>
          <div className="bubble small-b1"></div>
          <div className="bubble small-b2"></div>
        </div>

        <div className="register-right">
          <form onSubmit={handleSubmit}>
            <h2 className="auth-title">Create your ReSpawnMarket account</h2>
            <p className="auth-subtitle">Start buying & selling safely today.</p>

            <div className="register-form-grid">
              <div>
                <label className="form-label">First name</label>
                <input className="input-field" value={form.firstName} onChange={update('firstName')} required />
              </div>
              <div>
                <label className="form-label">Last name</label>
                <input className="input-field" value={form.lastName} onChange={update('lastName')} required />
              </div>
              <div>
                <label className="form-label">Email</label>
                <input className="input-field" type="email" value={form.email} onChange={update('email')} required />
              </div>
              <div>
                <label className="form-label">Phone number</label>
                <input className="input-field" value={form.phoneNumber} onChange={update('phoneNumber')} required />
              </div>
              <div>
                <label className="form-label">Password</label>
                <input className="input-field" type="password" value={form.password} onChange={update('password')} required />
              </div>
              <div>
                <label className="form-label">Repeat password</label>
                <input className="input-field" type="password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} required />
              </div>
              <div>
                <label className="form-label">Street</label>
                <input className="input-field" value={form.streetName} onChange={update('streetName')} required />
              </div>
              <div>
                <label className="form-label">Secondary unit</label>
                <input className="input-field" value={form.secondaryUnit} onChange={update('secondaryUnit')} />
              </div>
              <div>
                <label className="form-label">Postal code</label>
                <input className="input-field" type="number" value={form.postalCode} onChange={update('postalCode')} required />
              </div>
              <div>
                <label className="form-label">City</label>
                <input className="input-field" value={form.city} onChange={update('city')} required />
              </div>
            </div>

            <div style={{ marginTop: 25, textAlign: 'center' }}>
              <button className="register-btn-primary" type="submit" disabled={busy}>
                {busy ? 'Creating...' : 'Create'}
              </button>
              <button className="reset-btn" type="button" onClick={handleReset} disabled={busy}>
                Reset
              </button>
            </div>
          </form>
        </div>
      </div>

      {message && (
        <div className={`alert ${success ? 'alert-success' : 'alert-danger'}`} style={{ marginTop: 20, maxWidth: 600, margin: '20px auto' }}>
          {message}
        </div>
      )}
    </div>
  )
}
