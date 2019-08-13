import React from 'react';
import { Container } from '../components/app/Container';
import { Login } from '../components/auth/Login';
import { getAuthToken } from '../api/api';
import { Route, Redirect } from 'react-router-dom';
import { SelectService } from '../components/deployment/new/SelectService';

export const PrivateRoute = ({ path, ...props }) => {
  return getAuthToken() ? (
    <Route path={path} render={({ match }) => <Container match={match} {...props} />} />
  ) : (
    <Redirect to="/auth/login" />
  );
};

export const PublicRoute = ({ path, component }) => {
  return !getAuthToken() ? <Route path={path} component={component} /> : <Redirect to="/auth/addUser" />;
};
