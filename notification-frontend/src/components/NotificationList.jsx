import React, { useState, useEffect } from 'react';
import notificationService from '../services/notificationService';
import Pagination from './Pagination';
import './NotificationList.css';

const NotificationList = ({ refreshTrigger }) => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [filters, setFilters] = useState({
    status: '',
    type: '',
    page: 0,
    size: 10,
  });
  const [pageInfo, setPageInfo] = useState({
    totalElements: 0,
    totalPages: 0,
    currentPage: 0,
  });

  useEffect(() => {
    fetchNotifications();
  }, [filters, refreshTrigger]);

  const fetchNotifications = async () => {
    setLoading(true);
    setError('');

    try {
      const response = await notificationService.getNotifications(filters);
      setNotifications(response.content);
      setPageInfo({
        totalElements: response.totalElements,
        totalPages: response.totalPages,
        currentPage: response.pageNumber,
      });
    } catch (err) {
      setError('Failed to fetch notifications');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleRetry = async (id) => {
    try {
      await notificationService.retryNotification(id);
      alert('Retry initiated successfully!');
      fetchNotifications();
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Failed to retry notification';
      alert(errorMessage);
    }
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters((prev) => ({
      ...prev,
      [name]: value,
      page: 0, // Reset to first page on filter change
    }));
  };

  const handlePageChange = (newPage) => {
    setFilters((prev) => ({
      ...prev,
      page: newPage,
    }));
  };

  const getStatusClass = (status) => {
    const statusClasses = {
      SENT: 'status-success',
      FAILED: 'status-error',
      PENDING: 'status-warning',
      RETRYING: 'status-info',
    };
    return statusClasses[status] || '';
  };

  return (
    <div className="notification-list-container">
      <h2>Notifications</h2>

      {/* Filters */}
      <div className="filters">
        <div className="filter-group">
          <label htmlFor="statusFilter">Status:</label>
          <select
            id="statusFilter"
            name="status"
            value={filters.status}
            onChange={handleFilterChange}
          >
            <option value="">All</option>
            <option value="PENDING">Pending</option>
            <option value="SENT">Sent</option>
            <option value="FAILED">Failed</option>
            <option value="RETRYING">Retrying</option>
          </select>
        </div>

        <div className="filter-group">
          <label htmlFor="typeFilter">Type:</label>
          <select
            id="typeFilter"
            name="type"
            value={filters.type}
            onChange={handleFilterChange}
          >
            <option value="">All</option>
            <option value="EMAIL">Email</option>
            <option value="SMS">SMS</option>
            <option value="PUSH">Push</option>
          </select>
        </div>

        <button onClick={fetchNotifications} className="btn btn-secondary">
          Refresh
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="loading">Loading notifications...</div>
      ) : (
        <>
          <div className="table-container">
            <table className="notification-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>User ID</th>
                  <th>Type</th>
                  <th>Message</th>
                  <th>Status</th>
                  <th>Retry Count</th>
                  <th>Created At</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {notifications.length === 0 ? (
                  <tr>
                    <td colSpan="8" className="text-center">
                      No notifications found
                    </td>
                  </tr>
                ) : (
                  notifications.map((notification) => (
                    <tr key={notification.id}>
                      <td>{notification.id}</td>
                      <td>{notification.userId}</td>
                      <td>
                        <span className={`badge badge-${notification.type.toLowerCase()}`}>
                          {notification.type}
                        </span>
                      </td>
                      <td className="message-cell" title={notification.message}>
                        {notification.message.length > 50
                          ? `${notification.message.substring(0, 50)}...`
                          : notification.message}
                      </td>
                      <td>
                        <span className={`status-badge ${getStatusClass(notification.status)}`}>
                          {notification.status}
                        </span>
                      </td>
                      <td className="text-center">{notification.retryCount}</td>
                      <td>{new Date(notification.createdAt).toLocaleString()}</td>
                      <td>
                        {notification.status === 'FAILED' && 
                         notification.retryCount < 3 && (
                          <button
                            onClick={() => handleRetry(notification.id)}
                            className="btn btn-retry"
                          >
                            Retry
                          </button>
                        )}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          <Pagination
            currentPage={pageInfo.currentPage}
            totalPages={pageInfo.totalPages}
            onPageChange={handlePageChange}
          />

          <div className="result-info">
            Showing {notifications.length} of {pageInfo.totalElements} notifications
          </div>
        </>
      )}
    </div>
  );
};

export default NotificationList;