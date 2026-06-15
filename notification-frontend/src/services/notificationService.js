import axios from 'axios';

const API_BASE_URL = 'http://localhost:8181/notification/api';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor for error handling
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

const notificationService = {
  createNotification: async (notificationData) => {
    const response = await axiosInstance.post('/notifications', notificationData);
    return response.data;
  },

  getNotifications: async (params = {}) => {
    const { status, type, page = 0, size = 10 } = params;
    const queryParams = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });

    if (status) queryParams.append('status', status);
    if (type) queryParams.append('type', type);

    const response = await axiosInstance.get(`/notifications?${queryParams}`);
    return response.data;
  },

  retryNotification: async (id) => {
    const response = await axiosInstance.post(`/notifications/${id}/retry`);
    return response.data;
  },

  getDashboard: async () => {
    const response = await axiosInstance.get('/dashboard');
    return response.data;
  },
};

export default notificationService;