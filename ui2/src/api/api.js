import axios from 'axios';
import { logout } from '../store/actions/authActions';

const baseUrl = 'http://localhost:8081';
export const AUTH_TOKEN = 'token';

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

export const getAuthToken = () => localStorage.getItem(AUTH_TOKEN);

export const appInit = () => {
  const token = getAuthToken();
  if (token) {
    axios.defaults.headers.common['Authorization'] = token;
  } else {
    logout();
  }

  return !!token;
};

export const appLogout = () => {
  localStorage.removeItem(AUTH_TOKEN);
};

export const getServices = async () => {
  try {
    const { data = null } = await axios.get(`${baseUrl}/service/`);
    return data;
  } catch (error) {
    console.error(error);
    throw error;
  }
};

export const getServicesStack = async () => {
  try {
    const { data = null } = await axios.get(`${baseUrl}/services-stack/`);
    return data;
  } catch (error) {
    console.error(error);
    throw error;
  }
};

export const getEnvironment = async () => {
  try {
    const { data = null } = await axios.get(`${baseUrl}/environment/`);
    return data;
  } catch (error) {
    console.error(error);
    throw error;
  }
};

export const getEnvironmentsStack = async () => {
  try {
    const { data = null } = await axios.get(`${baseUrl}/environments-stack/`);
    return data;
  } catch (error) {
    console.error(error);
    throw error;
  }
};

export const getDeployableVersionById = async servicesId => {
  try {
    const { data = null } = await axios.get(`${baseUrl}/deployable-version/multi-service/${servicesId}`);
    return data;
  } catch (error) {
    console.error(error);
    throw error;
  }
};

export const getDeployableVersionBySha = async gitCommitSha => {
  try {
    const { data = null } = await axios.get(`${baseUrl}/deployable-version/sha/${gitCommitSha}`);
    return data;
  } catch (error) {
    console.error(error);
    throw error;
  }
};

export const getLastCommitFromBranch = async (branchName, deployableVersionId) => {
  try {
    const { data = null } = await axios.get(`${baseUrl}/deployable-version/latest/branch/${encodeURIComponent(encodeURIComponent(branchName))}/repofrom/${deployableVersionId}`);
    return data;
  } catch (error) {
    console.error(error);
    throw error;
  }
};
