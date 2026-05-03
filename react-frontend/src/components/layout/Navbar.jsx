import { useState } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'

export default function Navbar() {
  const [menuOpen, setMenuOpen] = useState(false)
  const { user, isAuthenticated, isCustomer, isReseller, canSell, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = async () => {
    await logout()
    navigate('/')
  }

  return (
    <>
      <nav className="top-navbar">
        <div className="nav-left">
          <span className="brand-title">ResPawnMarket</span>
        </div>

        <div className="nav-center desktop-menu">
          <NavLink to="/" className="nav-link">Home</NavLink>
          <NavLink to="/available-product" className="nav-link">Products</NavLink>
          <NavLink to="/register" className="nav-link">Sign up</NavLink>

          {isCustomer && (
            <>
              {canSell && <NavLink to="/customer/new-product" className="nav-link">Upload Item</NavLink>}
              <NavLink to="/purchase" className="nav-link">Cart</NavLink>
              <NavLink to="/customer/info" className="nav-link">Profile</NavLink>
              <NavLink to="/my-products" className="nav-link">My Products</NavLink>
            </>
          )}

          {isReseller && (
            <NavLink to="/resellercheck" className="nav-link">Check Quality</NavLink>
          )}
        </div>

        <div className="nav-right">
          {!isAuthenticated && (
            <>
              <NavLink to="/customer-login" className="nav-link">Customer Login</NavLink>
              <NavLink to="/reseller-login" className="nav-link">Reseller Login</NavLink>
            </>
          )}
          {isAuthenticated && (
            <button className="logout-btn" onClick={handleLogout}>Logout</button>
          )}
          <button className="hamburger" onClick={() => setMenuOpen(!menuOpen)}>&#9776;</button>
        </div>
      </nav>

      {menuOpen && (
        <div className="mobile-menu">
          <NavLink to="/" className="mobile-link" onClick={() => setMenuOpen(false)}>Home</NavLink>
          <NavLink to="/available-product" className="mobile-link" onClick={() => setMenuOpen(false)}>Products</NavLink>
          <NavLink to="/register" className="mobile-link" onClick={() => setMenuOpen(false)}>Sign up</NavLink>

          {isCustomer && (
            <>
              {canSell && <NavLink to="/customer/new-product" className="mobile-link" onClick={() => setMenuOpen(false)}>Upload Item</NavLink>}
              <NavLink to="/purchase" className="mobile-link" onClick={() => setMenuOpen(false)}>Cart</NavLink>
              <NavLink to="/customer/info" className="mobile-link" onClick={() => setMenuOpen(false)}>Profile</NavLink>
              <NavLink to="/my-products" className="mobile-link" onClick={() => setMenuOpen(false)}>My Products</NavLink>
            </>
          )}

          {isReseller && (
            <NavLink to="/resellercheck" className="mobile-link" onClick={() => setMenuOpen(false)}>Check Quality</NavLink>
          )}

          {!isAuthenticated && (
            <>
              <NavLink to="/customer-login" className="mobile-link" onClick={() => setMenuOpen(false)}>Customer Login</NavLink>
              <NavLink to="/reseller-login" className="mobile-link" onClick={() => setMenuOpen(false)}>Reseller Login</NavLink>
            </>
          )}

          {isAuthenticated && (
            <button className="mobile-logout" onClick={handleLogout}>Logout</button>
          )}
        </div>
      )}
    </>
  )
}
