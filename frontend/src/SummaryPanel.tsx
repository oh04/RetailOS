import { useState } from 'react'
import type { InventorySummary } from './types'
import { apiFetch } from './api'

function SummaryPanel() {
    const [summary, setSummary] = useState<InventorySummary | null>(null)
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')

    async function loadSummary() {

        try {
            setLoading(true)
            setError('')
            const response = await apiFetch('/api/products/summary?threshold=10')
            if (!response.ok) {
                throw new Error('Request failed')
            }
            const data: InventorySummary = await response.json()
            setSummary(data)
        } catch {
            setError('Could not load the inventory summary')
        } finally {
            setLoading(false)
        }

    }

    return (
        <section>
            <h2>Store overview</h2>
            <button type="button" onClick={loadSummary} disabled={loading}>
                {loading ? 'Loading…' : 'Load summary'}
            </button>
            {error && <p role="alert">{error}</p>}
            {summary !== null && (
                <div className="summary-grid">
                    <article className="summary-card">
                        <h3>Products</h3>
                        <p>{summary.productCount}</p>
                    </article>
                    <article className="summary-card">
                        <h3>Total units</h3>
                        <p>{summary.totalUnits}</p>
                    </article>
                    <article className="summary-card">
                        <h3>Low stock - 10 or fewer</h3>
                        <p>{summary.lowStockCount}</p>
                    </article>
                </div>
            )}
        </section>
    )
}

export default SummaryPanel