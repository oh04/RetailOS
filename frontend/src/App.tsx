import './App.css'
import type { Product } from './types'
import { useState } from 'react'
import TaskPanel from './TaskPanel'
import SummaryPanel from './SummaryPanel'
import StockHistoryPanel from './StockHistoryPanel'
import ExpirationReport from './ExpirationReport'
import OverdueReport from './OverdueReport'

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
  const [searchQuery, setSearchQuery] = useState('')
  const [adjustingId, setAdjustingId] = useState<number | null>(null)
  const [adjustError, setAdjustError] = useState('')
  const [adjustSuccess, setAdjustSuccess] = useState('')
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [deletingId, setDeletingId] = useState<number | null>(null)
  const [deleteError, setDeleteError] = useState('')
  const [deleteSuccess, setDeleteSuccess] = useState('')
  const [stockAmount, setStockAmount] = useState('1')

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

  async function saveProduct() {
    const isEditing = editingId !== null
    const url = isEditing ? `/api/products/${editingId}` : '/api/products'
    setCreating(true)
    setCreateError('')
    setCreateSuccess('')

    try {
      const response = await fetch(url, {
        method: isEditing ? 'PUT' : 'POST',
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

      resetProductForm()
      setCreateSuccess(isEditing ? 'Product updated successfully.' : 'Product created successfully.')
      await loadProducts()
    } catch {
      setCreateError('Could not save the product. Check your inputs and retry.')
    } finally {
      setCreating(false)
    }
  }

  async function adjustStock(id: number, change: number) {
    if (
      !Number.isInteger(change) ||
      change === 0 ||
      Math.abs(change) > 2147483647
    ) {
      setAdjustError('Enter a whole adjustment amount between 1 and 2147483647.')
      setAdjustSuccess('')
      return
    }
    setAdjustingId(id)
    setAdjustError('')
    setAdjustSuccess('')

    try {
      const response = await fetch(`/api/products/${id}/adjustments`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ change }),
      })

      if (!response.ok) {
        throw new Error('Adjustment failed')
      }

      setAdjustSuccess('Stock updated successfully.')
      await loadProducts()
    } catch {
      setAdjustError('Could not adjust stock. Reload inventory and check the backend.')
    } finally {
      setAdjustingId(null)
    }
  }

  async function deleteProduct(product: Product) {
    if (!window.confirm(`Delete "${product.name}"? This cannot be undone.`)) {
      return
    }

    setDeletingId(product.id)
    setDeleteError('')
    setDeleteSuccess('')

    try {
      const response = await fetch(`/api/products/${product.id}`, {
        method: 'DELETE',
      })

      if (!response.ok) {
        throw new Error('Deletion failed')
      }

      if (editingId === product.id) {
        resetProductForm()
      }
      if (selectedProduct !== null && selectedProduct.id === product.id) {
        setSelectedProduct(null)
      }

      setDeleteSuccess('Product deleted successfully.')
      await loadProducts()
    } catch {
      setDeleteError('Could not delete the product. Reload inventory and retry.')
    } finally {
      setDeletingId(null)
    }
  }

  function startEditing(product: Product) {
    setEditingId(product.id)
    setName(product.name)
    setQuantity(String(product.quantity))
    setCategory(product.category)
    setExpirationDate(product.expirationDate ?? '')
    setCreateError('')
    setCreateSuccess('')
  }

  function resetProductForm() {
    setEditingId(null)
    setName('')
    setCategory('')
    setExpirationDate('')
    setQuantity('0')
  }

  const visibleProducts = products.filter((product) => product.name.toLowerCase().includes(searchQuery.trim().toLowerCase()))
  const inventoryBusy = loading || creating || adjustingId !== null || deletingId !== null
  const adjustmentAmount = Number(stockAmount)
  const validStockAmount = Number.isInteger(adjustmentAmount) && adjustmentAmount >= 1 && adjustmentAmount <= 2147483647
  return (
    <main className="app">
      <header>
        <h1>RetailOS</h1>
        <p>Your store operations workspace.</p>
      </header>
      <SummaryPanel />
      <section>
        <h2>Inventory</h2>
        <form
          className="product-form"
          onSubmit={(event) => {
            event.preventDefault()
            void saveProduct()
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
              type="number"
              value={quantity}
              onChange={(event) => setQuantity(event.target.value)}
              required
              min="0"
              max="2147483647"
              step="1"
            />
          </label>
          <label>
            Category
            <input
              value={category}
              onChange={(event) => setCategory(event.target.value)}
              placeholder="Uncategorized"
            />
          </label>
          <label>
            Expiration date
            <input
              type="date"
              value={expirationDate}
              onChange={(event) => setExpirationDate(event.target.value)}
            />
          </label>
          <button type="submit" disabled={inventoryBusy}>
            {creating ? 'Saving…' : editingId !== null ? 'Save changes' : 'Create product'}
          </button>
          {editingId !== null && (
            <button type="button" onClick={resetProductForm} disabled={inventoryBusy}>
              Cancel edit
            </button>
          )}
        </form>
        {createError && <p role="alert">{createError}</p>}
        {createSuccess && <p role="status">{createSuccess}</p>}
        <button type="button" onClick={loadProducts} disabled={inventoryBusy}>
          {loading ? 'Loading…' : 'Load inventory'}
        </button>
        {error && <p role="alert">{error}</p>}
        {hasLoaded && !loading && !error && visibleProducts.length === 0 && (<p>{products.length === 0 ? 'No products in inventory yet.' : 'No products match your search.'}</p>)}
        {adjustError && <p role="alert">{adjustError}</p>}
        {adjustSuccess && <p role="status">{adjustSuccess}</p>}
        {deleteError && <p role="alert">{deleteError}</p>}
        {deleteSuccess && <p role="status">{deleteSuccess}</p>}
        <a className="download-link" href="/api/products/export">Download all inventory (CSV)</a>
        <label>
          Search products
          <input
            type="search"
            value={searchQuery}
            onChange={(event) => setSearchQuery(event.target.value)}
            placeholder="Search by product name"
          />
        </label>
        <label>
          Units to receive or sell
          <input type="number" value={stockAmount} onChange={(event) => setStockAmount(event.target.value)} min="1" max="2147483647" step="1" disabled={inventoryBusy} />
        </label>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Product</th>
              <th>Quantity</th>
              <th>Category</th>
              <th>Expires</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {visibleProducts.map((product) => (
              <tr key={product.id}>
                <td>{product.id}</td>
                <td>{product.name}</td>
                <td>{product.quantity}</td>
                <td>{product.category}</td>
                <td>{product.expirationDate ?? '-'}</td>
                <td>
                  <button type="button" onClick={() => void adjustStock(product.id, adjustmentAmount)} disabled={inventoryBusy || !validStockAmount}>
                    Receive
                  </button>
                  <button type="button" onClick={() => void adjustStock(product.id, -adjustmentAmount)} disabled={inventoryBusy || !validStockAmount || product.quantity < adjustmentAmount}>
                    Sell
                  </button>
                  <button type="button" onClick={() => setSelectedProduct(product)}>
                    History
                  </button>
                  <button type="button" onClick={() => startEditing(product)} disabled={inventoryBusy}>
                    Edit
                  </button>
                  <button type="button" onClick={() => void deleteProduct(product)} disabled={inventoryBusy}>
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
      {selectedProduct !== null && (
        <StockHistoryPanel key={selectedProduct.id} product={selectedProduct} onClose={() => setSelectedProduct(null)} />
      )}
      <ExpirationReport />
      <TaskPanel />
      <OverdueReport />
    </main>
  )
}

export default App