import axios from 'axios';
import { logout } from '../store/actions/authActions';
const baseUrl = 'http://localhost:8081';
export const AUTH_TOKEN = 'token';

export const fetchData = async (endPoint, customError) => {
  try {
    const { data = null } = await axios.get(`${baseUrl}/${endPoint}`);
    return data;
  } catch (error) {
    console.error(error);
    throw customError ? customError : error;
  }
};

/***********    AUTH API:   ***************/
export const signup = async user => {
  try {
    const { data = null } = await axios.post(`${baseUrl}/signup/`, user);
    return data;
  } catch (error) {
    console.error(error);
    throw error.response.data.error;
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
    throw new Error('User credentials are incorrect'); //temp until an error notification will be returned from the server
  }
};

export const getDeploymentRoles = async () => await fetchData('deployment-roles');

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

/***********    DEPLOYMENT API:   ***************/
export const getServices = () => fetchData('service/');
export const getServicesStacks = () => fetchData('services-stack/');
export const getEnvironments = () => fetchData('environment/');
export const getEnvironmentsStacks = () => fetchData('environments-stack/');
export const getDeployableVersionsById = servicesId => fetchData(`deployable-version/multi-service/${servicesId}/`);

export const getLastCommitFromBranch = (branchName, deployableVersionId) => {
  // Double encoding, as nginx is opening the first one
  const encodedBranchNameURI = encodeURIComponent(encodeURIComponent(branchName));
  fetchData(`deployable-version/latest/branch/${encodedBranchNameURI}/repofrom/${deployableVersionId}`);
};

export const getGroups = (envId, serviceId) => fetchData(`group/environment/${envId}/service/${serviceId}`);

export const deploy = async (
  serviceIdsCsv,
  environmentIdsCsv,
  deployableVersionId,
  deploymentMessage,
  isEmergencyDeployment,
) => {
  try {
    // const { data = null } = await axios.post(`${baseUrl}/deployment/`, {
    //   serviceIdsCsv,
    //   environmentIdsCsv,
    //   deployableVersionId,
    //   deploymentMessage,
    //   isEmergencyDeployment,
    // });
    // return data;
  } catch (error) {
    console.error(error);
    throw error;
  }
};

export const deployGroup = async (
  serviceIdsCsv,
  environmentIdsCsv,
  deployableVersionId,
  deploymentMessage,
  groupIdsCsv,
  isEmergencyDeployment,
) => {
  try {
    // const { data = null } = await axios.post(`${baseUrl}/deployment-groups/`, {
    //   serviceIdsCsv,
    //   environmentIdsCsv,
    //   deployableVersionId,
    //   deploymentMessage,
    //   groupIdsCsv,
    //   isEmergencyDeployment,
    // });
    // return data;
  } catch (error) {
    console.error(error);
    throw error;
  }
};
