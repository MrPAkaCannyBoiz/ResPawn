import { useState } from 'react'
import { useAuth } from '../hooks/useAuth'
import '../styles/login.css'

export default function ResellerLoginPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [message, setMessage] = useState('')
  const { user, isReseller, loginReseller, logout } = useAuth()

  const handleLogin = async () => {
    setMessage('')
    if (!username.trim()) {
      setMessage('Please enter a username.')
      return
    }
    try {
      await loginReseller(username, password)
    } catch (e) {
      setMessage(e.message)
    }
  }

  const handleLogout = async () => {
    await logout()
  }

  return (
    <div className="login-shell">
      <div className="login-right single-login-card">
        <h2 className="auth-title">Reseller Login</h2>
        <p className="auth-subtitle">Access your reseller dashboard and manage your products</p>

        {!isReseller ? (
          <div className="form-section">
            <h3>Please login as reseller</h3>

            <label className="form-label">Username:</label>
            <input
              type="text"
              className="input-field"
              value={username}
              onChange={e => setUsername(e.target.value)}
            />
            {username !== '' && !username.trim() && (
              <span style={{ color: 'red' }}>Username is required.</span>
            )}

            <label className="form-label">Password:</label>
            <input
              type="password"
              className="input-field"
              value={password}
              onChange={e => setPassword(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && handleLogin()}
            />

            {message && <span style={{ color: 'red' }}>{message}</span>}

            <button className="login-btn-primary" onClick={handleLogin}>Login</button>

            <div className="feature-box">
              &#128272; Secure login protected by ResPawn reseller authentication
            </div>
          </div>
        ) : (
          <div className="success-box">
            <h3>Welcome back, {user.username}!</h3>
            <button className="logout-btn" onClick={handleLogout}>Logout</button>
          </div>
        )}
      </div>
    </div>
  )
}
