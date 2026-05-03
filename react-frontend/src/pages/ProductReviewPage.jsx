import { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getProduct } from '../api/productApi'
import { getPawnshopAddresses } from '../api/addressApi'
import { reviewProduct, verifyProduct } from '../api/inspectionApi'
import { useAuth } from '../hooks/useAuth'
import LoadingSpinner from '../components/shared/LoadingSpinner'
import ErrorAlert from '../components/shared/ErrorAlert'
import '../styles/reseller.css'

export default function ProductReviewPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [product, setProduct] = useState(null)
  const [addresses, setAddresses] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [successMessage, setSuccessMessage] = useState(null)
  const [busy, setBusy] = useState(false)
  const [cooldown, setCooldown] = useState(false)
  const cooldownTimer = useRef(null)

  const [reviewForm, setReviewForm] = useState({ pawnshopId: 0, comments: '', isAccepted: false })
  const [verifyForm, setVerifyForm] = useState({ comments: '', isAccepted: false })

  const isReviewing = product?.approvalStatus?.toLowerCase() === 'reviewing'

  useEffect(() => {
    setLoading(true)
    setError(null)
    Promise.all([getProduct(id), getPawnshopAddresses()])
      .then(([prod, addrs]) => {
        setProduct(prod)
        setAddresses(addrs)
        const pawnshopId = addrs.find(a => a.pawnshopId === prod.pawnshopId)?.pawnshopId || 0
        setReviewForm(f => ({ ...f, pawnshopId, resellerId: user.resellerId }))
        setVerifyForm(f => ({ ...f, resellerId: user.resellerId }))
      })
      .catch(e => setError(e.message))
      .finally(() => setLoading(false))

    return () => clearTimeout(cooldownTimer.current)
  }, [id, user.resellerId])

  const handleSubmit = async () => {
    setBusy(true)
    setError(null)
    setSuccessMessage(null)

    try {
      if (isReviewing) {
        const result = await verifyProduct(id, {
          resellerId: user.resellerId,
          comments: verifyForm.comments,
          isAccepted: verifyForm.isAccepted,
        })
        setSuccessMessage(`Product Verified. ProductId=${result.productId}, New status=${result.approvalStatus}`)
      } else {
        if (reviewForm.pawnshopId === 0 && reviewForm.isAccepted) {
          throw new Error('Please select the address')
        }
        const result = await reviewProduct(id, {
          resellerId: user.resellerId,
          pawnshopId: reviewForm.pawnshopId,
          comments: reviewForm.comments,
          isAccepted: reviewForm.isAccepted,
        })
        setSuccessMessage(`Review saved. ProductId=${result.productId}, New status=${result.approvalStatus}, PawnshopId=${result.pawnshopId}`)
      }

      setCooldown(true)
      cooldownTimer.current = setTimeout(() => setCooldown(false), 5000)
    } catch (e) {
      setError(e.message)
    } finally {
      setBusy(false)
    }
  }

  if (loading) return <LoadingSpinner message="Loading product..." />

  const givenAddress = addresses.find(a => a.pawnshopId === product?.pawnshopId) || {}

  return (
    <div className="review-page">
      <div className="review-card">
        <h2>Review Product</h2>

        {error && <ErrorAlert message={error} />}

        {product && (
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: 20, marginBottom: 20 }}>
            <div>
              {product.images?.map((img, i) => (
                <img key={i} src={img.url} alt={product.name} style={{ width: '100%', borderRadius: 8, marginBottom: 8 }} />
              ))}
            </div>
            <div>
              <h5>{product.name} ({Number(product.price).toFixed(2)} DKK)</h5>
              <p><strong>Condition:</strong> {product.condition}</p>
              <p><strong>Category:</strong> {product.category}</p>
              <p><strong>Description:</strong> {product.description}</p>
              <p><strong>Current status:</strong> {product.approvalStatus}</p>
              <h6>Seller</h6>
              <p>{product.sellerFirstName} {product.sellerLastName}<br />{product.sellerEmail}<br />{product.sellerPhoneNumber}</p>
              <h6>Pawnshop</h6>
              <p>{product.pawnshopName}<br />{product.pawnshopStreetName} {product.pawnshopSecondaryUnit}<br />{product.pawnshopPostalCode} {product.pawnshopCity}</p>
            </div>
          </div>
        )}

        {successMessage && <div className="alert alert-success">{successMessage}</div>}

        <div className="review-form">
          <div className="review-checkbox">
            <input
              type="checkbox"
              checked={isReviewing ? verifyForm.isAccepted : reviewForm.isAccepted}
              onChange={e => {
                if (isReviewing) setVerifyForm(f => ({ ...f, isAccepted: e.target.checked }))
                else setReviewForm(f => ({ ...f, isAccepted: e.target.checked }))
              }}
            />
            <label>
              {isReviewing
                ? 'Accept this product for selling'
                : 'Confirm this product for reviewing in the pawnshop'}
            </label>
          </div>

          {!isReviewing ? (
            <div>
              <label>Pawnshop address to send the product</label>
              <select
                value={reviewForm.pawnshopId}
                onChange={e => setReviewForm(f => ({ ...f, pawnshopId: Number(e.target.value) }))}
              >
                <option value={0}>Select pawnshop address</option>
                {addresses.map(a => (
                  <option key={a.pawnshopId} value={a.pawnshopId}>
                    {a.streetName} {a.secondaryUnit}, {a.postalCode} {a.city}
                  </option>
                ))}
              </select>
            </div>
          ) : (
            <p>
              Pawnshop address: {givenAddress.streetName} {givenAddress.secondaryUnit}, {givenAddress.postalCode} {givenAddress.city}
            </p>
          )}

          <label>Comments</label>
          <textarea
            value={isReviewing ? verifyForm.comments : reviewForm.comments}
            onChange={e => {
              if (isReviewing) setVerifyForm(f => ({ ...f, comments: e.target.value }))
              else setReviewForm(f => ({ ...f, comments: e.target.value }))
            }}
          />

          <div style={{ display: 'flex', gap: 10, marginTop: 16 }}>
            <button className="review-submit" disabled={busy || cooldown} onClick={handleSubmit}>
              {busy
                ? (isReviewing ? 'Verifying...' : 'Submitting...')
                : (isReviewing ? 'Verify Product' : 'Submit Review')}
            </button>
            <button
              className="inspect-btn"
              style={{ background: '#6b7280' }}
              onClick={() => navigate('/resellercheck')}
            >
              Back to list
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
