import { useState } from 'react'
import { useAuth } from '../hooks/useAuth'
import '../styles/login.css'

export default function CustomerLoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState('')
  const { user, isAuthenticated, loginCustomer, logout } = useAuth()

  const isValidEmail = (e) => /^[^@\s]+@[^@\s]+\.[^@\s]+$/i.test(e)

  const handleLogin = async () => {
    setMessage('')
    if (!isValidEmail(email)) {
      setMessage('Please enter a valid email address.')
      return
    }
    try {
      await loginCustomer(email, password)
    } catch (e) {
      setMessage(e.message)
    }
  }

  const handleLogout = async () => {
    try {
      await logout()
      setEmail('')
      setPassword('')
      setMessage('You have been logged out')
    } catch (e) {
      setMessage(e.message)
    }
  }

  return (
    <div className="login-shell">
      <div className="login-card-2col">
        <div className="login-left">
          <div className="brand-badge">ReSpawnMarket</div>
          <h2 className="left-title">Buy & sell second-hand smarter.</h2>
          <p className="left-subtitle">
            Join a trusted community marketplace where every deal is safer and easier to manage.
          </p>
          <ul className="left-feature-list">
            <li>&#10004; Secure customer accounts</li>
            <li>&#10004; Activity overview in one place</li>
            <li>&#10004; Easy access from any device</li>
          </ul>
        </div>

        <div className="login-right">
          <h2 className="auth-title">Sign in to your account</h2>
          <p className="auth-subtitle">Access your dashboard and manage your activity.</p>

          {!isAuthenticated ? (
            <div className="form-section">
              <h3>Customer Login</h3>
              <label className="form-label">Email:</label>
              <input
                type="text"
                className="input-field"
                value={email}
                onChange={e => setEmail(e.target.value)}
              />
              {email && !isValidEmail(email) && (
                <div className="error-box">Invalid email format.</div>
              )}

              <label className="form-label">Password:</label>
              <input
                type="password"
                className="input-field"
                value={password}
                onChange={e => setPassword(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && handleLogin()}
              />

              {message && <div className="error-box">{message}</div>}

              <button className="login-btn-primary" onClick={handleLogin}>Login</button>

              <div className="feature-box">
                &#128274; Secure login protected by ReSpawnMarket authentication
              </div>
            </div>
          ) : (
            <div className="success-box">
              <h3>Welcome back, {user.firstName} {user.lastName}</h3>
              <button className="logout-btn" onClick={handleLogout}>Logout</button>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
