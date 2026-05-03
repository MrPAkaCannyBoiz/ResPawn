import { useState } from 'react'
import { useAuth } from '../hooks/useAuth'
import { uploadProduct } from '../api/productApi'
import '../styles/product.css'

const CATEGORIES = [
  'CATEGORY_UNSPECIFIED', 'ELECTRONICS', 'CLOTHING', 'FURNITURE',
  'SPORTS', 'BOOKS', 'TOYS', 'VEHICLES', 'OTHER',
]

const isValidPhotoUrl = (url) => {
  if (!url || url.trim() === '' || url.length > 1024) return false
  const lower = url.toLowerCase()
  const hasExt = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'].some(e => lower.endsWith(e))
  return (lower.startsWith('http://') || lower.startsWith('https://')) && hasExt
}

export default function UploadProductPage() {
  const { user } = useAuth()
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [condition, setCondition] = useState('')
  const [price, setPrice] = useState(0)
  const [urls, setUrls] = useState(['', '', '', '', ''])
  const [category, setCategory] = useState('CATEGORY_UNSPECIFIED')
  const [otherCategory, setOtherCategory] = useState('')
  const [message, setMessage] = useState(null)
  const [busy, setBusy] = useState(false)

  const updateUrl = (i) => (e) => {
    const copy = [...urls]
    copy[i] = e.target.value
    setUrls(copy)
  }

  const handleUpload = async () => {
    setMessage(null)
    setBusy(true)

    if (!name.trim() || !description.trim() || !condition.trim() || price < 0 || !isValidPhotoUrl(urls[0])) {
      setMessage('Please correct the input before uploading. Some fields are missing or invalid.')
      setBusy(false)
      return
    }
    if (category === 'OTHER' && !otherCategory.trim()) {
      setMessage('Other Category cannot be empty.')
      setBusy(false)
      return
    }

    try {
      await uploadProduct(user.customerId, {
        name, description, condition, price, category, otherCategory,
        imageUrls: urls.filter(isValidPhotoUrl),
      })
      setMessage('Item uploaded successfully! (Admin will soon check it)')
      setName(''); setDescription(''); setCondition(''); setPrice(0)
      setUrls(['', '', '', '', '']); setCategory('CATEGORY_UNSPECIFIED'); setOtherCategory('')
    } catch (e) {
      setMessage(`Error uploading item: ${e.message}`)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="upload-page">
      <div className="upload-card">
        <h3 className="upload-title">Upload your new Item</h3>

        <div style={{ marginBottom: 12 }}>
          <label className="form-label">Item Name</label>
          <input className="form-control" value={name} onChange={e => setName(e.target.value)} />
          {!name.trim() && <div className="text-danger">Name cannot be empty.</div>}

          <label className="form-label">Description</label>
          <input className="form-control" value={description} onChange={e => setDescription(e.target.value)} />
          {!description.trim() && <div className="text-danger">Description cannot be empty.</div>}

          <label className="form-label">Condition</label>
          <input className="form-control" value={condition} onChange={e => setCondition(e.target.value)} />
          {!condition.trim() && <div className="text-danger">Condition cannot be empty.</div>}

          <label className="form-label">Price offer</label>
          <input className="form-control" type="number" value={price} onChange={e => setPrice(Number(e.target.value))} />
          {price < 0 && <div className="text-danger">Price must be non-negative.</div>}

          <label className="form-label">Photo URLs (up to 5)</label>
          {urls.map((url, i) => (
            <div key={i} style={{ marginBottom: 8 }}>
              <input className="form-control" value={url} onChange={updateUrl(i)} placeholder={i === 0 ? 'Required' : 'Optional'} />
              {i === 0 && !isValidPhotoUrl(url) && <div className="text-danger">Photo URL must be filled / URL is invalid</div>}
              {i > 0 && url.trim() && !isValidPhotoUrl(url) && <div className="text-danger">Photo URL is not valid.</div>}
              {isValidPhotoUrl(url) && (
                <div style={{ marginTop: 4 }}>
                  <strong>Preview:</strong>
                  <div style={{ padding: 8, border: '1px solid #ddd', borderRadius: 8, background: '#f8f8f8', maxWidth: 600, marginTop: 4 }}>
                    <img src={url} alt="Preview" style={{ maxHeight: 300, objectFit: 'contain', maxWidth: '100%' }} />
                  </div>
                </div>
              )}
            </div>
          ))}

          <label className="form-label">Category</label>
          <select className="form-select" value={category} onChange={e => setCategory(e.target.value)}>
            {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
          </select>

          {category === 'OTHER' && (
            <>
              <label className="form-label" style={{ marginTop: 8 }}>Category Name</label>
              <input className="form-control" value={otherCategory} onChange={e => setOtherCategory(e.target.value)} />
              {!otherCategory.trim() && <div className="text-danger">Other Category cannot be empty.</div>}
            </>
          )}
        </div>

        <button className="upload-btn" disabled={busy} onClick={handleUpload}>
          {busy ? 'uploading...' : 'upload'}
        </button>

        {message && <div style={{ marginTop: 12 }}>{message}</div>}
      </div>
    </div>
  )
}
