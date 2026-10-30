import { useState } from 'react'
import type { Task } from './types'
import { apiFetch } from './api'

function OverdueReport() {
    const [before, setBefore] = useState('')
    const [reportedBefore, setReportedBefore] = useState('')
    const [tasks, setTasks] = useState<Task[]>([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')
    const [hasLoaded, setHasLoaded] = useState(false)

    async function loadReport() {
        setLoading(true)
        setError('')
        setTasks([])
        setHasLoaded(false)
        try {
            const response = await apiFetch(`/api/tasks/overdue?before=${before}`)
            if (!response.ok) {
                throw new Error('Request failed')
            }
            const data: Task[] = await response.json()
            setTasks(data)
            setReportedBefore(before)
            setHasLoaded(true)
        } catch {
            setError("Could not load the overdue-task report.")
        } finally {
            setLoading(false)
        }
    }

    return (
        <section>
            <h2>Overdue tasks</h2>
            <form className="product-form" onSubmit={(event) => {
                event.preventDefault()
                void loadReport()
            }}>
                <label>
                    Deadline before
                    <input type="date" value={before} onChange={(event) => setBefore(event.target.value)} required />
                </label>
                <button type="submit" disabled={loading}>
                    {loading ? 'Loading…' : 'Run report'}
                </button>
            </form>
            {error && <p role="alert">{error}</p>}
            {hasLoaded && <p>Unfinished tasks due before {reportedBefore}</p>}
            {hasLoaded && tasks.length === 0 && <p>No unfinished tasks are overdue before this date.</p>}
            <table>
                <thead>
                    <tr>
                        <th>Task</th>
                        <th>Status</th>
                        <th>Due date</th>
                    </tr>
                </thead>
                <tbody>
                    {tasks.map((task) => (
                        <tr key={task.id}>
                            <td>{task.title}</td>
                            <td>{task.status}</td>
                            <td>{task.dueDate}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </section>
    )
}

export default OverdueReport