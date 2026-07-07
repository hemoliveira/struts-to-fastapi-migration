import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api } from '../api/client.js'
import { formatDate } from '../format.js'

// Mirrors the stepper markup/logic in legacy-java's view.jsp so both apps
// render the same signature element for the order lifecycle.
function stepClasses(status) {
  const isDraft = status === 'DRAFT'
  const isSubmitted = status === 'SUBMITTED'
  const isApproved = status === 'APPROVED'
  const isRejected = status === 'REJECTED'
  const isDecided = isApproved || isRejected

  return {
    step1: isDraft ? 'is-current' : 'is-complete',
    step1Dot: isDraft ? '1' : '✓',
    line1: isDraft ? '' : 'is-complete',
    step2: isSubmitted ? 'is-current' : isDecided ? 'is-complete' : '',
    step2Dot: isDecided ? '✓' : '2',
    line2: isApproved ? 'is-approved' : isRejected ? 'is-rejected' : '',
    step3: isApproved ? 'is-approved' : isRejected ? 'is-rejected' : '',
    step3Label: isApproved ? 'Approved' : isRejected ? 'Rejected' : 'Decision',
  }
}

export default function OrderDetail() {
  const { id } = useParams()
  const [order, setOrder] = useState(null)
  const [products, setProducts] = useState(null)
  const [productId, setProductId] = useState('')
  const [quantity, setQuantity] = useState(1)
  const [error, setError] = useState(null)
  const [notFound, setNotFound] = useState(false)

  function reload() {
    api
      .getOrder(id)
      .then(setOrder)
      .catch(() => setNotFound(true))
  }

  useEffect(() => {
    reload()
    api.listProducts().then((data) => {
      setProducts(data)
      if (data.length > 0) setProductId(String(data[0].id))
    })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  async function handleAddLineItem(e) {
    e.preventDefault()
    setError(null)
    try {
      await api.addLineItem(id, Number(productId), Number(quantity))
      reload()
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleAction(action) {
    setError(null)
    try {
      await action(id)
      reload()
    } catch (err) {
      setError(err.message)
    }
  }

  if (notFound) return <p className="error">Order {id} was not found.</p>
  if (!order) return <p>Loading...</p>

  const s = stepClasses(order.status)

  return (
    <div>
      <h1>Order #{order.id}</h1>
      {error && <p className="error">{error}</p>}

      <div className="stepper">
        <div className={`step ${s.step1}`}>
          <span className="step-dot">{s.step1Dot}</span>
          <span className="step-label">Draft</span>
        </div>
        <div className={`step-line ${s.line1}`}></div>
        <div className={`step ${s.step2}`}>
          <span className="step-dot">{s.step2Dot}</span>
          <span className="step-label">Submitted</span>
        </div>
        <div className={`step-line ${s.line2}`}></div>
        <div className={`step ${s.step3}`}>
          <span className="step-dot">3</span>
          <span className="step-label">{s.step3Label}</span>
        </div>
      </div>

      <table className="fields">
        <tbody>
          <tr>
            <th>Customer</th>
            <td>{order.customer_name}</td>
          </tr>
          <tr>
            <th>Status</th>
            <td>
              <span className={`status status-${order.status}`}>{order.status}</span>
            </td>
          </tr>
          <tr>
            <th>Created</th>
            <td>{formatDate(order.created_date)}</td>
          </tr>
        </tbody>
      </table>

      <h2>Line Items</h2>
      <table className="grid">
        <thead>
          <tr>
            <th>Product</th>
            <th>Qty</th>
            <th>Unit Price</th>
            <th>Subtotal</th>
            <th>Discount</th>
            <th>Line Total</th>
          </tr>
        </thead>
        <tbody>
          {order.line_items.map((item) => (
            <tr key={item.id}>
              <td>{item.product_name}</td>
              <td>{item.quantity}</td>
              <td>${Number(item.unit_price).toFixed(2)}</td>
              <td>${Number(item.line_subtotal).toFixed(2)}</td>
              <td>${Number(item.line_discount).toFixed(2)}</td>
              <td>${Number(item.line_total).toFixed(2)}</td>
            </tr>
          ))}
          {order.line_items.length === 0 && (
            <tr>
              <td colSpan="6">No line items yet.</td>
            </tr>
          )}
        </tbody>
      </table>

      <table className="fields totals">
        <tbody>
          <tr>
            <th>Subtotal</th>
            <td>${Number(order.subtotal).toFixed(2)}</td>
          </tr>
          <tr>
            <th>Discount</th>
            <td>-${Number(order.discount_amount).toFixed(2)}</td>
          </tr>
          <tr>
            <th>Tax (8%)</th>
            <td>${Number(order.tax_amount).toFixed(2)}</td>
          </tr>
          <tr>
            <th>Total</th>
            <td>
              <strong>${Number(order.total).toFixed(2)}</strong>
            </td>
          </tr>
        </tbody>
      </table>

      {order.status === 'DRAFT' && products && (
        <>
          <h2>Add Line Item</h2>
          <form onSubmit={handleAddLineItem}>
            <label htmlFor="productId">Product</label>
            <select id="productId" value={productId} onChange={(e) => setProductId(e.target.value)}>
              {products.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name} (${Number(p.unit_price).toFixed(2)}/unit)
                </option>
              ))}
            </select>

            <label htmlFor="quantity">Quantity</label>
            <input
              id="quantity"
              type="number"
              min="1"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
            />
            <span className="hint">10+ units gets a 10% bulk discount on that line</span>

            <button type="submit">Add Item</button>
          </form>

          {order.line_items.length > 0 && (
            <button onClick={() => handleAction(api.submitOrder)}>Submit Order for Approval</button>
          )}
        </>
      )}

      {order.status === 'SUBMITTED' && (
        <>
          <h2>Approval</h2>
          <div className="button-row">
            <button onClick={() => handleAction(api.approveOrder)}>Approve</button>
            <button onClick={() => handleAction(api.rejectOrder)}>Reject</button>
          </div>
        </>
      )}

      <p>
        <Link to="/">&laquo; Back to order list</Link>
      </p>
    </div>
  )
}
