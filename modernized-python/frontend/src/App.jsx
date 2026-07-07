import { Link, Route, Routes } from 'react-router-dom'
import { useEffect, useState } from 'react'
import OrderList from './pages/OrderList.jsx'
import NewOrder from './pages/NewOrder.jsx'
import OrderDetail from './pages/OrderDetail.jsx'

export default function App() {
  const [theme, setTheme] = useState(() => {
    return localStorage.getItem('theme') || 'light'
  })

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme)
    localStorage.setItem('theme', theme)
  }, [theme])

  const toggleTheme = () => {
    setTheme((prev) => (prev === 'light' ? 'dark' : 'light'))
  }

  return (
    <div className="container">
      <div className="topbar" style={{ justifyContent: 'space-between' }}>
        <Link className="brand" to="/">
          OrderCo
        </Link>
        <div className="topbar-actions">
          <button className="theme-toggle-btn" onClick={toggleTheme}>
            {theme === 'light' ? '🌙 Dark Mode' : '☀️ Light Mode'}
          </button>
        </div>
      </div>
      <Routes>
        <Route path="/" element={<OrderList />} />
        <Route path="/new" element={<NewOrder />} />
        <Route path="/orders/:id" element={<OrderDetail />} />
      </Routes>
    </div>
  )
}
