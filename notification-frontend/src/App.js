import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Dashboard from './components/Dashboard';
import NotificationForm from './components/NotificationForm';
import NotificationList from './components/NotificationList';
import './App.css';

function App() {
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  const handleNotificationCreated = () => {
    setRefreshTrigger((prev) => prev + 1);
  };

  return (
    <Router>
      <div className="App">
        <nav className="navbar">
          <div className="nav-container">
            <div className="nav-brand">
              <h1>📬 Notification Management System</h1>
            </div>
            <ul className="nav-menu">
              <li>
                <Link to="/" className="nav-link">Dashboard</Link>
              </li>
              <li>
                <Link to="/create" className="nav-link">Create Notification</Link>
              </li>
              <li>
                <Link to="/notifications" className="nav-link">View Notifications</Link>
              </li>
            </ul>
          </div>
        </nav>

        <main className="main-content">
          <Routes>
            <Route 
              path="/" 
              element={<Dashboard refreshTrigger={refreshTrigger} />} 
            />
            <Route 
              path="/create" 
              element={<NotificationForm onSuccess={handleNotificationCreated} />} 
            />
            <Route 
              path="/notifications" 
              element={<NotificationList refreshTrigger={refreshTrigger} />} 
            />
          </Routes>
        </main>

        <footer className="footer">
          <p>&copy; 2024 Notification Management System. All rights reserved.</p>
        </footer>
      </div>
    </Router>
  );
}

export default App;