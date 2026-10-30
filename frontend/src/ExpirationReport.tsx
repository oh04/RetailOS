import { useState } from 'react'
import type { Product } from './types'
import { apiFetch } from './api'

function ExpirationReport() {
    const [through, setThrough] = useState('')
    const [reportedThrough, setReportedThrough] = useState('')
    const [products, setProducts] = useState<Product[]>([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')
    const [hasLoaded, setHasLoaded] = useState(false)

    async function loadReport() {
        setLoading(true)
        setError('')
        setProducts([])
        setHasLoaded(false)
        try {
            const response = await apiFetch(`/api/products/expiring?through=${through}`)
            if (!response.ok) {
                throw new Error('Request failed')
            }
            const data: Product[] = await response.json()
            setProducts(data)
            setReportedThrough(through)
            setHasLoaded(true)
        } catch {
            setError("Could not load the expiration report.")
        } finally {
            setLoading(false)
        }
    }

    return (
        <section>
            <h2>Expiration report</h2>
            <form className="product-form" onSubmit={(event) => {
                event.preventDefault()
                void loadReport()
            }}>
                <label>
                    Expiring on or before
                    <input type="date" value={through} onChange={(event) => setThrough(event.target.value)} required />
                </label>
                <button type="submit" disabled={loading}>
                    {loading ? 'Loading…' : 'Run report'}
                </button>
            </form>
            {error && <p role="alert">{error}</p>}
            {hasLoaded && <p>Products expiring on or before {reportedThrough}</p>}
            {hasLoaded && products.length === 0 && <p>No products expire by this date.</p>}
            <table>
                <thead>
                    <tr>
                        <th>Product</th>
                        <th>Quantity</th>
                        <th>Category</th>
                        <th>Expires</th>
                    </tr>
                </thead>
                <tbody>
                    {products.map((product) => (
                        <tr key={product.id}>
                            <td>{product.name}</td>
                            <td>{product.quantity}</td>
                            <td>{product.category}</td>
                            <td>{product.expirationDate}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </section>
    )
}

export default ExpirationReport