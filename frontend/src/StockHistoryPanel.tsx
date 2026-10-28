import { useState } from 'react'
import type { Product, StockAdjustment } from './types'

type Props = {
  product: Product
  onClose: () => void
}

function StockHistoryPanel({ product, onClose }: Props) {
    const [entries, setEntries] = useState<StockAdjustment[]>([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')
    const [hasLoaded, setHasLoaded] = useState(false)
    const [page, setPage] = useState(0)
    const pageSize = 5

    async function loadHistory(requestedPage: number){
  
        try {
            setLoading(true)
            setError('')
            const response = await fetch(`/api/products/${product.id}/adjustments?limit=${pageSize}&page=${requestedPage}`)
            if (!response.ok) {
                throw new Error('Request failed')
            }
            const data: StockAdjustment[] = await response.json()
            setEntries(data)
            setPage(requestedPage)
            setHasLoaded(true)
        } catch {
            setError('Could not load stock history')
        } finally {
            setLoading(false)
        }

    }

    return (
        <section>
            <h2>Stock history: {product.name}</h2>
            <button type="button" onClick={onClose}>Close history</button>
            <button type="button" onClick={() => void loadHistory(0)} disabled={loading}>Load history</button>
            {error && <p role="alert">{error}</p>}
            {hasLoaded && !loading && !error && entries.length === 0 && (<p>{page === 0 ? 'No adjustments recorded.' : 'No more adjustments.'}</p>)}
            <table>
                <thead>
                    <tr>
                        <th>Change</th>
                        <th>Resulting quantity</th>
                        <th>Recorded at</th>
                    </tr>
                </thead>
                <tbody>
                    {entries.map((entry) => (
                        <tr key={entry.id}>
                            <td>{entry.change}</td>
                            <td>{entry.resultingQuantity}</td>
                            <td>{new Date(entry.createdAt).toLocaleString()}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
            <button type="button" onClick={() => void loadHistory(page - 1)} disabled={loading || !hasLoaded || page === 0}>
                Previous
            </button>
            <button type="button" onClick={() => void loadHistory(page + 1)} disabled={loading || !hasLoaded || entries.length < pageSize}>
                Next
            </button>
            {hasLoaded && <p>Page {page + 1} — {pageSize} entries per page</p>}
        </section>
    )
}

export default StockHistoryPanel