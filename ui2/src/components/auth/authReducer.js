import {
    SIGNUP_REQUEST,
    SIGNUP_SUCCESS,
    SIGNUP_FAILURE
} from '../../actions';

const initialState = {
    user: null,
    isLoading: false
};

export default function authReducer(state = initialState, action) {
    switch (action.type) {
        case SIGNUP_REQUEST:
            return {...state, isLoading: true};
        case SIGNUP_SUCCESS:
            return {...state, user: action.payload, isLoading: false};
        case SIGNUP_FAILURE:
            return {...state, isLoading: false};
        default:
            return state;
    }
}
