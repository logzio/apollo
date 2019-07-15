import {
    SIGNUP_REQUEST,
    SIGNUP_SUCCESS,
    SIGNUP_FAILURE
} from '../../actions';
import * as API from '../../api/api';


export const signup = (userDetails) => {
    return async dispatch => {
        dispatch({
            type: SIGNUP_REQUEST
        });
        try {
            const data = await API.signup(userDetails);
            dispatch({
                type: SIGNUP_SUCCESS,
                payload: data
            });
        } catch (error) {
            dispatch({
                type: SIGNUP_FAILURE,
                error
            });
        }
    };
};
