import React, { useState } from 'react';
import notificationService from '../services/notificationService';
import './NotificationForm.css';

const NotificationForm = ({ onSuccess }) => {
  const [formData, setFormData] = useState({
    userId: '',
    type: 'EMAIL',
    message: '',
    scheduleTime: '',
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const payload = {
        ...formData,
        userId: parseInt(formData.userId),
      };

      await notificationService.createNotification(payload);
      setSuccess('Notification created successfully!');
      
      // Reset form
      setFormData({
        userId: '',
        type: 'EMAIL',
        message: '',
        scheduleTime: '',
      });

      if (onSuccess) {
        setTimeout(() => onSuccess(), 1500);
      }
    } catch (err) {
      const errorMessage = err.response?.data?.message || 
                          err.response?.data?.error || 
                          'Failed to create notification';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="notification-form-container">
      <h2>Create New Notification</h2>
      
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <form onSubmit={handleSubmit} className="notification-form">
        <div className="form-group">
          <label htmlFor="userId">User ID *</label>
          <input
            type="number"
            id="userId"
            name="userId"
            value={formData.userId}
            onChange={handleChange}
            required
            placeholder="Enter user ID"
          />
        </div>

        <div className="form-group">
          <label htmlFor="type">Notification Type *</label>
          <select
            id="type"
            name="type"
            value={formData.type}
            onChange={handleChange}
            required
          >
            <option value="EMAIL">Email</option>
            <option value="SMS">SMS</option>
            <option value="PUSH">Push Notification</option>
          </select>
        </div>

        <div className="form-group">
          <label htmlFor="message">Message *</label>
          <textarea
            id="message"
            name="message"
            value={formData.message}
            onChange={handleChange}
            required
            placeholder="Enter notification message"
            rows="4"
          />
          <small className="form-hint">
            Note: Words cannot be repeated more than 3 times
          </small>
        </div>

        <div className="form-group">
          <label htmlFor="scheduleTime">Schedule Time *</label>
          <input
            type="datetime-local"
            id="scheduleTime"
            name="scheduleTime"
            value={formData.scheduleTime}
            onChange={handleChange}
            required
          />
        </div>

        <button 
          type="submit" 
          className="btn btn-primary"
          disabled={loading}
        >
          {loading ? 'Creating...' : 'Create Notification'}
        </button>
      </form>
    </div>
  );
};

export default NotificationForm;