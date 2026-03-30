import { useState, useEffect, useCallback } from 'react'
import './App.css'

const API = 'http://localhost:8080/api'

function App() {
  const [auth, setAuth] = useState(() => {
    try { return JSON.parse(localStorage.getItem('bds_auth') || 'null') } catch { return null }
  })
  const [page, setPage] = useState('dashboard')
  const [stats, setStats] = useState(null)
  const [donors, setDonors] = useState([])
  const [requests, setRequests] = useState([])
  const [loading, setLoading] = useState(false)
  const [toast, setToast] = useState(null)
  const [loginForm, setLoginForm] = useState({ email: '', password: '' })
  const [loginError, setLoginError] = useState('')

  const showToast = (msg, type = 'success') => {
    setToast({ msg, type })
    setTimeout(() => setToast(null), 3000)
  }

  const apiFetch = useCallback(async (path, opts = {}) => {
    const res = await fetch(`${API}${path}`, {
      ...opts,
      headers: {
        'Content-Type': 'application/json',
        ...(auth?.token ? { Authorization: `Bearer ${auth.token}` } : {}),
        ...(opts.headers || {})
      }
    })
    const json = await res.json()
    if (!res.ok) throw new Error(json.message || 'Request failed')
    return json
  }, [auth])

  const login = async (e) => {
    e.preventDefault()
    setLoginError('')
    try {
      const res = await fetch(`${API}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(loginForm)
      })
      const json = await res.json()
      if (!res.ok) throw new Error(json.message || 'Login failed')
      localStorage.setItem('bds_auth', JSON.stringify(json.data))
      setAuth(json.data)
    } catch (err) {
      setLoginError(err.message)
    }
  }

  const logout = () => {
    localStorage.removeItem('bds_auth')
    setAuth(null)
  }

  const loadStats = useCallback(async () => {
    try {
      const res = await apiFetch('/requests/stats')
      setStats(res.data)
    } catch {}
  }, [apiFetch])

  const loadDonors = useCallback(async () => {
    setLoading(true)
    try {
      const res = await apiFetch('/donors')
      setDonors(res.data || [])
    } catch (err) { showToast(err.message, 'error') }
    finally { setLoading(false) }
  }, [apiFetch])

  const loadRequests = useCallback(async () => {
    setLoading(true)
    try {
      const res = await apiFetch('/requests')
      setRequests(res.data || [])
    } catch (err) { showToast(err.message, 'error') }
    finally { setLoading(false) }
  }, [apiFetch])

  const updateRequestStatus = async (id, status) => {
    try {
      await apiFetch(`/requests/${id}/status?status=${status}`, { method: 'PATCH' })
      showToast('Status updated')
      loadRequests()
    } catch (err) { showToast(err.message, 'error') }
  }

  const deleteRequest = async (id) => {
    if (!confirm('Delete this request?')) return
    try {
      await apiFetch(`/requests/${id}`, { method: 'DELETE' })
      showToast('Request deleted')
      loadRequests()
    } catch (err) { showToast(err.message, 'error') }
  }

  useEffect(() => {
    if (!auth) return
    loadStats()
    if (page === 'donors') loadDonors()
    if (page === 'requests') loadRequests()
  }, [auth, page])

  if (!auth) {
    return (
      <div className="login-screen">
        <div className="login-card">
          <div className="login-logo">
            <span className="drop-icon">🩸</span>
            <h1>BloodSync</h1>
            <p>Admin Dashboard</p>
          </div>
          <form onSubmit={login}>
            {loginError && <div className="form-error">{loginError}</div>}
            <div className="form-group">
              <label>Email</label>
              <input type="email" value={loginForm.email}
                onChange={e => setLoginForm(f => ({...f, email: e.target.value}))}
                placeholder="admin@blooddonation.com" required />
            </div>
            <div className="form-group">
              <label>Password</label>
              <input type="password" value={loginForm.password}
                onChange={e => setLoginForm(f => ({...f, password: e.target.value}))}
                placeholder="••••••••" required />
            </div>
            <button type="submit" className="btn-primary btn-block">Sign In</button>
          </form>
          <div className="login-hint">Default: admin@blooddonation.com / Admin@123</div>
        </div>
      </div>
    )
  }

  const bloodGroupColors = {
    A_POSITIVE: '#e74c3c', A_NEGATIVE: '#c0392b',
    B_POSITIVE: '#e67e22', B_NEGATIVE: '#d35400',
    AB_POSITIVE: '#9b59b6', AB_NEGATIVE: '#8e44ad',
    O_POSITIVE: '#27ae60', O_NEGATIVE: '#1e8449',
  }
  const bgLabel = {
    A_POSITIVE: 'A+', A_NEGATIVE: 'A-', B_POSITIVE: 'B+', B_NEGATIVE: 'B-',
    AB_POSITIVE: 'AB+', AB_NEGATIVE: 'AB-', O_POSITIVE: 'O+', O_NEGATIVE: 'O-'
  }
  const urgencyColor = { CRITICAL: '#e74c3c', HIGH: '#e67e22', MEDIUM: '#f1c40f', LOW: '#27ae60' }
  const statusColor = {
    PENDING: '#95a5a6', NOTIFIED: '#3498db', MATCHED: '#9b59b6',
    FULFILLED: '#27ae60', CANCELLED: '#e74c3c'
  }

  return (
    <div className="app">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-brand">
          <span>🩸</span>
          <span>BloodSync</span>
        </div>
        <nav className="sidebar-nav">
          {[
            { id: 'dashboard', icon: '◈', label: 'Dashboard' },
            { id: 'requests', icon: '⚡', label: 'Blood Requests' },
            { id: 'donors', icon: '♥', label: 'Donors' },
          ].map(item => (
            <button key={item.id}
              className={`nav-item ${page === item.id ? 'active' : ''}`}
              onClick={() => setPage(item.id)}>
              <span className="nav-icon">{item.icon}</span>
              <span>{item.label}</span>
            </button>
          ))}
        </nav>
        <div className="sidebar-footer">
          <div className="user-info">
            <div className="user-avatar">{auth.name?.[0]?.toUpperCase()}</div>
            <div>
              <div className="user-name">{auth.name}</div>
              <div className="user-role">{auth.roles?.[0]?.replace('ROLE_', '')}</div>
            </div>
          </div>
          <button className="btn-logout" onClick={logout}>⏏</button>
        </div>
      </aside>

      {/* Main */}
      <main className="main">
        {/* Toast */}
        {toast && <div className={`toast toast-${toast.type}`}>{toast.msg}</div>}

        {/* Dashboard */}
        {page === 'dashboard' && (
          <div className="page">
            <div className="page-header">
              <h2>Dashboard</h2>
              <button className="btn-refresh" onClick={loadStats}>↻ Refresh</button>
            </div>
            {stats ? (
              <>
                <div className="stats-grid">
                  {[
                    { label: 'Pending', value: stats.pending, color: '#95a5a6', icon: '⏳' },
                    { label: 'Notified', value: stats.notified, color: '#3498db', icon: '📣' },
                    { label: 'Matched', value: stats.matched, color: '#9b59b6', icon: '🤝' },
                    { label: 'Fulfilled', value: stats.fulfilled, color: '#27ae60', icon: '✅' },
                    { label: 'Total Donors', value: stats.totalDonors, color: '#e74c3c', icon: '♥' },
                    { label: 'Available', value: stats.availableDonors, color: '#e67e22', icon: '✓' },
                  ].map(s => (
                    <div key={s.label} className="stat-card" style={{ '--accent': s.color }}>
                      <div className="stat-icon">{s.icon}</div>
                      <div className="stat-value">{s.value}</div>
                      <div className="stat-label">{s.label}</div>
                    </div>
                  ))}
                </div>
                <div className="quick-actions">
                  <h3>Quick Actions</h3>
                  <div className="action-row">
                    <button className="action-btn" onClick={() => setPage('requests')}>
                      View All Requests →
                    </button>
                    <button className="action-btn" onClick={() => setPage('donors')}>
                      Manage Donors →
                    </button>
                  </div>
                </div>
              </>
            ) : (
              <div className="loading-state">Loading stats...</div>
            )}
          </div>
        )}

        {/* Requests */}
        {page === 'requests' && (
          <div className="page">
            <div className="page-header">
              <h2>Blood Requests</h2>
              <button className="btn-refresh" onClick={loadRequests}>↻ Refresh</button>
            </div>
            {loading ? (
              <div className="loading-state">Loading...</div>
            ) : requests.length === 0 ? (
              <div className="empty-state">No blood requests found</div>
            ) : (
              <div className="table-wrap">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Requester</th>
                      <th>Blood Group</th>
                      <th>Hospital</th>
                      <th>City</th>
                      <th>Urgency</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {requests.map(r => (
                      <tr key={r.id}>
                        <td>
                          <div className="cell-primary">{r.requesterName}</div>
                          <div className="cell-sub">{r.requesterPhone}</div>
                        </td>
                        <td>
                          <span className="blood-badge"
                            style={{ background: bloodGroupColors[r.bloodGroupNeeded] || '#e74c3c' }}>
                            {bgLabel[r.bloodGroupNeeded] || r.bloodGroupNeeded}
                          </span>
                        </td>
                        <td>{r.hospital}</td>
                        <td>{r.city}</td>
                        <td>
                          <span className="badge"
                            style={{ background: urgencyColor[r.urgency] || '#95a5a6' }}>
                            {r.urgency}
                          </span>
                        </td>
                        <td>
                          <span className="badge"
                            style={{ background: statusColor[r.status] || '#95a5a6' }}>
                            {r.status}
                          </span>
                        </td>
                        <td>
                          <div className="action-btns">
                            <select className="status-select"
                              value={r.status}
                              onChange={e => updateRequestStatus(r.id, e.target.value)}>
                              {['PENDING','NOTIFIED','MATCHED','FULFILLED','CANCELLED'].map(s => (
                                <option key={s} value={s}>{s}</option>
                              ))}
                            </select>
                            <button className="btn-del" onClick={() => deleteRequest(r.id)}>✕</button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {/* Donors */}
        {page === 'donors' && (
          <div className="page">
            <div className="page-header">
              <h2>Donors</h2>
              <button className="btn-refresh" onClick={loadDonors}>↻ Refresh</button>
            </div>
            {loading ? (
              <div className="loading-state">Loading...</div>
            ) : donors.length === 0 ? (
              <div className="empty-state">No donors registered yet</div>
            ) : (
              <div className="table-wrap">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Donor</th>
                      <th>Blood Group</th>
                      <th>City</th>
                      <th>Phone</th>
                      <th>Donations</th>
                      <th>Available</th>
                    </tr>
                  </thead>
                  <tbody>
                    {donors.map(d => (
                      <tr key={d.id}>
                        <td>
                          <div className="cell-primary">{d.name}</div>
                          <div className="cell-sub">{d.email}</div>
                        </td>
                        <td>
                          <span className="blood-badge"
                            style={{ background: bloodGroupColors[d.bloodGroup] || '#e74c3c' }}>
                            {bgLabel[d.bloodGroup] || d.bloodGroup}
                          </span>
                        </td>
                        <td>{d.city}</td>
                        <td>{d.phone}</td>
                        <td>
                          <span className="donation-count">{d.totalDonations || 0}</span>
                        </td>
                        <td>
                          <span className={`avail-dot ${d.available ? 'avail-yes' : 'avail-no'}`}>
                            {d.available ? '● Available' : '○ Unavailable'}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  )
}

export default App
