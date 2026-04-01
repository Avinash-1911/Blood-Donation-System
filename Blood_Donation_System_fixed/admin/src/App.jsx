import { useState, useEffect, useCallback } from 'react'
import './App.css'

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081'

// Indian states and their cities
const INDIA_STATES_CITIES = {
  "Andhra Pradesh": ["Visakhapatnam", "Vijayawada", "Guntur", "Nellore", "Tirupati"],
  "Arunachal Pradesh": ["Itanagar", "Naharlagun", "Pasighat", "Tawang"],
  "Assam": ["Guwahati", "Silchar", "Dibrugarh", "Jorhat", "Nagaon"],
  "Bihar": ["Patna", "Gaya", "Bhagalpur", "Muzaffarpur", "Darbhanga"],
  "Chhattisgarh": ["Raipur", "Bhilai", "Bilaspur", "Korba", "Durg"],
  "Goa": ["Panaji", "Margao", "Vasco da Gama", "Mapusa"],
  "Gujarat": ["Ahmedabad", "Surat", "Vadodara", "Rajkot", "Bhavnagar"],
  "Haryana": ["Faridabad", "Gurgaon", "Panipat", "Ambala", "Karnal"],
  "Himachal Pradesh": ["Shimla", "Dharamshala", "Solan", "Mandi", "Kullu"],
  "Jharkhand": ["Ranchi", "Jamshedpur", "Dhanbad", "Bokaro", "Hazaribagh"],
  "Karnataka": ["Bengaluru", "Mysuru", "Mangaluru", "Hubli", "Belagavi"],
  "Kerala": ["Thiruvananthapuram", "Kochi", "Kozhikode", "Thrissur", "Kollam"],
  "Madhya Pradesh": ["Bhopal", "Indore", "Gwalior", "Jabalpur", "Ujjain"],
  "Maharashtra": ["Mumbai", "Pune", "Nagpur", "Thane", "Nashik", "Aurangabad"],
  "Manipur": ["Imphal", "Thoubal", "Bishnupur"],
  "Meghalaya": ["Shillong", "Tura", "Nongstoin"],
  "Mizoram": ["Aizawl", "Lunglei", "Champhai"],
  "Nagaland": ["Kohima", "Dimapur", "Mokokchung"],
  "Odisha": ["Bhubaneswar", "Cuttack", "Rourkela", "Puri", "Berhampur"],
  "Punjab": ["Ludhiana", "Amritsar", "Jalandhar", "Patiala", "Bathinda"],
  "Rajasthan": ["Jaipur", "Jodhpur", "Udaipur", "Kota", "Ajmer", "Bikaner"],
  "Sikkim": ["Gangtok", "Namchi", "Gyalshing"],
  "Tamil Nadu": ["Chennai", "Coimbatore", "Madurai", "Tiruchirappalli", "Salem"],
  "Telangana": ["Hyderabad", "Warangal", "Nizamabad", "Khammam", "Karimnagar"],
  "Tripura": ["Agartala", "Dharmanagar", "Udaipur"],
  "Uttar Pradesh": ["Lucknow", "Kanpur", "Ghaziabad", "Agra", "Varanasi", "Meerut"],
  "Uttarakhand": ["Dehradun", "Haridwar", "Roorkee", "Haldwani", "Nainital"],
  "West Bengal": ["Kolkata", "Howrah", "Durgapur", "Asansol", "Siliguri"],
  "Delhi": ["New Delhi", "Delhi Cantonment", "Dwarka"],
  "Jammu and Kashmir": ["Srinagar", "Jammu", "Anantnag"],
  "Ladakh": ["Leh", "Kargil"],
  "Puducherry": ["Puducherry", "Karaikal", "Mahe", "Yanam"]
}

// City coordinates for geocoding
const CITY_COORDINATES = {
  "Mumbai": { lat: 19.0760, lng: 72.8777 },
  "Pune": { lat: 18.5204, lng: 73.8567 },
  "Bengaluru": { lat: 12.9716, lng: 77.5946 },
  "Chennai": { lat: 13.0827, lng: 80.2707 },
  "Hyderabad": { lat: 17.3850, lng: 78.4867 },
  "Kolkata": { lat: 22.5726, lng: 88.3639 },
  "Delhi": { lat: 28.7041, lng: 77.1025 },
  "New Delhi": { lat: 28.6139, lng: 77.2090 },
  "Jaipur": { lat: 26.9124, lng: 75.7873 },
  "Ahmedabad": { lat: 23.0225, lng: 72.5714 },
  "Lucknow": { lat: 26.8467, lng: 80.9462 },
  "Surat": { lat: 21.1702, lng: 72.8311 },
  "Indore": { lat: 22.7196, lng: 75.8577 },
  "Patna": { lat: 25.5941, lng: 85.1376 },
  "Bhopal": { lat: 23.2599, lng: 77.4126 },
  "Visakhapatnam": { lat: 17.6869, lng: 83.2185 },
  "Nagpur": { lat: 21.1458, lng: 79.0882 },
  "Thane": { lat: 19.2183, lng: 72.9781 },
  "Vadodara": { lat: 22.3072, lng: 73.1812 },
  "Ghaziabad": { lat: 28.6692, lng: 77.4538 },
  "Kanpur": { lat: 26.4499, lng: 80.3319 },
  "Kochi": { lat: 9.9312, lng: 76.2673 },
  "Coimbatore": { lat: 11.0168, lng: 76.9558 },
  "Mysuru": { lat: 12.2958, lng: 76.6394 },
  "Thiruvananthapuram": { lat: 8.5241, lng: 76.9366 }
}

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
  const [dashboardError, setDashboardError] = useState('')
  const [authMode, setAuthMode] = useState('existing')
  const [loginForm, setLoginForm] = useState({ email: '', password: '' })
  const [signupForm, setSignupForm] = useState({ name: '', email: '', phone: '', password: '' })
  const [loginError, setLoginError] = useState('')
  
  // Modal states
  const [showDonorModal, setShowDonorModal] = useState(false)
  const [showRequestModal, setShowRequestModal] = useState(false)
  const [requestsTab, setRequestsTab] = useState('my-requests')
  const [donorForm, setDonorForm] = useState({
    name: '', email: '', password: '', phone: '', bloodGroup: 'O_POSITIVE',
    age: '', gender: 'Male', city: '', address: '', state: '', pincode: ''
  })
  const [requestForm, setRequestForm] = useState({
    requesterName: '', requesterPhone: '', requesterEmail: '', bloodGroupNeeded: 'O_POSITIVE',
    unitsNeeded: 1, urgency: 'MEDIUM', patientName: '', hospital: '', hospitalAddress: '',
    city: '', state: '', notes: ''
  })

  const [donorCities, setDonorCities] = useState([])
  const [requestCities, setRequestCities] = useState([])

  const showToast = (msg, type = 'success') => {
    setToast({ msg, type })
    setTimeout(() => setToast(null), 3000)
  }

  const apiFetch = useCallback(async (path, opts = {}) => {
    const res = await fetch(`${API_BASE}${path}`, {
      ...opts,
      headers: {
        'Content-Type': 'application/json',
        ...(auth?.token ? { Authorization: `Bearer ${auth.token}` } : {}),
        ...(opts.headers || {})
      }
    })

    const contentType = res.headers.get('content-type') || ''
    let payload = null

    if (contentType.includes('application/json')) {
      payload = await res.json()
    } else {
      const text = await res.text()
      payload = text ? { message: text } : null
    }

    if (!res.ok) {
      throw new Error(payload?.message || `Request failed (${res.status})`)
    }

    return payload || { data: null }
  }, [auth])

  const login = async (e) => {
    e.preventDefault()
    setLoginError('')
    try {
      const res = await fetch(`${API_BASE}/api/auth/login`, {
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

  const signup = async (e) => {
    e.preventDefault()
    setLoginError('')
    try {
      const res = await fetch(`${API_BASE}/api/auth/register/admin`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(signupForm)
      })
      const json = await res.json()
      if (!res.ok) throw new Error(json.message || 'Sign up failed')
      localStorage.setItem('bds_auth', JSON.stringify(json.data))
      setAuth(json.data)
      showToast('Account created successfully!')
    } catch (err) {
      setLoginError(err.message)
    }
  }

  const forgotPassword = () => {
    setLoginError('Forgot password is not configured yet. Please contact your administrator.')
  }

  const logout = () => {
    localStorage.removeItem('bds_auth')
    setAuth(null)
  }

  const loadStats = useCallback(async () => {
    setDashboardError('')
    try {
      const res = await apiFetch('/api/requests/stats')
      setStats(res.data)
    } catch (err) {
      setStats(null)
      setDashboardError(err.message)
      showToast(err.message, 'error')
      console.error('Stats error:', err)
    }
  }, [apiFetch])

  const loadDonors = useCallback(async () => {
    setLoading(true)
    try {
      const res = await apiFetch('/api/donors')
      setDonors(res.data || [])
    } catch (err) { 
      showToast(err.message, 'error')
      setDonors([])
    }
    finally { setLoading(false) }
  }, [apiFetch])

  const loadRequests = useCallback(async () => {
    setLoading(true)
    try {
      const res = await apiFetch('/api/requests')
      setRequests(res.data || [])
    } catch (err) { 
      showToast(err.message, 'error')
      setRequests([])
    }
    finally { setLoading(false) }
  }, [apiFetch])

  const getCoordinates = (city) => {
    return CITY_COORDINATES[city] || { lat: 20.5937, lng: 78.9629 } // Default to center of India
  }

  const submitDonor = async (e) => {
    e.preventDefault()
    try {
      const coords = getCoordinates(donorForm.city)
      const payload = {
        ...donorForm,
        age: parseInt(donorForm.age),
        longitude: coords.lng,
        latitude: coords.lat
      }
      
      const res = await fetch(`${API_BASE}/api/donors/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
      
      if (!res.ok) {
        const err = await res.json()
        throw new Error(err.message || 'Registration failed')
      }
      
      showToast('Donor registered successfully!')
      setShowDonorModal(false)
      setDonorForm({
        name: '', email: '', password: '', phone: '', bloodGroup: 'O_POSITIVE',
        age: '', gender: 'Male', city: '', address: '', state: '', pincode: ''
      })
      setDonorCities([])
      
      await loadDonors()
      await loadStats()
    } catch (err) {
      showToast(err.message, 'error')
    }
  }

  const submitRequest = async (e) => {
    e.preventDefault()
    try {
      const coords = getCoordinates(requestForm.city)
      const payload = {
        ...requestForm,
        unitsNeeded: parseInt(requestForm.unitsNeeded),
        longitude: coords.lng,
        latitude: coords.lat
      }
      
      const res = await fetch(`${API_BASE}/api/requests/create`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
      
      if (!res.ok) {
        const err = await res.json()
        throw new Error(err.message || 'Request creation failed')
      }
      
      showToast('Blood request created successfully!')
      setShowRequestModal(false)
      setRequestForm({
        requesterName: '', requesterPhone: '', requesterEmail: '', bloodGroupNeeded: 'O_POSITIVE',
        unitsNeeded: 1, urgency: 'MEDIUM', patientName: '', hospital: '', hospitalAddress: '',
        city: '', state: '', notes: ''
      })
      setRequestCities([])
      
      await loadRequests()
      await loadStats()
    } catch (err) {
      showToast(err.message, 'error')
    }
  }

  const updateRequestStatus = async (id, status) => {
    try {
      await apiFetch(`/api/requests/${id}/status?status=${status}`, { method: 'PATCH' })
      showToast('Status updated')
      await loadRequests()
      await loadStats()
    } catch (err) { showToast(err.message, 'error') }
  }

  const deleteRequest = async (id) => {
    if (!confirm('Delete this request?')) return
    try {
      await apiFetch(`/api/requests/${id}`, { method: 'DELETE' })
      showToast('Request deleted')
      await loadRequests()
      await loadStats()
    } catch (err) { showToast(err.message, 'error') }
  }

  // Handle state change for donor form
  const handleDonorStateChange = (state) => {
    setDonorForm({...donorForm, state, city: ''})
    setDonorCities(INDIA_STATES_CITIES[state] || [])
  }

  // Handle state change for request form
  const handleRequestStateChange = (state) => {
    setRequestForm({...requestForm, state, city: ''})
    setRequestCities(INDIA_STATES_CITIES[state] || [])
  }

  useEffect(() => {
    if (!auth) return
    loadStats()
  }, [auth, loadStats])

  useEffect(() => {
    if (!auth) return
    if (page === 'dashboard') {
      loadRequests()
      loadDonors()
    }
    if (page === 'donors') loadDonors()
    if (page === 'requests') {
      loadRequests()
      loadDonors()
    }
  }, [auth, page, loadDonors, loadRequests])

  if (!auth) {
    return (
      <div className="login-screen">
        <div className="login-card">
          <div className="login-logo">
            <span className="drop-icon">🩸</span>
            <h1>BloodSync</h1>
            <p>Admin Dashboard</p>
          </div>

          <div className="auth-switch">
            <button
              type="button"
              className={`auth-switch-btn ${authMode === 'existing' ? 'active' : ''}`}
              onClick={() => {
                setAuthMode('existing')
                setLoginError('')
              }}
            >
              Existing User
            </button>
            <button
              type="button"
              className={`auth-switch-btn ${authMode === 'new' ? 'active' : ''}`}
              onClick={() => {
                setAuthMode('new')
                setLoginError('')
              }}
            >
              New User
            </button>
          </div>

          <form onSubmit={authMode === 'existing' ? login : signup}>
            {loginError && <div className="form-error">{loginError}</div>}

            {authMode === 'existing' ? (
              <>
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
                <button type="button" className="forgot-link" onClick={forgotPassword}>Forgot password?</button>
              </>
            ) : (
              <>
                <div className="form-group">
                  <label>Full Name</label>
                  <input type="text" value={signupForm.name}
                    onChange={e => setSignupForm(f => ({...f, name: e.target.value}))}
                    placeholder="Your name" required />
                </div>
                <div className="form-group">
                  <label>Email</label>
                  <input type="email" value={signupForm.email}
                    onChange={e => setSignupForm(f => ({...f, email: e.target.value}))}
                    placeholder="you@example.com" required />
                </div>
                <div className="form-group">
                  <label>Phone</label>
                  <input type="text" value={signupForm.phone}
                    onChange={e => setSignupForm(f => ({...f, phone: e.target.value}))}
                    placeholder="+919876543210" required />
                </div>
                <div className="form-group">
                  <label>Password</label>
                  <input type="password" value={signupForm.password}
                    onChange={e => setSignupForm(f => ({...f, password: e.target.value}))}
                    placeholder="At least 6 characters" required minLength={6} />
                </div>
                <button type="submit" className="btn-primary btn-block">Create Account</button>
              </>
            )}
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
              <h2>Dashboard Overview</h2>
              <button className="btn-refresh" onClick={loadStats}>↻ Refresh</button>
            </div>
            {stats ? (
              <>
                <div className="stats-grid-large">
                  <div className="stat-card-large" style={{ '--accent': '#3498db' }}>
                    <div className="stat-header">
                      <span className="stat-icon-large">📋</span>
                      <span className="stat-trend">Active Requests</span>
                    </div>
                    <div className="stat-value-large">{stats.pending + stats.notified + stats.matched}</div>
                    <div className="stat-breakdown">
                      <div className="breakdown-item">
                        <span className="breakdown-label">Pending</span>
                        <span className="breakdown-value">{stats.pending}</span>
                      </div>
                      <div className="breakdown-item">
                        <span className="breakdown-label">Notified</span>
                        <span className="breakdown-value">{stats.notified}</span>
                      </div>
                      <div className="breakdown-item">
                        <span className="breakdown-label">Matched</span>
                        <span className="breakdown-value">{stats.matched}</span>
                      </div>
                    </div>
                  </div>

                  <div className="stat-card-large" style={{ '--accent': '#27ae60' }}>
                    <div className="stat-header">
                      <span className="stat-icon-large">✅</span>
                      <span className="stat-trend">Fulfilled</span>
                    </div>
                    <div className="stat-value-large">{stats.fulfilled}</div>
                    <div className="stat-detail">Total successful donations</div>
                  </div>

                  <div className="stat-card-large" style={{ '--accent': '#e74c3c' }}>
                    <div className="stat-header">
                      <span className="stat-icon-large">♥</span>
                      <span className="stat-trend">Registered Donors</span>
                    </div>
                    <div className="stat-value-large">{stats.totalDonors}</div>
                    <div className="stat-breakdown">
                      <div className="breakdown-item">
                        <span className="breakdown-label">Available</span>
                        <span className="breakdown-value" style={{color:'#27ae60'}}>{stats.availableDonors}</span>
                      </div>
                      <div className="breakdown-item">
                        <span className="breakdown-label">Unavailable</span>
                        <span className="breakdown-value" style={{color:'#95a5a6'}}>{stats.totalDonors - stats.availableDonors}</span>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="quick-actions-new">
                  <h3>Quick Actions</h3>
                  <div className="action-grid">
                    <button className="action-card" onClick={() => setShowRequestModal(true)}>
                      <div className="action-icon">⚡</div>
                      <div className="action-label">Create Blood Request</div>
                      <div className="action-desc">Submit new emergency request</div>
                    </button>
                    <button className="action-card" onClick={() => setShowDonorModal(true)}>
                      <div className="action-icon">👤</div>
                      <div className="action-label">Register Donor</div>
                      <div className="action-desc">Add new blood donor</div>
                    </button>
                    <button className="action-card" onClick={() => setPage('requests')}>
                      <div className="action-icon">📊</div>
                      <div className="action-label">View All Requests</div>
                      <div className="action-desc">Manage blood requests</div>
                    </button>
                    <button className="action-card" onClick={() => setPage('donors')}>
                      <div className="action-icon">📇</div>
                      <div className="action-label">Manage Donors</div>
                      <div className="action-desc">View donor database</div>
                    </button>
                  </div>
                </div>

                <div className="dashboard-panels">
                  <section className="dashboard-panel">
                    <div className="panel-head">
                      <h3>Recent Blood Requests</h3>
                      <button className="panel-link" onClick={() => setPage('requests')}>View all</button>
                    </div>
                    {requests.length === 0 ? (
                      <div className="panel-empty">No recent requests yet.</div>
                    ) : (
                      <div className="panel-list">
                        {requests.slice(0, 5).map(r => (
                          <div key={r.id} className="panel-row">
                            <div>
                              <div className="panel-row-title">{r.requesterName}</div>
                              <div className="panel-row-sub">{r.city || 'Location not specified'}</div>
                            </div>
                            <div className="panel-row-right">
                              <span className="blood-badge" style={{ background: bloodGroupColors[r.bloodGroupNeeded] || '#e74c3c' }}>
                                {bgLabel[r.bloodGroupNeeded] || r.bloodGroupNeeded}
                              </span>
                              <span className="badge" style={{ background: statusColor[r.status] || '#95a5a6' }}>{r.status}</span>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </section>

                  <section className="dashboard-panel">
                    <div className="panel-head">
                      <h3>Donor Availability by Group</h3>
                      <button className="panel-link" onClick={() => setPage('donors')}>Manage</button>
                    </div>
                    {Object.keys(bgLabel).map(group => {
                      const total = donors.filter(d => d.bloodGroup === group).length
                      const available = donors.filter(d => d.bloodGroup === group && d.available).length
                      return (
                        <div key={group} className="group-row">
                          <span className="blood-badge" style={{ background: bloodGroupColors[group] || '#e74c3c' }}>{bgLabel[group]}</span>
                          <div className="group-bar-wrap">
                            <div className="group-bar" style={{ width: `${total > 0 ? (available / total) * 100 : 0}%` }} />
                          </div>
                          <span className="group-count">{available}/{total}</span>
                        </div>
                      )
                    })}
                  </section>
                </div>
              </>
            ) : dashboardError ? (
              <div className="loading-state">Unable to load dashboard: {dashboardError}</div>
            ) : (
              <div className="loading-state">Loading dashboard...</div>
            )}
          </div>
        )}

        {/* Requests */}
        {page === 'requests' && (
          <div className="page">
            <div className="page-header">
              <h2>Blood Requests</h2>
              <div style={{display:'flex',gap:'10px'}}>
                <button className="btn-add" onClick={() => setShowRequestModal(true)}>+ Create Request</button>
                <button className="btn-refresh" onClick={loadRequests}>↻ Refresh</button>
              </div>
            </div>
            <div className="request-overview-grid">
              <div className="request-overview-card">
                <div className="request-overview-title">Blood Requests</div>
                <div className="request-overview-value">{requests.length}</div>
                <div className="request-overview-sub">{requests.filter(r => r.status === 'PENDING').length} pending</div>
              </div>
              <div className="request-overview-card">
                <div className="request-overview-title">Available Donors</div>
                <div className="request-overview-value">{donors.filter(d => d.available).length}</div>
                <div className="request-overview-sub">{donors.filter(d => d.available && d.bloodGroup === 'O_POSITIVE').length} matching O+</div>
              </div>
              <div className="request-overview-card">
                <div className="request-overview-title">Nearby Donors</div>
                <div className="request-overview-value">{donors.filter(d => d.available && d.city).length}</div>
                <div className="request-overview-sub">Within 50km radius</div>
              </div>
            </div>

            <div className="request-tabs">
              <button
                className={`request-tab ${requestsTab === 'my-requests' ? 'active' : ''}`}
                onClick={() => setRequestsTab('my-requests')}
              >
                My Blood Requests
              </button>
              <button
                className={`request-tab ${requestsTab === 'available-donors' ? 'active' : ''}`}
                onClick={() => setRequestsTab('available-donors')}
              >
                Available Donors
              </button>
            </div>

            {loading ? (
              <div className="loading-state">Loading...</div>
            ) : requestsTab === 'my-requests' ? (
              requests.length === 0 ? (
                <div className="empty-state">No blood requests found. Create one using the button above!</div>
              ) : (
                <div className="request-card-grid">
                  {requests.map(r => (
                    <div key={r.id} className="request-data-card">
                      <div className="request-data-head">
                        <div>
                          <div className="request-data-name">{r.requesterName}</div>
                          <div className="request-data-city">{r.city || 'Location not specified'}</div>
                        </div>
                        <div className="request-data-units">Units: {r.unitsNeeded}</div>
                      </div>
                      <div className="request-data-meta">
                        <span className="blood-badge" style={{ background: bloodGroupColors[r.bloodGroupNeeded] || '#e74c3c' }}>
                          {bgLabel[r.bloodGroupNeeded] || r.bloodGroupNeeded}
                        </span>
                        <span className="badge" style={{ background: urgencyColor[r.urgency] || '#95a5a6' }}>{r.urgency}</span>
                        <span className="badge" style={{ background: statusColor[r.status] || '#95a5a6' }}>{r.status}</span>
                      </div>
                      <div className="request-data-actions">
                        <select
                          className="status-select"
                          value={r.status}
                          onChange={e => updateRequestStatus(r.id, e.target.value)}
                        >
                          {['PENDING','NOTIFIED','MATCHED','FULFILLED','CANCELLED'].map(s => (
                            <option key={s} value={s}>{s}</option>
                          ))}
                        </select>
                        <button className="btn-del" onClick={() => deleteRequest(r.id)}>✕</button>
                      </div>
                    </div>
                  ))}
                </div>
              )
            ) : (
              donors.filter(d => d.available).length === 0 ? (
                <div className="empty-state">No available donors found.</div>
              ) : (
                <div className="request-card-grid">
                  {donors.filter(d => d.available).map(d => (
                    <div key={d.id} className="request-data-card">
                      <div className="request-data-head">
                        <div>
                          <div className="request-data-name">{d.name}</div>
                          <div className="request-data-city">{d.city || 'Location not specified'}</div>
                        </div>
                        <div className="request-data-units">Donations: {d.totalDonations || 0}</div>
                      </div>
                      <div className="request-data-meta">
                        <span className="blood-badge" style={{ background: bloodGroupColors[d.bloodGroup] || '#e74c3c' }}>
                          {bgLabel[d.bloodGroup] || d.bloodGroup}
                        </span>
                        <span className="badge" style={{ background: '#27ae60' }}>AVAILABLE</span>
                      </div>
                    </div>
                  ))}
                </div>
              )
            )}
          </div>
        )}

        {/* Donors */}
        {page === 'donors' && (
          <div className="page">
            <div className="page-header">
              <h2>Donors</h2>
              <div style={{display:'flex',gap:'10px'}}>
                <button className="btn-add" onClick={() => setShowDonorModal(true)}>+ Add Donor</button>
                <button className="btn-refresh" onClick={loadDonors}>↻ Refresh</button>
              </div>
            </div>
            {loading ? (
              <div className="loading-state">Loading...</div>
            ) : donors.length === 0 ? (
              <div className="empty-state">No donors registered yet. Add one using the button above!</div>
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

      {/* Donor Modal */}
      {showDonorModal && (
        <div className="modal-overlay" onClick={() => setShowDonorModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Register New Donor</h3>
              <button className="modal-close" onClick={() => setShowDonorModal(false)}>✕</button>
            </div>
            <form onSubmit={submitDonor} className="modal-form">
              <div className="form-row">
                <div className="form-group">
                  <label>Full Name *</label>
                  <input required value={donorForm.name} onChange={e => setDonorForm({...donorForm, name: e.target.value})} />
                </div>
                <div className="form-group">
                  <label>Email *</label>
                  <input required type="email" value={donorForm.email} onChange={e => setDonorForm({...donorForm, email: e.target.value})} />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Password *</label>
                  <input required type="password" value={donorForm.password} onChange={e => setDonorForm({...donorForm, password: e.target.value})} />
                </div>
                <div className="form-group">
                  <label>Phone *</label>
                  <input required value={donorForm.phone} placeholder="+919876543210" onChange={e => setDonorForm({...donorForm, phone: e.target.value})} />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Blood Group *</label>
                  <select required value={donorForm.bloodGroup} onChange={e => setDonorForm({...donorForm, bloodGroup: e.target.value})}>
                    {Object.keys(bgLabel).map(bg => <option key={bg} value={bg}>{bgLabel[bg]}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label>Age *</label>
                  <input required type="number" min="18" max="65" value={donorForm.age} onChange={e => setDonorForm({...donorForm, age: e.target.value})} />
                </div>
                <div className="form-group">
                  <label>Gender *</label>
                  <select required value={donorForm.gender} onChange={e => setDonorForm({...donorForm, gender: e.target.value})}>
                    <option>Male</option>
                    <option>Female</option>
                    <option>Other</option>
                  </select>
                </div>
              </div>
              <div className="form-group">
                <label>Address</label>
                <input value={donorForm.address} onChange={e => setDonorForm({...donorForm, address: e.target.value})} />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>State *</label>
                  <select required value={donorForm.state} onChange={e => handleDonorStateChange(e.target.value)}>
                    <option value="">Select State</option>
                    {Object.keys(INDIA_STATES_CITIES).sort().map(state => (
                      <option key={state} value={state}>{state}</option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label>City *</label>
                  <select required value={donorForm.city} onChange={e => setDonorForm({...donorForm, city: e.target.value})} disabled={!donorForm.state}>
                    <option value="">Select City</option>
                    {donorCities.map(city => (
                      <option key={city} value={city}>{city}</option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label>Pincode</label>
                  <input value={donorForm.pincode} onChange={e => setDonorForm({...donorForm, pincode: e.target.value})} />
                </div>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn-secondary" onClick={() => setShowDonorModal(false)}>Cancel</button>
                <button type="submit" className="btn-primary">Register Donor</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Request Modal */}
      {showRequestModal && (
        <div className="modal-overlay" onClick={() => setShowRequestModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Create Blood Request</h3>
              <button className="modal-close" onClick={() => setShowRequestModal(false)}>✕</button>
            </div>
            <form onSubmit={submitRequest} className="modal-form">
              <div className="form-row">
                <div className="form-group">
                  <label>Requester Name *</label>
                  <input required value={requestForm.requesterName} onChange={e => setRequestForm({...requestForm, requesterName: e.target.value})} />
                </div>
                <div className="form-group">
                  <label>Requester Phone *</label>
                  <input required value={requestForm.requesterPhone} placeholder="+919123456789" onChange={e => setRequestForm({...requestForm, requesterPhone: e.target.value})} />
                </div>
              </div>
              <div className="form-group">
                <label>Requester Email</label>
                <input type="email" value={requestForm.requesterEmail} onChange={e => setRequestForm({...requestForm, requesterEmail: e.target.value})} />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Blood Group Needed *</label>
                  <select required value={requestForm.bloodGroupNeeded} onChange={e => setRequestForm({...requestForm, bloodGroupNeeded: e.target.value})}>
                    {Object.keys(bgLabel).map(bg => <option key={bg} value={bg}>{bgLabel[bg]}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label>Units Needed *</label>
                  <input required type="number" min="1" max="10" value={requestForm.unitsNeeded} onChange={e => setRequestForm({...requestForm, unitsNeeded: e.target.value})} />
                </div>
                <div className="form-group">
                  <label>Urgency *</label>
                  <select required value={requestForm.urgency} onChange={e => setRequestForm({...requestForm, urgency: e.target.value})}>
                    <option value="CRITICAL">Critical</option>
                    <option value="HIGH">High</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="LOW">Low</option>
                  </select>
                </div>
              </div>
              <div className="form-group">
                <label>Patient Name</label>
                <input value={requestForm.patientName} onChange={e => setRequestForm({...requestForm, patientName: e.target.value})} />
              </div>
              <div className="form-group">
                <label>Hospital *</label>
                <input required value={requestForm.hospital} onChange={e => setRequestForm({...requestForm, hospital: e.target.value})} />
              </div>
              <div className="form-group">
                <label>Hospital Address</label>
                <input value={requestForm.hospitalAddress} onChange={e => setRequestForm({...requestForm, hospitalAddress: e.target.value})} />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>State *</label>
                  <select required value={requestForm.state} onChange={e => handleRequestStateChange(e.target.value)}>
                    <option value="">Select State</option>
                    {Object.keys(INDIA_STATES_CITIES).sort().map(state => (
                      <option key={state} value={state}>{state}</option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label>City *</label>
                  <select required value={requestForm.city} onChange={e => setRequestForm({...requestForm, city: e.target.value})} disabled={!requestForm.state}>
                    <option value="">Select City</option>
                    {requestCities.map(city => (
                      <option key={city} value={city}>{city}</option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="form-group">
                <label>Notes</label>
                <textarea rows="3" value={requestForm.notes} onChange={e => setRequestForm({...requestForm, notes: e.target.value})} />
              </div>
              <div className="modal-footer">
                <button type="button" className="btn-secondary" onClick={() => setShowRequestModal(false)}>Cancel</button>
                <button type="submit" className="btn-primary">Create Request</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

export default App
