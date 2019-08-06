import React from 'react';
import { connect } from 'react-redux';
import { Switch, Redirect, Route } from 'react-router-dom';
import {
  getServices,
  getServicesStack,
  getEnvironment,
  getEnvironmentsStack,
} from '../../../store/actions/deploymentActions';
import { SelectService } from './SelectService';
import { SelectEnvironment } from './SelectEnv';
import { SelectGrourp } from './SelectGroup'; //temp placeholder

const NewDeploymentComponent = ({ match, ...props }) => {
  return (
    <Switch>
      <Route path={`${match.url}/service`} render={({ match }) => <SelectService match={match} {...props} />} />
      <Route path={`${match.url}/environment`} render={({ match }) => <SelectEnvironment match={match} {...props} />} />
      <Route path={`${match.url}/group`} render={({ match }) => <SelectGrourp match={match} {...props}/>} />
      <Redirect to={`${match.url}/service`} />
    </Switch>
  );
};

const mapStateToProps = ({
  deploy: { services, isLoading, servicesStacks, selectedServices, environment, environmentsStacks },
}) => ({ services, isLoading, servicesStacks, selectedServices, environment, environmentsStacks });

export const NewDeployment = connect(
  mapStateToProps,
  { getServices, getServicesStack, getEnvironment, getEnvironmentsStack },
)(NewDeploymentComponent);
