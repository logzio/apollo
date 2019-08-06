import {
  GET_SERVICES_REQUEST,
  GET_SERVICES_SUCCESS,
  GET_SERVICES_FAILURE,
  GET_SERVICES_STACK_REQUEST,
  GET_SERVICES_STACK_SUCCESS,
  GET_SERVICES_STACK_FAILURE,
  SELECT_SERVICE,
  GET_ENV_STACK_REQUEST,
  GET_ENV_STACK_SUCCESS,
  GET_ENV_STACK_FAILURE,
  GET_ENV_REQUEST,
  GET_ENV_SUCCESS,
  GET_ENV_FAILURE,
  GET_DEPLOYABLE_VERSION_REQUEST,
  GET_DEPLOYABLE_VERSION_SUCCESS,
  GET_DEPLOYABLE_VERSION_FAILURE,
} from '../actions';
import * as API from '../../api/api';
import { servicesMock, stackServicesMock, envMock, stackEnvironmentsMock, depVersionMock } from './tempMock';

export const getServices = () => {
  return async dispatch => {
    dispatch({
      type: GET_SERVICES_REQUEST,
    });
    try {
      const data = await API.getServices();
      dispatch({
        type: GET_SERVICES_SUCCESS,
        payload: data,
      });
    } catch (error) {
      dispatch({
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
      const data = await API.getServicesStacks();
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
      const data = await API.getEnvironments();
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
      const data = await API.getEnvironmentsStacks();
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

export const selectServices = servicesId => {
  return dispatch => {
    dispatch({
      type: SELECT_SERVICE,
      payload: servicesId,
    });
  };
};

export const getDeployableVersion = (servicesId) => {
  return async dispatch => {
    dispatch({
      type: GET_DEPLOYABLE_VERSION_REQUEST,
    });
    try {
      const data = await API.getDeployableVersion(servicesId);
      dispatch({
        type: GET_DEPLOYABLE_VERSION_SUCCESS,
        // payload: data,
        payload: depVersionMock,
      });
    } catch (error) {
      dispatch({
        type: GET_DEPLOYABLE_VERSION_FAILURE,
        error,
      });
    }
  };
};
