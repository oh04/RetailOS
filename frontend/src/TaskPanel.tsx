import { useState } from 'react'
import type { Task, TaskStatus } from './types'

function TaskPanel() {
    const [tasks, setTasks] = useState<Task[]>([])
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')
    const [hasLoaded, setHasLoaded] = useState(false)
    const [title, setTitle] = useState('')
    const [dueDate, setDueDate] = useState('')
    const [creating, setCreating] = useState(false)
    const [createError, setCreateError] = useState('')
    const [createSuccess, setCreateSuccess] = useState('')
    const [updatingId, setUpdatingId] = useState<number | null>(null)
    const [updateError, setUpdateError] = useState('')
    const [statusFilter, setStatusFilter] = useState('ALL')

  async function loadTasks() {
    setLoading(true)
    setError('')
    try {
        const response = await fetch('/api/tasks')
        if (!response.ok) {
          throw new Error('Request failed')
        }

        const data: Task[] = await response.json()
        setTasks(data)
        setHasLoaded(true)
    } catch {
        setError('Could not load tasks. Check that the backend is running.')
    } finally {
        setLoading(false)
    }
  }

  async function createTask() {
    setCreating(true)
    setCreateError('')
    setCreateSuccess('')

    try {
      const response = await fetch('/api/tasks', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          title: title.trim(),
          dueDate: dueDate || null,
        }),
      })

      if (!response.ok) {
        throw new Error('Creation failed')
      }

      setTitle('')
      setDueDate('')
      await loadTasks()
      setCreateSuccess("Task created successfully.")
    } catch {
      setCreateError('Could not create the task. Check your inputs and retry.')
    } finally {
      setCreating(false)
    }
  }

  async function updateTaskStatus(id: number, status: TaskStatus) {
    setUpdatingId(id)
    setUpdateError('')

    try {
        const response = await fetch(`/api/tasks/${id}/status`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ status }),
        })

        if (!response.ok) {
            throw new Error('Status update failed')
        }

        await loadTasks()
    } catch {
        setUpdateError('Could not update task status. Please retry.')
    } finally {
        setUpdatingId(null)
    }
  }

  const busy = loading || creating || updatingId !== null

  const visibleTasks = tasks.filter((task) => statusFilter === 'ALL' || task.status === statusFilter)

  return (
    <section>
        <h2>Employee tasks</h2>
        <form className="product-form" onSubmit={(event) => {
            event.preventDefault()
            void createTask()
        }}>
            <label>
              Task title
              <input
                value = {title}
                onChange = {(event) => setTitle(event.target.value)}
                required
              />
            </label>
            <label>
              Due date
              <input
                type = "date"
                value = {dueDate}
                onChange = {(event) => setDueDate(event.target.value)}
              />
            </label>
            <button type="submit" disabled={busy}>
                {creating ? 'Creating…' : 'Create task'}
            </button>
        </form>
        {createError && <p role="alert">{createError}</p>}
        {createSuccess && <p role="status">{createSuccess}</p>}
        <button type = "button" onClick = {loadTasks} disabled = {busy}>
            {loading ? 'Loading...' : 'Load tasks'}
        </button>
        <label>
            Filter by status
            <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
                <option value="ALL">All tasks</option>
                <option value="TODO">To do</option>
                <option value="IN_PROGRESS">In progress</option>
                <option value="DONE">Done</option>
            </select>
        </label>
        {updateError && <p role="alert">{updateError}</p>}
        {error && <p role="alert">{error}</p>}
        {hasLoaded && !loading && !error && visibleTasks.length === 0 && (<p>{tasks.length === 0 ? 'No tasks yet.' : 'No tasks match this status.'}</p>)}
        <table>
            <thead>
              <tr>
                <th>Task</th>
                <th>Status</th>
                <th>Due date</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
                {visibleTasks.map((task) => (
                    <tr key={task.id}>
                    <td>{task.title}</td>
                    <td>{task.status}</td>
                    <td>{task.dueDate ?? '-'}</td>
                    <td>
                        <button type="button" onClick = {() => void updateTaskStatus(task.id, 'IN_PROGRESS')} disabled={busy || task.status === 'IN_PROGRESS'}>
                            Start
                        </button>
                        <button type="button" onClick={() => void updateTaskStatus(task.id, 'DONE')} disabled={busy || task.status === 'DONE'}>
                            Complete
                        </button>
                    </td>
                    </tr>
                ))}
            </tbody>
          </table>
    </section>
  )
}

export default TaskPanel