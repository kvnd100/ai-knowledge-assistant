const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

const TOKEN_KEY = 'aka_token'

export class ApiError extends Error {
  constructor(status, message, fieldErrors) {
    super(message)
    this.status = status
    this.fieldErrors = fieldErrors || null
  }
}

let onUnauthorized = null

/** Registered once by AuthContext so any 401 logs the user out globally. */
export function setUnauthorizedHandler(handler) {
  onUnauthorized = handler
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }
}

async function request(path, { method = 'GET', body, formData, auth = true } = {}) {
  const headers = {}
  if (auth) {
    const token = getToken()
    if (token) headers.Authorization = `Bearer ${token}`
  }
  if (body !== undefined) headers['Content-Type'] = 'application/json'

  let response
  try {
    response = await fetch(`${API_URL}${path}`, {
      method,
      headers,
      body: formData ?? (body !== undefined ? JSON.stringify(body) : undefined),
    })
  } catch {
    throw new ApiError(0, 'Cannot reach the server. Please check your connection and try again.')
  }

  if (response.status === 204) return null

  let payload = null
  try {
    payload = await response.json()
  } catch {
    // Non-JSON body (e.g. gateway error page); fall through with null payload.
  }

  if (!response.ok) {
    if (response.status === 401 && auth && onUnauthorized) onUnauthorized()
    throw new ApiError(
      response.status,
      payload?.message || `Request failed (${response.status})`,
      payload?.fieldErrors,
    )
  }
  return payload
}

export const api = {
  register: (data) => request('/api/auth/register', { method: 'POST', body: data, auth: false }),
  login: (data) => request('/api/auth/login', { method: 'POST', body: data, auth: false }),
  me: () => request('/api/users/me'),

  listConversations: () => request('/api/conversations'),
  createConversation: (title) => request('/api/conversations', { method: 'POST', body: { title: title || null } }),
  getConversation: (id) => request(`/api/conversations/${id}`),
  deleteConversation: (id) => request(`/api/conversations/${id}`, { method: 'DELETE' }),
  sendMessage: (id, content) =>
    request(`/api/conversations/${id}/messages`, { method: 'POST', body: { content } }),

  listDocuments: () => request('/api/documents'),
  uploadDocument: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request('/api/documents', { method: 'POST', formData })
  },

  dashboardStats: () => request('/api/dashboard/stats'),
}
