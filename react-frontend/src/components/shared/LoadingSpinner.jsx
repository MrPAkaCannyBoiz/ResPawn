export default function LoadingSpinner({ message = 'Loading...' }) {
  return (
    <div style={{ textAlign: 'center', padding: '40px' }}>
      <div className="spinner-border text-primary" role="status">
        <span className="visually-hidden">Loading...</span>
      </div>
      <p style={{ marginTop: '10px', color: '#6b7280' }}>{message}</p>
    </div>
  )
}
