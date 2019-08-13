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
  GET_DEPLOYABLE_VERSION_SHA_REQUEST,
  GET_DEPLOYABLE_VERSION_SHA_SUCCESS,
  GET_DEPLOYABLE_VERSION_SHA_FAILURE,
  GET_BRANCH_LATEST_VERSION_REQUEST,
  GET_BRANCH_LATEST_VERSION_SUCCESS,
  GET_BRANCH_LATEST_VERSION_FAILURE,
  GET_GROUPS_REQUEST,
  GET_GROUPS_SUCCESS,
  GET_GROUPS_FAILURE,
  NEW_DEPLOYMENT_REQUEST,
  NEW_DEPLOYMENT_SUCCESS,
  NEW_DEPLOYMENT_FAILURE,
    GET_SERVICE_BY_ID_REQUEST,
    GET_SERVICE_BY_ID_SUCCESS,
    GET_SERVICE_BY_ID_FAILURE,
    LOUGOUT,
    SELECT_SERVICES,
  GET_ONGOING_DEPLOYMENT_REQUEST,
  GET_ONGOING_DEPLOYMENT_SUCCESS,
  GET_ONGOING_DEPLOYMENT_FAILURE,
  GET_LATEST_POD_REQUEST,
  GET_LATEST_POD_SUCCESS,
  GET_LATEST_POD_FAILURE,
  GET_LATEST_GROUP_POD_REQUEST, GET_LATEST_GROUP_POD_SUCCESS, GET_LATEST_GROUP_POD_FAILURE,
} from '../actions';

import _ from 'lodash';
const initialState = {
  services: null,
  servicesStacks: null,
  isLoading: false,
  selectedServices: [],
  environment: null,
  environmentsStacks: null,
  versions: null,
  groups: [],
  newDeployment: null,
  ongoingDeployments: null,
  lastCreatedPod: null,
  lastCreatedGroupPod: null,
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
    case SELECT_SERVICES:
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
    case GET_SERVICE_BY_ID_REQUEST:
      return { ...state, isLoading: true };
    case GET_SERVICE_BY_ID_SUCCESS: {
      state.selectedServices[action.payload.id] = action.payload;
      return { ...state, isLoading: false };
    }
    case GET_SERVICE_BY_ID_FAILURE:
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
      return { ...state, versions: [action.payload], isLoading: false };
    case GET_BRANCH_LATEST_VERSION_FAILURE:
      return { ...state, isLoading: false };
    case GET_GROUPS_REQUEST:
      return { ...state, isLoading: true };
    case GET_GROUPS_SUCCESS:
      return { ...state, groups: _.unionWith(state.groups, action.payload, _.isEqual), isLoading: false };
    case GET_GROUPS_FAILURE:
      return { ...state, isLoading: false };
    case NEW_DEPLOYMENT_REQUEST:
      return { ...state, isLoading: true };
    case NEW_DEPLOYMENT_SUCCESS:
      return { ...state, newDeployment: action.payload, isLoading: false };
    case NEW_DEPLOYMENT_FAILURE:
      return { ...state, isLoading: false };
    case GET_ONGOING_DEPLOYMENT_REQUEST:
      return { ...state, isLoading: true };
    case GET_ONGOING_DEPLOYMENT_SUCCESS:
      return { ...state, ongoingDeployments: action.payload, isLoading: false };
    case GET_ONGOING_DEPLOYMENT_FAILURE:
      return { ...state, isLoading: false };
    case GET_LATEST_POD_REQUEST:
      return { ...state, isLoading: true };
    case GET_LATEST_POD_SUCCESS:
      return { ...state, lastCreatedPod: action.payload, isLoading: false };
    case GET_LATEST_POD_FAILURE:
      return { ...state, isLoading: false };
    case GET_LATEST_GROUP_POD_REQUEST:
      return { ...state, isLoading: true };
    case GET_LATEST_GROUP_POD_SUCCESS:
      return { ...state, lastCreatedGroupPod: action.payload, isLoading: false };
    case GET_LATEST_GROUP_POD_FAILURE:
      return { ...state, isLoading: false };
    case GET_ONGOING_DEPLOYMENT_REQUEST:
      return { ...state, isLoading: true };
    case GET_ONGOING_DEPLOYMENT_SUCCESS:
      return { ...state, ongoingDeployments: action.payload, isLoading: false };
    case GET_ONGOING_DEPLOYMENT_FAILURE:
      return { ...state, isLoading: false };
    case GET_LATEST_POD_REQUEST:
      return { ...state, isLoading: true };
    case GET_LATEST_POD_SUCCESS:
      return { ...state, lastCreatedPod: action.payload, isLoading: false };
    case GET_LATEST_POD_FAILURE:
      return { ...state, isLoading: false };
    case GET_LATEST_GROUP_POD_REQUEST:
      return { ...state, isLoading: true };
    case GET_LATEST_GROUP_POD_SUCCESS:
      return { ...state, lastCreatedGroupPod: action.payload, isLoading: false };
    case GET_LATEST_GROUP_POD_FAILURE:
      return { ...state, isLoading: false };
    case LOUGOUT:
      return initialState;
    default:
      return state;
  }
}
