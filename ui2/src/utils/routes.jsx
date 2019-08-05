import React from 'react';
import { Route, Redirect } from 'react-router-dom';
import { Container } from '../components/app/Container';
import { Login } from '../components/auth/Login';

export const PrivateRoute = ({ path, component, title }) => {
  return localStorage.getItem('token') ? (
    <Route
      path={path}
      render={({ match, ...props }) => <Container title={title} component={component} match={match} {...props} />}
    />
  ) : (
    <Redirect to="/auth/login" />
  );
};

export const PublicRoute = () => {
  return localStorage.getItem('token') ? (
    <Redirect to="/auth/addUser" /> //TODO: temp, will be directed to home
  ) : (
    <Route path="/auth/login" component={Login} />
  );
};
