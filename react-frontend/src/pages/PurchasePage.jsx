import { useState } from 'react'
import { useAuth } from '../hooks/useAuth'
import { useCart } from '../hooks/useCart'
import { buyProducts } from '../api/purchaseApi'
import '../styles/product.css'

export default function PurchasePage() {
  const { user } = useAuth()
  const { items, removeItem, setQuantity, clear } = useCart()
  const [creditCard, setCreditCard] = useState({ cardNumber: '', cardholderName: '', expiration: '', cvv: '' })
  const [result, setResult] = useState(null)
  const [message, setMessage] = useState(null)
  const [busy, setBusy] = useState(false)

  const updateCard = (field) => (e) => setCreditCard(prev => ({ ...prev, [field]: e.target.value }))

  const validateCard = () => {
    const digits = creditCard.cardNumber.replace(/\s/g, '')
    if (!digits || digits.length !== 16 || !/^\d+$/.test(digits))
      return 'Card number must be 16 digits.'
    if (!creditCard.cardholderName.trim())
      return 'Cardholder name is required.'
    if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(creditCard.expiration))
      return 'Expiration must be in MM/YY format.'
    if (!/^\d{3,4}$/.test(creditCard.cvv))
      return 'CVV must be 3 or 4 digits.'
    return null
  }

  const handleBuy = async () => {
    setBusy(true)
    setMessage(null)
    setResult(null)

    if (items.length === 0) {
      setMessage('Your cart is empty.')
      setBusy(false)
      return
    }

    const validationError = validateCard()
    if (validationError) {
      setMessage(validationError)
      setBusy(false)
      return
    }

    try {
      const res = await buyProducts({
        customerId: user.customerId,
        items: items.map(i => ({ productId: i.productId, quantity: i.quantity })),
      })
      setResult(res)
      clear()
      setCreditCard({ cardNumber: '', cardholderName: '', expiration: '', cvv: '' })
    } catch (e) {
      setMessage(`Error: ${e.message}`)
    } finally {
      setBusy(false)
    }
  }

  if (result) {
    return (
      <div className="cart-wrapper">
        <div className="cart-card">
          <div style={{ background: '#22c55e', color: 'white', textAlign: 'center', padding: 16, borderRadius: 8, marginBottom: 20 }}>
            <h4>Payment Successful!</h4>
          </div>
          <div style={{ textAlign: 'center', marginBottom: 20 }}>
            <h5>Thank you for your purchase</h5>
            <p style={{ color: '#6b7280', fontSize: 14 }}>
              Transaction ID: <strong>{result.transaction?.id}</strong><br />
              Date: {result.transaction?.date && new Date(result.transaction.date).toLocaleString()}
            </p>
          </div>
          <hr />
          <h5 style={{ marginBottom: 12 }}>Order Summary</h5>
          <table className="cart-table">
            <thead>
              <tr>
                <th>Product</th>
                <th style={{ textAlign: 'center' }}>Qty</th>
                <th style={{ textAlign: 'right' }}>Unit Price</th>
                <th style={{ textAlign: 'right' }}>Total</th>
              </tr>
            </thead>
            <tbody>
              {result.cartProducts?.map(ci => {
                const prod = result.purchasedProduct?.find(p => p.id === ci.productId)
                if (!prod) return null
                return (
                  <tr key={ci.productId}>
                    <td><strong>{prod.name}</strong><br /><small style={{ color: '#6b7280' }}>{prod.category}</small></td>
                    <td style={{ textAlign: 'center' }}>{ci.quantity}</td>
                    <td style={{ textAlign: 'right' }}>{prod.price} DKK</td>
                    <td style={{ textAlign: 'right' }}>{prod.price * ci.quantity} DKK</td>
                  </tr>
                )
              })}
            </tbody>
            <tfoot>
              <tr style={{ fontWeight: 'bold', fontSize: 18 }}>
                <td colSpan={3} style={{ textAlign: 'right' }}>Total Paid:</td>
                <td style={{ textAlign: 'right', color: '#22c55e' }}>{result.shoppingCart?.totalPrice} DKK</td>
              </tr>
            </tfoot>
          </table>
          <div style={{ textAlign: 'center', marginTop: 24 }}>
            <button className="checkout-btn" onClick={() => setResult(null)}>Continue Shopping</button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="cart-wrapper">
      <div className="cart-card">
        <h2>Shopping Cart</h2>

        <div style={{ display: 'grid', gridTemplateColumns: '1.4fr 1fr', gap: 24 }}>
          <div>
            {items.length === 0 ? (
              <p className="empty-msg">Your cart is empty.</p>
            ) : (
              <table className="cart-table">
                <thead>
                  <tr>
                    <th>Product</th>
                    <th style={{ width: 120 }}>Price</th>
                    <th style={{ width: 120 }}>Quantity</th>
                    <th style={{ width: 140 }}>Subtotal</th>
                    <th style={{ width: 80 }}></th>
                  </tr>
                </thead>
                <tbody>
                  {items.map(item => (
                    <tr key={item.productId}>
                      <td>{item.name}</td>
                      <td>{item.price} DKK</td>
                      <td>
                        <input
                          type="number"
                          min="1"
                          value={item.quantity}
                          onChange={e => setQuantity(item.productId, Number(e.target.value))}
                          style={{ width: 60, padding: 4, borderRadius: 4, border: '1px solid #ddd' }}
                        />
                      </td>
                      <td>{item.price * item.quantity} DKK</td>
                      <td>
                        <button className="remove-btn" onClick={() => removeItem(item.productId)}>X</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          <div>
            <h4 style={{ color: '#1e1b4b', marginBottom: 16 }}>Credit Card Information</h4>

            <div style={{ marginBottom: 8 }}>
              <label style={{ fontWeight: 600, color: '#374151' }}>Card Number</label>
              <input className="input-field" placeholder="0000 0000 0000 0000" value={creditCard.cardNumber} onChange={updateCard('cardNumber')} />
            </div>

            <div style={{ marginBottom: 8 }}>
              <label style={{ fontWeight: 600, color: '#374151' }}>Cardholder Name</label>
              <input className="input-field" value={creditCard.cardholderName} onChange={updateCard('cardholderName')} />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 8 }}>
              <div>
                <label style={{ fontWeight: 600, color: '#374151' }}>Expiration (MM/YY)</label>
                <input className="input-field" placeholder="MM/YY" value={creditCard.expiration} onChange={updateCard('expiration')} />
              </div>
              <div>
                <label style={{ fontWeight: 600, color: '#374151' }}>CVV</label>
                <input className="input-field" value={creditCard.cvv} onChange={updateCard('cvv')} />
              </div>
            </div>

            <div style={{ display: 'flex', gap: 10, marginTop: 16 }}>
              <button className="checkout-btn" onClick={handleBuy} disabled={busy}>
                {busy ? 'Processing...' : 'Pay Now'}
              </button>
              <button className="remove-btn" onClick={clear} disabled={busy}>Clear cart</button>
            </div>

            {message && <div className="error-box" style={{ marginTop: 12 }}>{message}</div>}
          </div>
        </div>
      </div>
    </div>
  )
}
