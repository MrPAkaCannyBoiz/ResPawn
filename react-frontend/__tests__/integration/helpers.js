const BASE_URL = process.env.VITE_API_BASE_URL || 'https://localhost:6760'

async function request(method, url, body, cookie) {
  const options = {
    method,
    headers: {},
  }
  if (body !== undefined) {
    options.headers['Content-Type'] = 'application/json'
    options.body = JSON.stringify(body)
  }
  if (cookie) {
    options.headers['Cookie'] = cookie
  }
  const response = await fetch(`${BASE_URL}${url}`, options)
  const text = await response.text()
  let data = null
  if (text) {
    try { data = JSON.parse(text) } catch { data = text }
  }
  return { status: response.status, data, headers: response.headers }
}

export function get(url, cookie) { return request('GET', url, undefined, cookie) }
export function post(url, body, cookie) { return request('POST', url, body, cookie) }
export function patch(url, body, cookie) { return request('PATCH', url, body, cookie) }

export function extractCookie(headers) {
  const setCookie = headers.get('set-cookie')
  if (!setCookie) return null
  const match = setCookie.match(/JwtCookie=([^;]+)/)
  return match ? `JwtCookie=${match[1]}` : null
}
