import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client.js'
import { formatDate } from '../format.js'

export default function OrderList() {
  const [orders, setOrders] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    api.listOrders().then(setOrders).catch((e) => setError(e.message))
  }, [])

  if (error) return <p className="error">{error}</p>
  if (!orders) return <p>Loading...</p>

  const stats = {
    totalRevenue: 0,
    aov: 0,
    approvalRate: 0,
    pendingCount: 0,
  }

  if (orders.length > 0) {
    const approvedOrders = orders.filter((o) => o.status === 'APPROVED')
    const rejectedOrders = orders.filter((o) => o.status === 'REJECTED')
    const submittedOrders = orders.filter((o) => o.status === 'SUBMITTED')

    stats.totalRevenue = approvedOrders.reduce((sum, o) => sum + Number(o.total), 0)
    stats.pendingCount = submittedOrders.length

    const totalFinished = approvedOrders.length + rejectedOrders.length
    stats.approvalRate = totalFinished > 0 ? (approvedOrders.length / totalFinished) * 100 : 0

    const sumAllTotals = orders.reduce((sum, o) => sum + Number(o.total), 0)
    stats.aov = sumAllTotals / orders.length
  }

  return (
    <div>
      <h1>Purchase Orders</h1>

      <div className="dashboard-grid">
        <div className="metric-card">
          <span className="metric-label">💰 Total Revenue</span>
          <span className="metric-value">${stats.totalRevenue.toFixed(2)}</span>
        </div>
        <div className="metric-card">
          <span className="metric-label">📊 Average Order (AOV)</span>
          <span className="metric-value">${stats.aov.toFixed(2)}</span>
        </div>
        <div className="metric-card">
          <span className="metric-label">✅ Approval Rate</span>
          <span className="metric-value">{stats.approvalRate.toFixed(1)}%</span>
        </div>
        <div className="metric-card">
          <span className="metric-label">⏳ Pending Approval</span>
          <span className="metric-value">{stats.pendingCount}</span>
        </div>
      </div>

      <p style={{ margin: '1.5rem 0' }}>
        <Link to="/new" className="button-link" style={{ 
          background: 'var(--accent)', 
          color: '#fff', 
          padding: '0.55rem 1.1rem', 
          borderRadius: '6px', 
          fontWeight: '600',
          display: 'inline-block',
          textDecoration: 'none'
        }}>
          + New Order
        </Link>
      </p>

      <table className="grid">
        <thead>
          <tr>
            <th>ID</th>
            <th>Customer</th>
            <th>Status</th>
            <th>Created</th>
            <th>Total</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {orders.map((order) => (
            <tr key={order.id}>
              <td>{order.id}</td>
              <td>{order.customer_name}</td>
              <td>
                <span className={`status status-${order.status}`}>{order.status}</span>
              </td>
              <td>{formatDate(order.created_date)}</td>
              <td>${Number(order.total).toFixed(2)}</td>
              <td>
                <Link to={`/orders/${order.id}`} style={{ fontWeight: '600' }}>View</Link>
              </td>
            </tr>
          ))}
          {orders.length === 0 && (
            <tr>
              <td colSpan="6">No orders yet.</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  )
}
