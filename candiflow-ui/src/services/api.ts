import axios, { AxiosInstance } from 'axios';
import Constants from 'expo-constants';

// API URL from environment or default to relative path
// Utiliser une chaîne vide pour que le chemin soit relatif à la racine du serveur
const API_URL = Constants.expoConfig?.extra?.apiUrl || '';

console.log('Using API URL:', API_URL);

// Create API instance
const api: AxiosInstance = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Simple error handler
api.interceptors.response.use(
  response => response,
  error => {
    console.error('API Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

export default api;
