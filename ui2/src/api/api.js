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

export const getServices = async () => await fetchData('service/');
<<<<<<< HEAD
export const getServicesStacks = async () => await fetchData('services-stack/');
export const getEnvironments = async () => await fetchData('environment/');
export const getEnvironmentsStacks = async () => await fetchData('environments-stack/');
=======
export const getServicesStack = async () => await fetchData('services-stack/');
export const getEnvironment = async () => await fetchData('environment/');
export const getEnvironmentsStack = async () => await fetchData('environments-stack/');
>>>>>>> fa90210c1fd1c64f417f792029d84ca8e926c6b9
export const getDeployableVersionById = async servicesId =>
  await fetchData(`deployable-version/multi-service/${servicesId}/`);
export const getDeployableVersionBySha = async gitCommitSha =>
  await fetchData(`deployable-version/sha/${gitCommitSha}/`);
// Double encoding, as nginx is opening the first one
export const getLastCommitFromBranch = async (branchName, deployableVersionId) =>
  await fetchData(
    `deployable-version/latest/branch/${encodeURIComponent(
      encodeURIComponent(branchName),
    )}/repofrom/${deployableVersionId}`,
  );
export const getGroups = async (environmentId, serviceId) =>
  await fetchData(`group/environment/${environmentId}/service/${serviceId}`);

export const deploy = async newDeployment => {
  try {
    const { data = null } = await axios.post(`${baseUrl}/deployment/`, {
      serviceIdsCsv: newDeployment,
      environmentIdsCsv: newDeployment,
      deployableVersionId: newDeployment,
      deploymentMessage: newDeployment,
    });
    return data;
  } catch (error) {
    console.error(error);
    throw error;
  }
};
