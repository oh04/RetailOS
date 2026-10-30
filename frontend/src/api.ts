type CsrfResponse = {
    token: string
    headerName: string
}

export async function apiFetch(
    url: string,
    options: RequestInit = {},
): Promise<Response> {
    const method = (options.method ?? 'GET').toUpperCase()
    const headers = new Headers(options.headers)

    if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
        const tokenResponse = await fetch('/api/auth/csrf', {
            credentials: 'same-origin',
            cache: 'no-store',
            signal: options.signal,
        })

        if (!tokenResponse.ok) {
            throw new Error('Could not obtain a CSRF token')
        }

        const csrf: CsrfResponse = await tokenResponse.json()
        headers.set(csrf.headerName, csrf.token)
    }

    return fetch(url, {
        ...options,
        headers,
        credentials: 'same-origin',
    })
}