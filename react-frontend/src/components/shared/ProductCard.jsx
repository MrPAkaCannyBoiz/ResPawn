import { Link } from 'react-router-dom'

export default function ProductCard({ product, onAddToCart }) {
  return (
    <div className="product-row">
      <Link className="product-card" to={`/products/${product.id}`}>
        {product.image ? (
          <img className="product-image" src={product.image.url} alt={product.name} />
        ) : (
          <span className="product-image">No Image</span>
        )}
        <div className="product-details">
          <div><strong>{product.name}</strong></div>
          <div>{product.description}</div>
        </div>
        <div className="product-price">{product.price} DKK</div>
      </Link>
      {onAddToCart && (
        <div className="add-to-cart" onClick={() => onAddToCart(product)}>Add to cart</div>
      )}
    </div>
  )
}
