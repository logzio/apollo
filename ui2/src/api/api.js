import axios from 'axios';
import { logout } from '../store/actions/authActions';

const baseUrl = 'http://localhost:8081';
const AUTH_TOKEN = 'token';

export const signup = async user => {
  try {
    const { data = null } = await axios.post(`${baseUrl}/signup/`, user);
    return data;
  } catch (error) {
    console.error(error);
    throw error.response.data.error;
  }
};

export const getDeploymentRoles = async () => {
  try {
    const { data = null } = await axios.get(`${baseUrl}/deployment-roles/`);
    return data;
  } catch (error) {
    console.log(error.response.status);
    console.error(error);
    throw error;
  }
};

export const login = async user => {
  try {
    const { data = null } = await axios.post(`${baseUrl}/_login/`, user);
    localStorage.setItem(AUTH_TOKEN, data.token);
    return data;
  } catch (error) {
    console.error(error);
    // throw error.response.data.error;
    throw new Error('User credentials are incorrect'); //temp until an error notification will return from the server
  }
};

export const appInit = () => {
  const token = localStorage.getItem(AUTH_TOKEN);
  let loggedIn = false;
  if (token) {
    axios.defaults.headers.common['Authorization'] = token;
    loggedIn = true;
  } else {
    logout();
  }

  return loggedIn;
};

export const appLogout = () => {
  localStorage.removeItem(AUTH_TOKEN);
};

export const getServices = async () => {
  try {
    const response = await axios.get(`${baseUrl}/service/`);
    return response.data;
  } catch (error) {
    console.error(error);
    throw error;
  }
};
