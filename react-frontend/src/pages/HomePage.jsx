import { Link } from 'react-router-dom'
import '../styles/home.css'

export default function HomePage() {
  return (
    <div className="home-background">
      <div className="hero-container">
        <div className="hero-content">
          <h1>Welcome to <span>ReSpawnMarket</span></h1>
          <p>Buy & sell second-hand items safely and easily.</p>

          <p className="explore-text">
            Want to take a look first? Explore our available products!
          </p>

          <div className="home-buttons">
            <div className="main-explore">
              <Link to="/available-product" className="btn-tertiary hero-main-btn">
                Explore Products
              </Link>
            </div>

            <div className="sub-buttons">
              <Link to="/register" className="home-btn-primary hero-sub-btn">
                Create Account
              </Link>
              <Link to="/customer-login" className="home-btn-secondary hero-sub-btn">
                Login
              </Link>
            </div>
          </div>
        </div>

        <img
          className="hero-image"
          src="https://cdn-icons-png.flaticon.com/512/5046/5046845.png"
          alt="ReSpawnMarket"
        />
      </div>
    </div>
  )
}
