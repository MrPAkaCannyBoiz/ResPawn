import { useState, useEffect } from 'react'
import { getAvailableProducts } from '../api/productApi'
import { useAuth } from '../hooks/useAuth'
import { useCart } from '../hooks/useCart'
import ProductCard from '../components/shared/ProductCard'
import LoadingSpinner from '../components/shared/LoadingSpinner'
import ErrorAlert from '../components/shared/ErrorAlert'
import '../styles/product.css'

export default function AvailableProductsPage() {
  const [products, setProducts] = useState(null)
  const [error, setError] = useState(null)
  const [cartMessage, setCartMessage] = useState('')
  const { isCustomer } = useAuth()
  const { items, addItem } = useCart()

  useEffect(() => {
    getAvailableProducts()
      .then(setProducts)
      .catch(e => setError(e.message))
  }, [])

  const handleAddToCart = (product) => {
    if (items.some(i => i.productId === product.id)) {
      setCartMessage(`'${product.name}' is already in the cart.`)
      return
    }
    addItem({
      productId: product.id,
      name: product.name,
      price: product.price,
      imageUrl: product.image?.url,
    })
    setCartMessage(`'${product.name}' added to cart.`)
  }

  if (!products && !error) return <LoadingSpinner message="Loading products..." />
  if (error) return <ErrorAlert message={error} />

  return (
    <div>
      <h3>Buy Products here</h3>
      {cartMessage && <p>{cartMessage}</p>}
      <div className="product-list">
        {products.map(product => (
          <ProductCard
            key={product.id}
            product={product}
            onAddToCart={isCustomer ? handleAddToCart : undefined}
          />
        ))}
      </div>
    </div>
  )
}
