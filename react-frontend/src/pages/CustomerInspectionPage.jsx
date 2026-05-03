import { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'
import { getCustomer } from '../api/customerApi'
import { setCanSell } from '../api/inspectionApi'
import LoadingSpinner from '../components/shared/LoadingSpinner'
import '../styles/customer.css'

export default function CustomerInspectionPage() {
  const { id } = useParams()
  const [customer, setCustomer] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [message, setMessage] = useState('')
  const [canSellState, setCanSellState] = useState(false)

  useEffect(() => {
    setLoading(true)
    setError(null)
    getCustomer(id)
      .then(c => {
        setCustomer(c)
        setCanSellState(c.canSell ?? false)
      })
      .catch(e => setError(e.message))
      .finally(() => setLoading(false))
  }, [id])

  const applyCanSell = async () => {
    if (!customer) {
      setMessage('No customer loaded.')
      return
    }
    try {
      await setCanSell(customer.id, { canSell: canSellState })
      setCustomer(prev => ({ ...prev, canSell: canSellState }))
      setMessage(canSellState
        ? `Customer ${customer.id} marked as allowed to sell.`
        : `Customer ${customer.id} marked as not allowed to sell.`)
    } catch (e) {
      setMessage(`Error: ${e.message}`)
    }
  }

  if (loading) return <LoadingSpinner />
  if (error) return <div className="customer-status customer-status-error">{error}</div>
  if (!customer) return <div className="customer-status customer-status-warn">Customer not found.</div>

  return (
    <div>
      <h3>Customer Inspection</h3>

      {message && <div className="customer-status customer-status-info">{message}</div>}

      <div className="customer-card">
        <div className="customer-header">
          <div className="customer-title">Customer #{customer.id}</div>
          <div className="customer-sub">{customer.firstName} {customer.lastName}</div>
        </div>

        <div className="customer-grid">
          {[
            ['Email', customer.email],
            ['Phone', customer.phoneNumber],
            ['Street', customer.streetName],
            ['Unit', customer.secondaryUnit],
            ['Postal Code', customer.postalCode],
            ['City', customer.city],
          ].map(([key, val]) => (
            <div className="customer-row" key={key}>
              <div className="customer-cell customer-cell-key">{key}</div>
              <div className="customer-cell customer-cell-val">{val}</div>
            </div>
          ))}

          <div className="customer-row">
            <div className="customer-cell customer-cell-key">Current Can Sell</div>
            <div className="customer-cell customer-cell-val">
              {customer.canSell ? (
                <span className="customer-badge customer-badge-ok">Yes</span>
              ) : (
                <span className="customer-badge customer-badge-no">No</span>
              )}
            </div>
          </div>

          <div className="customer-row">
            <div className="customer-cell customer-cell-key">Desired Selling State</div>
            <div className="customer-cell customer-cell-val">
              <label>
                <input
                  type="checkbox"
                  checked={canSellState}
                  onChange={e => setCanSellState(e.target.checked)}
                />
                {' '}Check to allow selling
              </label>
              <button className="customer-btn customer-btn-primary" style={{ marginLeft: 12 }} onClick={applyCanSell}>
                Apply
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
