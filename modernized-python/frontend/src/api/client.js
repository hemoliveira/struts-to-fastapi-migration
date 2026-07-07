const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8000'

async function request(path, options = {}) {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })
  const body = await res.json().catch(() => null)
  if (!res.ok) {
    const message = body?.detail || `Request failed with status ${res.status}`
    throw new Error(message)
  }
  return body
}

export const api = {
  listCustomers: () => request('/customers'),
  listProducts: () => request('/products'),
  listOrders: () => request('/orders'),
  getOrder: (id) => request(`/orders/${id}`),
  createOrder: (customerId) =>
    request('/orders', { method: 'POST', body: JSON.stringify({ customer_id: customerId }) }),
  addLineItem: (orderId, productId, quantity) =>
    request(`/orders/${orderId}/line-items`, {
      method: 'POST',
      body: JSON.stringify({ product_id: productId, quantity }),
    }),
  submitOrder: (orderId) => request(`/orders/${orderId}/submit`, { method: 'POST' }),
  approveOrder: (orderId) => request(`/orders/${orderId}/approve`, { method: 'POST' }),
  rejectOrder: (orderId) => request(`/orders/${orderId}/reject`, { method: 'POST' }),
}
