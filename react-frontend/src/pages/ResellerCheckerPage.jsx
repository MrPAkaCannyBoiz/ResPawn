import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getPendingProducts, getReviewingProducts } from '../api/productApi'
import LoadingSpinner from '../components/shared/LoadingSpinner'
import ErrorAlert from '../components/shared/ErrorAlert'
import '../styles/reseller.css'

export default function ResellerCheckerPage() {
  const navigate = useNavigate()
  const [products, setProducts] = useState(null)
  const [filter, setFilter] = useState('pending')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const fetchProducts = async (status) => {
    setLoading(true)
    setError(null)
    try {
      const data = status === 'reviewing'
        ? await getReviewingProducts()
        : await getPendingProducts()
      const sorted = [...data].sort((a, b) => new Date(a.registerDate) - new Date(b.registerDate))
      setProducts(sorted)
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchProducts(filter)
  }, [filter])

  if (loading) return <LoadingSpinner message="Loading pending products..." />
  if (error) return <ErrorAlert message={error} />

  return (
    <div>
      <h3>Pending Products</h3>

      <div className="filter-row">
        <select value={filter} onChange={e => setFilter(e.target.value)}>
          <option value="pending">Pending</option>
          <option value="reviewing">Reviewing</option>
        </select>
      </div>

      {!products || products.length === 0 ? (
        <p>No pending/reviewing products awaiting for inspection</p>
      ) : (
        <div className="reseller-grid">
          {products.map(p => (
            <div key={p.id} className="reseller-product-card">
              <div className="product-image-container">
                {p.image?.url ? (
                  <img src={p.image.url} alt={p.name} />
                ) : (
                  <span>No Image</span>
                )}
              </div>
              <div className="product-card-body">
                <h5 className="product-card-title">{p.name}</h5>
                <p className="product-card-price">{Number(p.price).toFixed(2)} DKK</p>
                <div className="product-card-meta">
                  <span><strong>Condition:</strong> {p.condition}</span><br />
                  <span><strong>Status:</strong> {p.approvalStatus}</span>
                </div>
                <button className="inspect-btn" onClick={() => navigate(`/reseller/product/${p.id}`)}>
                  Inspect Product
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
