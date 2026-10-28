import './App.css'
import type {Product} from './types'
import {useState} from 'react'

function App() {
  const [name, setName] = useState('')
  const [quantity, setQuantity] = useState('0')
  const [creating, setCreating] = useState(false)
  const [createError, setCreateError] = useState('')
  const [hasLoaded, setHasLoaded] = useState(false)
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [category, setCategory] = useState('')
  const [expirationDate, setExpirationDate] = useState('')
  const [createSuccess, setCreateSuccess] = useState('')

  async function loadProducts() {
    setLoading(true)
    setError('')

    try {
      const response = await fetch('/api/products')

      if (!response.ok) {
        throw new Error('Request failed')
      }

      const data: Product[] = await response.json()
      setProducts(data)
      setHasLoaded(true)
    } catch {
      setError('Could not load inventory. Check that the backend is running.')
    } finally {
      setLoading(false)
    }
  }

  async function createProduct() {
    setCreating(true)
    setCreateError('')
    setCreateSuccess('')

    try {
      const response = await fetch('/api/products', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name: name.trim(),
          quantity: Number(quantity),
          category: category.trim() || null,
          expirationDate: expirationDate || null,
        }),
      })

      if (!response.ok) {
        throw new Error('Creation failed')
      }

      setName('')
      setQuantity('0')
      await loadProducts()
    } catch {
      setCreateError('Could not create the product. Check your inputs and retry.')
    } finally {
      setCreating(false)
    }
    setCategory('')
    setExpirationDate('')
    setCreateSuccess("Product created successfully.")
  }

  return (
    <main className="app">
      <header>
        <h1>RetailOS</h1>
        <p>Your store operations workspace.</p>
      </header>
      <section>
          <h2>Inventory</h2>
          <form
            className="product-form"
            onSubmit={(event) => {
              event.preventDefault()
              void createProduct()
            }}
          >
            <label>
              Product name
              <input
                value={name}
                onChange={(event) => setName(event.target.value)}
                required
              />
            </label>

            <label>
              Quantity
              <input
                type = "number"
                value = {quantity}
                onChange={(event) => setQuantity(event.target.value)}
                required
                min = "0"
                max = "2147483647"
                step = "1"
              />
            </label>
            <label>
              Category
              <input
                value = {category}
                onChange = {(event) => setCategory(event.target.value)}
                placeholder = "Uncategorized"
              />
            </label>
            <label>
              Expiration date
              <input
                type = "date"
                value = {expirationDate}
                onChange = {(event) => setExpirationDate(event.target.value)}
              />
            </label>
            <button type="submit" disabled={creating || loading}>
              {creating ? 'Creating…' : 'Create product'}
            </button>
          </form>
          {createError && <p role="alert">{createError}</p>}
          {createSuccess && <p role="status">{createSuccess}</p>}
          <button type="button" onClick={loadProducts} disabled={loading || creating}>
            {loading ? 'Loading…' : 'Load inventory'}
          </button>
          {error && <p role="alert">{error}</p>}
          {hasLoaded && !loading && !error && products.length === 0 && (
            <p>No products in inventory yet.</p>
          )}
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Product</th>
                <th>Quantity</th>
                <th>Category</th>
                <th>Expires</th>
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr key={product.id}>
                  <td>{product.id}</td>
                  <td>{product.name}</td>
                  <td>{product.quantity}</td>
                  <td>{product.category}</td>
                  <td>{product.expirationDate ?? '-'}</td>
                </tr>
              ))}
            </tbody>
          </table>
      </section>
    </main>
  )
}

export default App