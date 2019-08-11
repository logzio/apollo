import React from 'react';
import { connect } from 'react-redux';
import { Switch, Redirect, Route } from 'react-router-dom';
import {
  getServices,
  getServicesStacks,
  getEnvironments,
  getEnvironmentsStacks,
    getDeployableVersionById,
    getDeployableVersionBySha,getLastCommitFromBranch
} from '../../../store/actions/deploymentActions';
import { SelectService } from './SelectService';
import { SelectEnvironment } from './SelectEnv';
import { SelectGrourp } from './SelectGroup'; //temp placeholder
import { SelectVersion } from './SelectVersion'; //temp placeholder

const NewDeploymentComponent = ({ match, ...props }) => {
  return (
    <Switch>
      <Route path={`${match.url}/service`} render={({ match }) => <SelectService match={match} {...props} />} />
      <Route path={`${match.url}/environment`} render={({ match }) => <SelectEnvironment match={match} {...props} />} />
      <Route path={`${match.url}/group`} render={({ match }) => <SelectGrourp match={match} {...props} />} />
      <Route path={`${match.url}/version`} render={({ match }) => <SelectVersion match={match} {...props} />} />
      <Redirect to={`${match.url}/service`} />
    </Switch>
  );
};

const mapStateToProps = ({
  deploy: { services, isLoading, servicesStacks, selectedServices, environment, environmentsStacks, versions },
}) => ({ services, isLoading, servicesStacks, selectedServices, environment, environmentsStacks, versions });

export const NewDeployment = connect(
  mapStateToProps,
  { getServices, getServicesStacks, getEnvironments, getEnvironmentsStacks,
      getDeployableVersionById,
      getDeployableVersionBySha,getLastCommitFromBranch },
)(NewDeploymentComponent);
