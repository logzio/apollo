import { GET_SERVICES_REQUEST, GET_SERVICES_SUCCESS, GET_SERVICES_FAILURE } from '../../actions';

const initialState = {
  services: null,
  isLoading: false,
};

export default function deploymentsReducer(state = initialState, action) {
  switch (action.type) {
    case GET_SERVICES_REQUEST:
      return { ...state, isLoading: true };
    case GET_SERVICES_SUCCESS:
      return { ...state, services: action.payload, isLoading: false };
    case GET_SERVICES_FAILURE:
      return { ...state, isLoading: false };
    default:
      return state;
  }
}
