import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { getProduct } from '../api/productApi'
import { useAuth } from '../hooks/useAuth'
import { useCart } from '../hooks/useCart'
import LoadingSpinner from '../components/shared/LoadingSpinner'
import ErrorAlert from '../components/shared/ErrorAlert'
import '../styles/productDetails.css'

export default function ProductDetailsPage() {
  const { id } = useParams()
  const [product, setProduct] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [infoMessage, setInfoMessage] = useState(null)
  const { isCustomer, isReseller } = useAuth()
  const { addItem } = useCart()

  useEffect(() => {
    setLoading(true)
    setError(null)
    getProduct(id)
      .then(setProduct)
      .catch(e => setError(e.message))
      .finally(() => setLoading(false))
  }, [id])

  const handleAddToCart = () => {
    if (!product) return
    addItem({
      productId: product.id,
      name: product.name,
      price: product.price,
      imageUrl: product.images?.[0]?.url,
    })
    setInfoMessage(`'${product.name}' added to your cart.`)
  }

  if (loading) return <LoadingSpinner message="Loading product details..." />
  if (error) return <ErrorAlert message={error} />
  if (!product) return <p>Product not found.</p>

  return (
    <div className="product-details-container">
      <h4>{product.name} ({Number(product.price).toFixed(2)} DKK)</h4>
      <div className="product-details-info">
        <p><strong>Condition:</strong> {product.condition}</p>
        <p><strong>Category:</strong> {product.category}</p>
        <p><strong>Description:</strong> {product.description}</p>
        <p><strong>Registered:</strong> {new Date(product.registerDate).toLocaleString()}</p>

        {product.images?.length > 0 ? (
          <div className="product-details-images">
            {product.images.map((img, i) => (
              <img key={i} src={img.url} alt={product.name} className="product-image-large" />
            ))}
          </div>
        ) : (
          <p>No images available.</p>
        )}

        <p><strong>Seller:</strong> {product.sellerFirstName} {product.sellerLastName}</p>
        <p><strong>Seller Email:</strong> {product.sellerEmail}</p>
        <p><strong>Seller Phone:</strong> {product.sellerPhoneNumber}</p>
        <p><strong>Pawnshop:</strong> {product.pawnshopName}, {product.pawnshopStreetName} {product.pawnshopSecondaryUnit}, {product.pawnshopPostalCode} {product.pawnshopCity}</p>
        <p><strong>Status:</strong> {product.approvalStatus}</p>

        {isReseller && (
          <Link className="btn btn-primary" to={`/customer/${product.sellerId}/inspection`}>
            Inspect Customer
          </Link>
        )}

        {isCustomer && (
          <button className="btn btn-success" onClick={handleAddToCart} style={{ marginTop: 10 }}>
            Add to cart
          </button>
        )}

        {infoMessage && (
          <div className="alert alert-info" style={{ marginTop: 10 }}>{infoMessage}</div>
        )}
      </div>
    </div>
  )
}
