import React, { useState, useEffect } from 'react';
import notificationService from '../services/notificationService';
import './Dashboard.css';

const Dashboard = ({ refreshTrigger }) => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchDashboard();
  }, [refreshTrigger]);

  const fetchDashboard = async () => {
    setLoading(true);
    setError('');

    try {
      const data = await notificationService.getDashboard();
      setStats(data);
    } catch (err) {
      setError('Failed to fetch dashboard statistics');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="loading">Loading dashboard...</div>;
  }

  if (error) {
    return <div className="alert alert-error">{error}</div>;
  }

  if (!stats) {
    return null;
  }

  const successRate = stats.totalNotifications > 0
    ? ((stats.sentCount / stats.totalNotifications) * 100).toFixed(1)
    : 0;

  const failureRate = stats.totalNotifications > 0
    ? ((stats.failedCount / stats.totalNotifications) * 100).toFixed(1)
    : 0;

  return (
    <div className="dashboard-container">
      <h2>Dashboard</h2>

      <div className="stats-grid">
        <div className="stat-card stat-total">
          <div className="stat-icon">📊</div>
          <div className="stat-content">
            <div className="stat-value">{stats.totalNotifications}</div>
            <div className="stat-label">Total Notifications</div>
          </div>
        </div>

        <div className="stat-card stat-success">
          <div className="stat-icon">✅</div>
          <div className="stat-content">
            <div className="stat-value">{stats.sentCount}</div>
            <div className="stat-label">Sent</div>
            <div className="stat-percentage">{successRate}%</div>
          </div>
        </div>

        <div className="stat-card stat-failed">
          <div className="stat-icon">❌</div>
          <div className="stat-content">
            <div className="stat-value">{stats.failedCount}</div>
            <div className="stat-label">Failed</div>
            <div className="stat-percentage">{failureRate}%</div>
          </div>
        </div>

        <div className="stat-card stat-pending">
          <div className="stat-icon">⏳</div>
          <div className="stat-content">
            <div className="stat-value">{stats.pendingCount}</div>
            <div className="stat-label">Pending</div>
          </div>
        </div>

        <div className="stat-card stat-retry">
          <div className="stat-icon">🔄</div>
          <div className="stat-content">
            <div className="stat-value">{stats.retryCount}</div>
            <div className="stat-label">Retried</div>
          </div>
        </div>
      </div>

      <div className="type-wise-stats">
        <h3>Type-wise Distribution</h3>
        <div className="type-stats-grid">
          {Object.entries(stats.typeWiseStatistics || {}).map(([type, count]) => (
            <div key={type} className="type-stat-card">
              <div className="type-stat-header">{type}</div>
              <div className="type-stat-value">{count}</div>
            </div>
          ))}
        </div>
      </div>

      <button onClick={fetchDashboard} className="btn btn-primary refresh-btn">
        Refresh Dashboard
      </button>
    </div>
  );
};

export default Dashboard;