import { useState, useEffect } from 'react'
import { useAuth } from '../hooks/useAuth'
import { getCustomerInspectionProducts } from '../api/productApi'
import LoadingSpinner from '../components/shared/LoadingSpinner'
import ErrorAlert from '../components/shared/ErrorAlert'
import '../styles/product.css'

const statusColor = (status) => {
  if (!status) return 'pending'
  switch (status.toLowerCase()) {
    case 'approved': return 'approved'
    case 'rejected': return 'rejected'
    case 'reviewing': return 'reviewing'
    default: return 'pending'
  }
}

export default function MyProductsPage() {
  const { user } = useAuth()
  const [rows, setRows] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (!user?.customerId) return
    setLoading(true)
    getCustomerInspectionProducts(user.customerId)
      .then(data => {
        const sorted = [...data].sort((a, b) =>
          new Date(b.productWithFirstImage?.registerDate) - new Date(a.productWithFirstImage?.registerDate)
        )
        setRows(sorted)
      })
      .catch(e => setError(e.message))
      .finally(() => setLoading(false))
  }, [user?.customerId])

  if (loading) return <LoadingSpinner message="Fetching your products..." />
  if (error) return <ErrorAlert message={error} />

  return (
    <div className="products-page">
      <div className="products-card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
          <h3 className="products-title" style={{ marginBottom: 0 }}>My Uploaded Products</h3>
          <span style={{ background: '#f3f4f6', padding: '4px 12px', borderRadius: 8, color: '#333', border: '1px solid #e5e7eb' }}>
            {rows?.length ?? 0} Items
          </span>
        </div>

        {!rows || rows.length === 0 ? (
          <div style={{ textAlign: 'center', padding: 40 }}>
            <h5>No products yet</h5>
            <p className="no-products-text">You haven&apos;t uploaded any items for sale yet.</p>
          </div>
        ) : (
          <table className="products-table">
            <thead>
              <tr>
                <th style={{ width: '10%' }}>ID</th>
                <th style={{ width: '30%' }}>Product Name</th>
                <th style={{ width: '15%' }}>Status</th>
                <th style={{ width: '45%' }}>Latest Comment</th>
              </tr>
            </thead>
            <tbody>
              {rows.map(row => {
                const p = row.productWithFirstImage
                return (
                  <tr key={p?.id}>
                    <td style={{ color: '#6b7280' }}>#{p?.id}</td>
                    <td>
                      <strong style={{ color: '#111' }}>{p?.name}</strong><br />
                      <small style={{ color: '#6b7280', fontSize: '0.75rem' }}>
                        {p?.registerDate && new Date(p.registerDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
                      </small>
                    </td>
                    <td>
                      <span className={`status-tag ${statusColor(p?.approvalStatus)}`}>
                        {p?.approvalStatus}
                      </span>
                    </td>
                    <td>
                      {row.inspectionComment ? (
                        <span style={{ color: '#6b7280', maxWidth: 300, display: 'inline-block', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }} title={row.inspectionComment}>
                          {row.inspectionComment}
                        </span>
                      ) : (
                        <span style={{ color: '#9ca3af', fontStyle: 'italic', fontSize: 13 }}>- No comments -</span>
                      )}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}
