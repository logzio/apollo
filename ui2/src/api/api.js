import axios from 'axios';
import { logout } from '../store/actions/authActions';

const baseUrl = 'http://localhost:8081';
export const wsUrl = `${document.location.protocol === 'https:' ? 'wss' : 'ws'}://${document.location.host}/ws`;
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

export const addUserRole = async (deploymentRoleId, userEmail) => {
  try {
    const { data = null } = await axios.post(`${baseUrl}/deployment-roles/add-user`, { deploymentRoleId, userEmail });
    return data;
  } catch (error) {
    console.error(error);
    throw new Error("User doesn't have the required role"); //TEMP - depend on server side
  }
};

export const login = async user => {
  try {
    const { data = null } = await axios.post(`${baseUrl}/_login/`, user);
    localStorage.setItem(AUTH_TOKEN, data.token);
    return data;
  } catch (error) {
    console.error(error);
    throw error;
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

/***********    NEW DEPLOYMENT API:   ***************/
export const getServices = async () => await fetchData('service/');
export const getServicesStacks = async () => await fetchData('services-stack/');
export const getEnvironments = async () => await fetchData('environment/');
export const getEnvironmentsStacks = async () => await fetchData('environments-stack/');
export const getDeployableVersionsById = async servicesId =>
  await fetchData(`deployable-version/multi-service/${servicesId}/`);

// Double encoding, as nginx is opening the first one
export const getLastCommitFromBranch = async (branchName, deployableVersionId) =>
  await fetchData(
    `deployable-version/latest/branch/${encodeURIComponent(
      encodeURIComponent(branchName),
    )}/repofrom/${deployableVersionId}`,
  );
export const getGroups = async (environmentId, serviceId) =>
  await fetchData(`group/environment/${environmentId}/service/${serviceId}`);

export const deploy = async (
  serviceIdsCsv,
  environmentIdsCsv,
  deployableVersionId,
  deploymentMessage,
  isEmergencyDeployment,
) => {
  try {
    const { data = null } = await axios.post(`${baseUrl}/deployment/`, {
      serviceIdsCsv: serviceIdsCsv,
      environmentIdsCsv: environmentIdsCsv,
      deployableVersionId: deployableVersionId,
      deploymentMessage: deploymentMessage,
      isEmergencyDeployment: isEmergencyDeployment,
    });
    return data;
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
    const { data = null } = await axios.post(`${baseUrl}/deployment-groups/`, {
      serviceIdsCsv: serviceIdsCsv,
      environmentIdsCsv: environmentIdsCsv,
      deployableVersionId: deployableVersionId,
      deploymentMessage: deploymentMessage,
      groupIdsCsv: groupIdsCsv,
      isEmergencyDeployment: isEmergencyDeployment,
    });
    return data;
  } catch (error) {
    console.error(error);
    throw error;
  }
};

/***********    ONGOING DEPLOYMENT API:   ***************/
export const getOngoingDeployments = async () => await fetchData('running-and-just-finished-deployments');
export const getLatestCreatedPod = async (environmentId, serviceId) =>
  await fetchData(`status/environment/${environmentId}/service/${serviceId}/latestpod`);
export const getLatestCreatedGroupPod = async (environmentId, serviceId, groupName) =>
  await fetchData(`status/environment/${environmentId}/service/${serviceId}/group/${groupName}/latestpod`);
export const getContainers = async (environmentId, podName) =>
  await fetchData(`status/environment/${environmentId}/pod/${podName}/containers`);

export const revertDeployment = async deploymentId => {
  try {
    await axios.delete(`${baseUrl}/deployment/${deploymentId}/`);
  } catch (error) {
    console.error(error);
    throw error;
  }
};

export const getLiveLogsWebSocketUrl = (lastCreatedPod, container, environmentId) =>
  `${wsUrl}/logs/pod/${lastCreatedPod}/container/${container}?environment=${environmentId}`;

/***********    HISTORY DEPLOYMENT API:   ***************/

export const getDeploymentHistory = async (descending, pageNumber, pageSize, searchTerm, sortByColName) => {
  try {
    const { data = null } = await axios.post(`${baseUrl}/deployment-history`, {
      descending,
      pageNumber,
      pageSize,
      searchTerm,
      colName: sortByColName,
    });
    return data;
  } catch (error) {
    console.error(error);
    throw error;
  }
};
export const getDeployableVersionById = async deployableVersionId =>
  await fetchData(`deployable-version/${deployableVersionId}`);
export const getDeploymentEnvStatus = async deploymentId => await fetchData(`deployment/${deploymentId}/envstatus`);
export const getDeploymentById = async deploymentId => await fetchData(`deployment/${deploymentId}/`);
export const getAllGroups = async () => await fetchData(`group/`);
