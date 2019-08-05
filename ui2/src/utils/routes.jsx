import React from 'react';
import { Route, Redirect } from 'react-router-dom';
import { connect } from 'react-redux';
import Container from '../components/app/Container';
import Login from '../components/auth/Login';

const PrivateRouteEl = ({path, loggedIn, component, title }) => {
  return !!localStorage.getItem('token') ? (
    <Route
      path={path}
      render={({ match, ...props }) => <Container title={title} component={component} match={match} {...props} />}
    />
  ) : (
    <Redirect to="/auth/login" />
  );
};

const PublicRouteEl = ({loggedIn}) => {
  return !!localStorage.getItem('token') ? (
    <Redirect to="/deployment/new" /> //temp, will be directed to home
  ) : (
    <Route path="/auth/login" component={Login} />
  );
};

