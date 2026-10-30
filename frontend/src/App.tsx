import { useEffect, useState } from 'react'
import type { UserResponse } from './types'
import AuthPanel from './AuthPanel'
import Workspace from './Workspace'
import { apiFetch } from './api'

function App() {
    const [user, setUser] = useState<UserResponse | null>(null)
    const [checking, setChecking] = useState(true)
    const [sessionError, setSessionError] = useState('')
    const [loggingOut, setLoggingOut] = useState(false)
    const [logoutError, setLogoutError] = useState('')

    useEffect(() => {
        const controller = new AbortController()

        async function checkSession() {
            try {
                const response = await apiFetch('/api/auth/me', {
                    cache: 'no-store',
                    signal: controller.signal,
                })

                if (controller.signal.aborted) {
                    return
                }

                if (response.status === 401) {
                    setUser(null)
                    return
                }

                if (!response.ok) {
                    throw new Error('Session check failed')
                }

                const account: UserResponse = await response.json()

                if (!controller.signal.aborted) {
                    setUser(account)
                }
            } catch {
                if (!controller.signal.aborted) {
                    setSessionError('Could not connect to RetailOS. Check the backend.')
                }
            } finally {
                if (!controller.signal.aborted) {
                    setChecking(false)
                }
            }
        }

        void checkSession()

        return () => controller.abort()
    }, [])

    if (checking) {
        return <main className="app"><p>Checking your session…</p></main>
    }

    if (sessionError) {
        return (
            <main className="app">
                <p role="alert">{sessionError}</p>
                <button type="button" onClick={() => window.location.reload()}>
                    Retry connection
                </button>
            </main>
        )
    }

    if (user === null) {
        return <AuthPanel onAuthenticated={setUser} />
    }

    async function logout() {
        setLoggingOut(true)
        setLogoutError('')

        try {
            const response = await apiFetch(`/api/auth/logout`, { method: 'POST' })

            if (!response.ok) {
                throw new Error('Sign out failed')
            }

            setUser(null)
        } catch {
            setLogoutError('Could not sign out. Please retry.')
        } finally {
            setLoggingOut(false)
        }
    }

    return (
        <>
            <div className="session-bar">
                <span>Signed in as {user.email} — {user.role}</span>

                <button
                    type="button"
                    onClick={() => void logout()}
                    disabled={loggingOut}
                >
                    {loggingOut ? 'Signing out…' : 'Sign out'}
                </button>
            </div>

            {logoutError && <p role="alert">{logoutError}</p>}

            <Workspace />
        </>
    )
}

export default App