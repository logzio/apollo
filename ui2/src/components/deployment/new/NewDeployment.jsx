import React, { useEffect, useState } from 'react';
import { connect } from 'react-redux';
import { Switch, Redirect, Route } from 'react-router-dom';
import { PrivateRoute } from '../../../utils/routes';
import SelectService from './SelectService';
import SelectEnv from './SelectEnv';
import { getServices } from '../deploymentActions';
import Signup from '../../auth/Signup';

const NewDeploymentComponent = ({ addBreadcrumb, getServices, services, match }) => {
  return (
    <Switch>
      <Route
        path={`${match.url}/service`}
        render={() => <SelectService getServices={getServices} services={services} addBreadcrumb={addBreadcrumb} />}
      />
      <Route path={`${match.url}/environment`} render={() => <SelectEnv addBreadcrumb={addBreadcrumb} />} />
      <Redirect to={`${match.url}/service`} />
    </Switch>
  );
};

const mapStateToProps = state => {
  const { deploy } = state;
  return {
    services: deploy.services,
    isLoading: deploy.isLoading,
  };
};

const NewDeployment = connect(
  mapStateToProps,
  { getServices },
)(NewDeploymentComponent);

export default NewDeployment;
