import * as API from '../../api/api';
import { historyBrowser } from '../../utils/history';
import { fetchAndStore, setToCache } from '../../utils/cacheService';
import { cacheKeys } from '../../utils/cacheConfig';
import { errorHandler } from '../../utils/errorHandler';
import { appNotification } from '../../common/notification';
import {
  GET_SERVICES_REQUEST,
  GET_SERVICES_SUCCESS,
  GET_SERVICES_FAILURE,
  GET_SERVICES_STACK_REQUEST,
  GET_SERVICES_STACK_SUCCESS,
  GET_SERVICES_STACK_FAILURE,
  GET_ENV_STACK_REQUEST,
  GET_ENV_STACK_SUCCESS,
  GET_ENV_STACK_FAILURE,
  GET_ENV_REQUEST,
  GET_ENV_SUCCESS,
  GET_ENV_FAILURE,
  GET_DEPLOYABLE_VERSION_ID_REQUEST,
  GET_DEPLOYABLE_VERSION_ID_SUCCESS,
  GET_DEPLOYABLE_VERSION_ID_FAILURE,
  GET_DEPLOYABLE_VERSIONS_ID_REQUEST,
  GET_DEPLOYABLE_VERSIONS_ID_SUCCESS,
  GET_DEPLOYABLE_VERSIONS_ID_FAILURE,
  GET_BRANCH_LATEST_VERSION_REQUEST,
  GET_BRANCH_LATEST_VERSION_SUCCESS,
  GET_BRANCH_LATEST_VERSION_FAILURE,
  GET_GROUPS_REQUEST,
  GET_GROUPS_SUCCESS,
  GET_GROUPS_FAILURE,
  NEW_DEPLOYMENT_REQUEST,
  NEW_DEPLOYMENT_SUCCESS,
  NEW_DEPLOYMENT_FAILURE,
  NEW_GROUP_DEPLOYMENT_REQUEST,
  NEW_GROUP_DEPLOYMENT_SUCCESS,
  NEW_GROUP_DEPLOYMENT_FAILURE,
  SELECT_SERVICES,
  SELECT_ENVIRONMENTS,
  SELECT_GROUPS,
  SELECT_VERSION,
  GET_ONGOING_DEPLOYMENT_FAILURE,
  GET_ONGOING_DEPLOYMENT_REQUEST,
  GET_ONGOING_DEPLOYMENT_SUCCESS,
  GET_CONTAINERS_REQUEST,
  GET_CONTAINERS_FAILURE,
  GET_CONTAINERS_SUCCESS,
  DELETE_DEPLOYMENT_REQUEST,
  DELETE_DEPLOYMENT_SUCCESS,
  DELETE_DEPLOYMENT_FAILURE,
  GET_GROUP_CONTAINERS_REQUEST,
  GET_GROUP_CONTAINERS_SUCCESS,
  GET_GROUP_CONTAINERS_FAILURE,
  GET_DEPLOYMENT_HISTORY_REQUEST,
  GET_DEPLOYMENT_HISTORY_SUCCESS,
  GET_DEPLOYMENT_HISTORY_FAILURE,
  GET_ENV_STATUS_REQUEST,
  GET_ENV_STATUS_SUCCESS,
  GET_ENV_STATUS_FAILURE,
  GET_DEPLOYMENT_REQUEST,
  GET_DEPLOYMENT_SUCCESS,
  GET_DEPLOYMENT_FAILURE,
} from './index';
import { ongoing as mock } from './mock';

export const getServices = () => {
  return async dispatch => {
    dispatch({
      type: GET_SERVICES_REQUEST,
    });
    try {
      const data = await fetchAndStore(cacheKeys.SERVICES, API.getServices, 60 * 6);
      dispatch({
        type: GET_SERVICES_SUCCESS,
        payload: data,
      });
    } catch (error) {
      dispatch({
        type: GET_SERVICES_FAILURE,
        error,
      });
      errorHandler(error);
    }
  };
};

export const getServicesStacks = () => {
  return async dispatch => {
    dispatch({
      type: GET_SERVICES_STACK_REQUEST,
    });
    try {
      const data = await fetchAndStore(cacheKeys.SERVICES_STACKS, API.getServicesStacks, 60 * 6);
      dispatch({
        type: GET_SERVICES_STACK_SUCCESS,
        payload: data,
      });
    } catch (error) {
      dispatch({
        type: GET_SERVICES_STACK_FAILURE,
        error,
      });
      errorHandler(error);
    }
  };
};

export const getEnvironments = () => {
  return async dispatch => {
    dispatch({
      type: GET_ENV_REQUEST,
    });
    try {
      const data = await fetchAndStore(cacheKeys.ENVIRONMENTS, API.getEnvironments, 60 * 6);
      dispatch({
        type: GET_ENV_SUCCESS,
        payload: data,
      });
    } catch (error) {
      dispatch({
        type: GET_ENV_FAILURE,
        error,
      });
      errorHandler(error);
    }
  };
};

export const getEnvironmentsStacks = () => {
  return async dispatch => {
    dispatch({
      type: GET_ENV_STACK_REQUEST,
    });
    try {
      const data = await fetchAndStore(cacheKeys.ENVIRONMENTS_STACKS, API.getEnvironmentsStacks, 60 * 6);
      dispatch({
        type: GET_ENV_STACK_SUCCESS,
        payload: data,
      });
    } catch (error) {
      dispatch({
        type: GET_ENV_STACK_FAILURE,
        error,
      });
      errorHandler(error);
    }
  };
};

export const getDeployableVersionsById = servicesId => {
  return async dispatch => {
    dispatch({
      type: GET_DEPLOYABLE_VERSIONS_ID_REQUEST,
    });
    try {
      const data = await fetchAndStore(cacheKeys.DEPLOYABLE_VERSIONS, API.getDeployableVersionsById, 60 * 6, servicesId);
      dispatch({
        type: GET_DEPLOYABLE_VERSIONS_ID_SUCCESS,
        payload: data,
      });
    } catch (error) {
      dispatch({
        type: GET_DEPLOYABLE_VERSIONS_ID_FAILURE,
        error,
      });
      errorHandler(error);
    }
  };
};

//Supply one of the service deployable versions ID's
export const getLastCommitFromBranch = (branchName, deployableVersionId) => {
  return async dispatch => {
    dispatch({
      type: GET_BRANCH_LATEST_VERSION_REQUEST,
    });
    try {
      const data = await API.getLastCommitFromBranch(branchName, deployableVersionId);
      dispatch({
        type: GET_BRANCH_LATEST_VERSION_SUCCESS,
        payload: data,
      });
    } catch (error) {
      dispatch({
        type: GET_BRANCH_LATEST_VERSION_FAILURE,
        error,
      });
      errorHandler(error);
    }
  };
};

export const getGroups = (environmentId, serviceId) => {
  return async dispatch => {
    dispatch({
      type: GET_GROUPS_REQUEST,
    });
    try {
      const data = await API.getGroups(environmentId, serviceId);
      dispatch({
        type: GET_GROUPS_SUCCESS,
        payload: data,
      });
    } catch (error) {
      dispatch({
        type: GET_GROUPS_FAILURE,
        error,
      });
      errorHandler(error);
    }
  };
};

export const deploy = (
  serviceIdsCsv,
  environmentIdsCsv,
  deployableVersionId,
  deploymentMessage,
  isEmergencyDeployment,
) => {
  return async dispatch => {
    dispatch({
      type: NEW_DEPLOYMENT_REQUEST,
    });
    try {
      //Temp, so I wouldn't deploy!
      // const data = await API.deploy(
      //   serviceIdsCsv,
      //   environmentIdsCsv,
      //   deployableVersionId,
      //   deploymentMessage,
      //   isEmergencyDeployment,
      // );
      appNotification(`Commit: ${deployableVersionId} was successfully deployed`, '', 'smile', 'twoTone');
      historyBrowser.push({
        pathname: '/deployment/ongoing',
      });
      dispatch({
        type: NEW_DEPLOYMENT_SUCCESS,
        // payload: data,
        payload: 'temp',
      });
    } catch (error) {
      dispatch({
        type: NEW_DEPLOYMENT_FAILURE,
        error,
      });
      errorHandler(error);
    }
  };
};

export const deployGroup = (
  serviceIdsCsv,
  environmentIdsCsv,
  deployableVersionId,
  deploymentMessage,
  groupIdsCsv,
  isEmergencyDeployment,
) => {
  return async dispatch => {
    dispatch({
      type: NEW_GROUP_DEPLOYMENT_REQUEST,
    });
    try {
      //Temp, so I wouldn't deploy!
      // const data = await API.deployGroup(
      //   serviceIdsCsv,
      //   environmentIdsCsv,
      //   deployableVersionId,
      //   deploymentMessage,
      // groupIdsCsv,
      //   isEmergencyDeployment,
      // );
      appNotification(`Commit: ${deployableVersionId} was successfully deployed`, '', 'smile', 'twoTone');
      historyBrowser.push({
        pathname: '/deployment/ongoing',
      });
      dispatch({
        type: NEW_GROUP_DEPLOYMENT_SUCCESS,
        // payload: data,
        payload: 'temp',
      });
    } catch (error) {
      dispatch({
        type: NEW_GROUP_DEPLOYMENT_FAILURE,
        error,
      });
      errorHandler(error);
    }
  };
};

export const selectServices = selectServices => {
  setToCache(cacheKeys.SELECTED_SERVICES, selectServices, 60 * 1);
  return dispatch => {
    dispatch({
      type: SELECT_SERVICES,
      payload: selectServices,
    });
  };
};

export const selectEnvironments = environments => {
  setToCache(cacheKeys.SELECTED_ENVIRONMENTS, environments, 60 * 1);
  return dispatch => {
    dispatch({
      type: SELECT_ENVIRONMENTS,
      payload: environments,
    });
  };
};

export const selectGroups = groups => {
  setToCache(cacheKeys.SELECTED_GROUPS, groups, 60 * 1);
  return dispatch => {
    dispatch({
      type: SELECT_GROUPS,
      payload: groups,
    });
  };
};

export const selectVersion = version => {
  setToCache(cacheKeys.SELECTED_VERSION, version, 60 * 1);
  return dispatch => {
    dispatch({
      type: SELECT_VERSION,
      payload: version,
    });
  };
};

export const getOngoingDeployments = () => {
  return async dispatch => {
    dispatch({
      type: GET_ONGOING_DEPLOYMENT_REQUEST,
    });
    try {
      const data = await API.getOngoingDeployments();
      dispatch({
        type: GET_ONGOING_DEPLOYMENT_SUCCESS,
        payload: data,
        // payload: mock,
      });
    } catch (error) {
      dispatch({
        type: GET_ONGOING_DEPLOYMENT_FAILURE,
        error,
      });
      errorHandler(error);
    }
  };
};

export const getContainers = (environmentId, serviceId) => {
  return async dispatch => {
    dispatch({
      type: GET_CONTAINERS_REQUEST,
    });
    try {
      const lastCreatedPod = await API.getLatestCreatedPod(environmentId, serviceId);
      const containers = await API.getContainers(environmentId, lastCreatedPod);
      dispatch({
        type: GET_CONTAINERS_SUCCESS,
        payload: { containers, lastCreatedPod },
      });
    } catch (error) {
      dispatch({
        type: GET_CONTAINERS_FAILURE,
        error,
      });
      errorHandler(error);
    }
  };
};

export const getGroupContainers = (environmentId, serviceId, groupName) => {
  return async dispatch => {
    dispatch({
      type: GET_GROUP_CONTAINERS_REQUEST,
    });
    try {
      const lastCreatedPod = await API.getLatestCreatedGroupPod(environmentId, serviceId, groupName);
      const containers = await API.getContainers(environmentId, lastCreatedPod);
      dispatch({
        type: GET_GROUP_CONTAINERS_SUCCESS,
        payload: { containers, lastCreatedPod },
      });
    } catch (error) {
      dispatch({
        type: GET_GROUP_CONTAINERS_FAILURE,
        error,
      });
      errorHandler(error);
    }
  };
};

export const revertDeployment = deploymentId => {
  return async dispatch => {
    dispatch({
      type: DELETE_DEPLOYMENT_REQUEST,
    });
    try {
      await API.revertDeployment(deploymentId);
      appNotification(`Successfully revert deployment id: ${deploymentId}`);
      dispatch({
        type: DELETE_DEPLOYMENT_SUCCESS,
      });
    } catch (error) {
      dispatch({
        type: DELETE_DEPLOYMENT_FAILURE,
        error,
      });
      errorHandler(error);
    }
  };
};

export const getDeploymentHistory = (descending, pageNumber, pageSize, searchTerm) => {
  return async dispatch => {
    dispatch({
      type: GET_DEPLOYMENT_HISTORY_REQUEST,
    });
    try {
      const data = await API.getDeploymentHistory(descending, pageNumber, pageSize, searchTerm);
      dispatch({
        type: GET_DEPLOYMENT_HISTORY_SUCCESS,
        payload: data,
      });
    } catch (error) {
      dispatch({
        type: GET_DEPLOYMENT_HISTORY_FAILURE,
        error,
      });
      errorHandler(error);
    }
  };
};

export const getDeployableVersionById = deployableVersionId => {
  return async dispatch => {
    dispatch({
      type: GET_DEPLOYABLE_VERSION_ID_REQUEST,
    });
    try {
      const data = await API.getDeployableVersionById(deployableVersionId);
      dispatch({
        type: GET_DEPLOYABLE_VERSION_ID_SUCCESS,
        payload: data,
      });
    } catch (error) {
      dispatch({
        type: GET_DEPLOYABLE_VERSION_ID_FAILURE,
        error,
      });
      errorHandler(error);
    }
  };
};

export const getDeploymentEnvStatus = deploymentId => {
  return async dispatch => {
    dispatch({
      type: GET_ENV_STATUS_REQUEST,
    });
    try {
      const data = await API.getDeploymentEnvStatus(deploymentId);
      dispatch({
        type: GET_ENV_STATUS_SUCCESS,
        payload: data,
      });
    } catch (error) {
      dispatch({
        type: GET_ENV_STATUS_FAILURE,
        error,
      });
      errorHandler(error); //TODO ERROR FOR MODALS!
    }
  };
};

export const getDeploymentById = deploymentId => {
  return async dispatch => {
    dispatch({
      type: GET_DEPLOYMENT_REQUEST,
    });
    try {
      const data = await API.getDeploymentById(deploymentId);
      dispatch({
        type: GET_DEPLOYMENT_SUCCESS,
        payload: data,
      });
    } catch (error) {
      dispatch({
        type: GET_DEPLOYMENT_FAILURE,
        error,
      });
      errorHandler(error);
    }
  };
};
