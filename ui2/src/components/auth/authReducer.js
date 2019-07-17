import {
    SIGNUP_REQUEST,
    SIGNUP_SUCCESS,
    SIGNUP_FAILURE,
    GET_DEP_ROLE_REQUEST,
    GET_DEP_ROLE_SUCCESS,
    GET_DEP_ROLE_FAILURE
} from '../../actions';

const initialState = {
    user: null,
    isLoading: false,
    isAdmin: false,
    depRoles: null,
    error: null,
    loggedIn: true //temp until I set up Login screen + action + api
};

export default function authReducer(state = initialState, action) {
    switch (action.type) {
        case SIGNUP_REQUEST:
            return {...state, isLoading: true};
        case SIGNUP_SUCCESS:
            return {...state, user: action.payload, error: null, isLoading: false};
        case SIGNUP_FAILURE:
            return {...state, error: action.error, isLoading: false};
        case GET_DEP_ROLE_REQUEST:
            return {...state, isLoading: true};
        case GET_DEP_ROLE_SUCCESS:
            return {...state, depRoles: action.payload, isLoading: false};
        case GET_DEP_ROLE_FAILURE:
            return {...state, isLoading: false};
        default:
            return state;
    }
}
