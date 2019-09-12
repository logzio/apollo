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
} from '../actions';
import * as API from '../../api/api';
import { historyBrowser } from '../../utils/history';
import { fetchAndStore, setToCache } from '../../utils/cacheService';
import { cacheKeys } from '../../utils/cacheConfig';
import { appNotification } from '../../common/notification';

export const getServices = () => {
  return async dispatch => {
    dispatch({
      type: GET_SERVICES_REQUEST,
    });
    try {
      const data = await fetchAndStore(cacheKeys.SERVICES, API.getServices, 60 * 6);
      await dispatch({
        type: GET_SERVICES_SUCCESS,
        payload: data,
      });
    } catch (error) {
      await dispatch({
        type: GET_SERVICES_FAILURE,
        error,
      });
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
    }
  };
};

export const getDeployableVersionsById = servicesId => {
  return async dispatch => {
    dispatch({
      type: GET_DEPLOYABLE_VERSION_ID_REQUEST,
    });
    try {
      const data = await fetchAndStore(cacheKeys.DEPLOYABLE_VERSIONS, API.getDeployableVersionsById, 60 * 6, servicesId);
      dispatch({
        type: GET_DEPLOYABLE_VERSION_ID_SUCCESS,
        payload: data,
      });
    } catch (error) {
      dispatch({
        type: GET_DEPLOYABLE_VERSION_ID_FAILURE,
        error,
      });
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
      appNotification(`Commit: ${deployableVersionId} was successfully deployed`, 'smile', 'twoTone');
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
      appNotification(`Commit: ${deployableVersionId} was successfully deployed`, 'smile', 'twoTone');
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
