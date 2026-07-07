import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../api/client.js'

export default function NewOrder() {
  const [customers, setCustomers] = useState(null)
  const [customerId, setCustomerId] = useState('')
  const [error, setError] = useState(null)
  const navigate = useNavigate()

  useEffect(() => {
    api
      .listCustomers()
      .then((data) => {
        setCustomers(data)
        if (data.length > 0) setCustomerId(String(data[0].id))
      })
      .catch((e) => setError(e.message))
  }, [])

  async function handleSubmit(e) {
    e.preventDefault()
    try {
      const order = await api.createOrder(Number(customerId))
      navigate(`/orders/${order.id}`)
    } catch (err) {
      setError(err.message)
    }
  }

  if (error) return <p className="error">{error}</p>
  if (!customers) return <p>Loading...</p>

  return (
    <div>
      <h1>New Purchase Order</h1>
      <form onSubmit={handleSubmit}>
        <label htmlFor="customerId">Customer</label>
        <select id="customerId" value={customerId} onChange={(e) => setCustomerId(e.target.value)}>
          {customers.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>
        <button type="submit">Create Order</button>
      </form>
      <p>
        <Link to="/">&laquo; Back to order list</Link>
      </p>
    </div>
  )
}
