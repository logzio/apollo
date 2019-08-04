import { combineReducers } from 'redux';

import authReducer from './authReducer';
// import deploymentsReducer from './deploymentReducer';

const rootReducer = combineReducers({
  auth: authReducer,
});

export default rootReducer;
