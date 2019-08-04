import { GET_SERVICES_REQUEST, GET_SERVICES_SUCCESS, GET_SERVICES_FAILURE } from '../../actions';
import {
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
} from './constants';

const initialState = {
  services: null,
  servicesStacks: null,
  isLoading: false,
  selectedServices: null,
  environment: null,
  environmentsStacks: null,
};

export default function deploymentsReducer(state = initialState, action) {
  switch (action.type) {
    case GET_SERVICES_REQUEST:
      return { ...state, isLoading: true };
    case GET_SERVICES_SUCCESS:
      return { ...state, services: action.payload, isLoading: false };
    case GET_SERVICES_FAILURE:
      return { ...state, isLoading: false };
    case GET_SERVICES_STACK_REQUEST:
      return { ...state, isLoading: true };
    case GET_SERVICES_STACK_SUCCESS:
      return { ...state, servicesStacks: action.payload, isLoading: false };
    case GET_SERVICES_STACK_FAILURE:
      return { ...state, isLoading: false };
    case SELECT_SERVICE:
      return { ...state, selectedServices: action.payload };
    case GET_ENV_REQUEST:
      return { ...state, isLoading: true };
    case GET_ENV_SUCCESS:
      return { ...state, environment: action.payload, isLoading: false };
    case GET_ENV_FAILURE:
      return { ...state, isLoading: false };
    case GET_ENV_STACK_REQUEST:
      return { ...state, isLoading: true };
    case GET_ENV_STACK_SUCCESS:
      return { ...state, environmentsStacks: action.payload, isLoading: false };
    case GET_ENV_STACK_FAILURE:
      return { ...state, isLoading: false };
    default:
      return state;
  }
}
