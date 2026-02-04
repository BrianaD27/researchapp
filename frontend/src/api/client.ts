import axios from 'axios';

// Create an Axios instance with default configuration to be used for all API requests
const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api', // Adjust the base URL as needed
  headers: {
    'Content-Type': 'application/json',
  },
});

// TODO: Add interceptors for for authentication tokens

// Add a response interceptor to handle unauthorized access globally
apiClient.interceptors.request.use(
    (response) => response,
    (error) => {
        if (error.response?.staus === 401){
            // Handle unauthorized access, e.g., redirect to login page
            console.error('Unauthorized access - redirecting to login');
        }
        return Promise.reject(error);
    }
);

export default apiClient;