const BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

async function request(method, url, body) {
  const options = {
    method,
    credentials: 'include',
    headers: {},
  }

  if (body !== undefined) {
    options.headers['Content-Type'] = 'application/json'
    options.body = JSON.stringify(body)
  }

  const response = await fetch(`${BASE_URL}${url}`, options)

  if (!response.ok) {
    let errorMessage
    try {
      const errorBody = await response.text()
      errorMessage = errorBody || response.statusText
    } catch {
      errorMessage = response.statusText
    }
    const error = new Error(errorMessage)
    error.status = response.status
    throw error
  }

  const text = await response.text()
  if (!text) return null
  return JSON.parse(text)
}

export function get(url) {
  return request('GET', url)
}

export function post(url, body) {
  return request('POST', url, body)
}

export function patch(url, body) {
  return request('PATCH', url, body)
}
