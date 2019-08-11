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
  GET_DEPLOYABLE_VERSION_ID_REQUEST,
  GET_DEPLOYABLE_VERSION_ID_SUCCESS,
  GET_DEPLOYABLE_VERSION_ID_FAILURE,
  GET_DEPLOYABLE_VERSION_SHA_REQUEST,
  GET_DEPLOYABLE_VERSION_SHA_SUCCESS,
  GET_DEPLOYABLE_VERSION_SHA_FAILURE,
  GET_BRANCH_LATEST_VERSION_REQUEST,
  GET_BRANCH_LATEST_VERSION_SUCCESS,
  GET_BRANCH_LATEST_VERSION_FAILURE,
} from '../actions';

const initialState = {
  services: null,
  servicesStacks: null,
  isLoading: false,
  selectedServices: null,
  environment: null,
  environmentsStacks: null,
  versions: null,
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
    case GET_DEPLOYABLE_VERSION_ID_REQUEST:
      return { ...state, isLoading: true };
    case GET_DEPLOYABLE_VERSION_ID_SUCCESS:
      return { ...state, versions: action.payload, isLoading: false };
    case GET_DEPLOYABLE_VERSION_ID_FAILURE:
      return { ...state, isLoading: false };
    case GET_DEPLOYABLE_VERSION_SHA_REQUEST:
      return { ...state, isLoading: true, versions: null };
    case GET_DEPLOYABLE_VERSION_SHA_SUCCESS:
      return { ...state, versions: action.payload, isLoading: false };
    case GET_DEPLOYABLE_VERSION_SHA_FAILURE:
      return { ...state, isLoading: false };
    case GET_BRANCH_LATEST_VERSION_REQUEST:
      return { ...state, isLoading: true, versions: null };
    case GET_BRANCH_LATEST_VERSION_SUCCESS:
      return { ...state, versions: action.payload, isLoading: false };
    case GET_BRANCH_LATEST_VERSION_FAILURE:
      return { ...state, isLoading: false };
    default:
      return state;
  }
}
