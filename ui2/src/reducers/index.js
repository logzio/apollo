import { combineReducers } from 'redux';
import authReducer from '../components/auth/authReducer';

const rootReducer = combineReducers({
  auth: authReducer,
});

export default rootReducer;
