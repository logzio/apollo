import axios from 'axios';
import { logout } from '../store/actions/authActions';
import { routes } from './routesConfig';
const baseUrl = 'http://localhost:8081/';
export const AUTH_TOKEN = 'token';

export const fetchData = async (endPoint, customError) => {
  try {
    const { data = null } = await axios.get(`${baseUrl}${endPoint}`);
    return data;
  } catch (error) {
    console.error(error);
    throw customError ? customError : error;
  }
};

/***********    AUTH API:   ***************/
export const signup = async user => {
  try {
    const { data = null } = await axios.post(`${baseUrl}${routes.SIGNUP}`, user);
    return data;
  } catch (error) {
    console.error(error);
    throw error.response.data.error;
  }
};

export const login = async user => {
  try {
    const { data = null } = await axios.post(`${baseUrl}${routes.LOGIN}`, user);
    localStorage.setItem(AUTH_TOKEN, data.token);
    return data;
  } catch (error) {
    console.error(error);
    // throw error.response.data.error;
    throw new Error('User credentials are incorrect'); //temp until an error notification will be returned from the server
  }
};

export const getDeploymentRoles = async () => await fetchData(routes.DEPLOYMENT_ROLES);

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
export const getServices = () => fetchData(routes.SERVICES);
export const getServicesStacks = () => fetchData(routes.SERVICES_STACKS);
export const getEnvironments = () => fetchData(routes.ENVIRONMENTS);
export const getEnvironmentsStacks = () => fetchData(routes.ENVIRONMENTS_STACKS);
export const getDeployableVersionsById = servicesId => fetchData(`${routes.DEPLOYABLE_VERSIONS}${servicesId}/`);

export const getLastCommitFromBranch = (branchName, deployableVersionId) => {
  // Double encoding, as nginx is opening the first one
  const encodedBranchNameURI = encodeURIComponent(encodeURIComponent(branchName));
  fetchData(`${routes.DEPLOYABLE_VERSIONS_LATEST_BRANCH}${encodedBranchNameURI}/repofrom/${deployableVersionId}`);
};

export const getGroups = (envId, serviceId) => fetchData(`${routes.GROUP_ENVIRONMENT}${envId}/${routes.SERVICES}${serviceId}`);

export const deploy = async (
  serviceIdsCsv,
  environmentIdsCsv,
  deployableVersionId,
  deploymentMessage,
  isEmergencyDeployment,
) => {
  try {
    // const { data = null } = await axios.post(`${baseUrl}${routes.DEPLOYMENT}`, {
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
    // const { data = null } = await axios.post(`${baseUrl}${routes.GROUPS_DEPLOYMENT}`, {
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
