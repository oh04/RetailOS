import { useState } from 'react'
import { apiFetch } from './api'
import type { UserResponse } from './types'

type Props = {
    onAuthenticated: (user: UserResponse) => void
}

function AuthPanel({ onAuthenticated }: Props) {
    const [mode, setMode] = useState<'login' | 'register'>('login')
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [saving, setSaving] = useState(false)
    const [error, setError] = useState('')
    const [notice, setNotice] = useState('')

    async function submit() {
        setSaving(true)
        setError('')
        setNotice('')

        try {
            if (mode === 'register') {
                const response = await apiFetch('/api/auth/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        email: email.trim(),
                        password,
                    }),
                })

                if (response.status === 409) {
                    throw new Error('That email is already registered.')
                }

                if (!response.ok) {
                    throw new Error(
                        'Registration failed. Check the email and password requirements.'
                    )
                }

                setMode('login')
                setPassword('')
                setNotice('Account created. Sign in with your password.')
                return
            }

            const response = await apiFetch('/api/auth/login', {
                method: 'POST',
                body: new URLSearchParams({
                    username: email.trim(),
                    password,
                }),
            })

            if (!response.ok) {
                throw new Error('Sign-in failed. Check your email and password.')
            }

            const accountResponse = await apiFetch('/api/auth/me', {
                cache: 'no-store',
            })

            if (!accountResponse.ok) {
                throw new Error('Could not load your account. Please retry.')
            }

            const user: UserResponse = await accountResponse.json()
            onAuthenticated(user)
        } catch (error) {
            setError(error instanceof Error ? error.message : 'Request failed.')
        } finally {
            setSaving(false)
        }
    }

    return (
        <main className="app">
            <h1>RetailOS</h1>
            <h2>{mode === 'login' ? 'Sign in' : 'Create account'}</h2>

            <form
                className="product-form"
                onSubmit={(event) => {
                    event.preventDefault()
                    void submit()
                }}
            >
                <label>
                    Email
                    <input
                        type="email"
                        value={email}
                        onChange={(event) => setEmail(event.target.value)}
                        required
                        autoComplete="username"
                    />
                </label>

                <label>
                    Password
                    <input
                        type="password"
                        value={password}
                        onChange={(event) => setPassword(event.target.value)}
                        required
                        autoComplete={
                            mode === 'register' ? 'new-password' : 'current-password'
                        }
                    />
                </label>

                <button type="submit" disabled={saving}>
                    {saving
                        ? 'Please wait…'
                        : mode === 'login'
                            ? 'Sign in'
                            : 'Create account'}
                </button>
            </form>

            {mode === 'register' && (
                <p>Use at least 12 characters and at most 72 UTF-8 bytes.</p>
            )}

            {error && <p role="alert">{error}</p>}
            {notice && <p role="status">{notice}</p>}

            <button
                type="button"
                disabled={saving}
                onClick={() => {
                    setMode(mode === 'login' ? 'register' : 'login')
                    setError('')
                    setNotice('')
                }}
            >
                {mode === 'login' ? 'Create an account' : 'Back to sign in'}
            </button>
        </main>
    )
}

export default AuthPanel