import React from 'react';
import { connect } from 'react-redux';
import { Switch, Redirect, Route } from 'react-router-dom';
import {
  getServices,
  getServicesStacks,
  getEnvironments,
  getEnvironmentsStacks,
  getServiceById,
  getDeployableVersionById,
  getDeployableVersionBySha,
  getLastCommitFromBranch,
  getGroups,
  selectServices,
} from '../../../store/actions/deploymentActions';
import { SelectService } from './SelectService';
import { SelectEnvironment } from './SelectEnv';
import { SelectGrourp } from './SelectGroup';
import { SelectVersion } from './SelectVersion';
import { VerifyDeployment } from './VerifyDeployment';

const NewDeploymentComponent = ({ match, location, selectedServices, ...props }) => {

  return (
    <Switch>
      <Route path={`${match.url}/service`} render={({ match }) => <SelectService match={match} location={location} {...props} />} />
      <Route path={`${match.url}/environment`} render={({ match }) => <SelectEnvironment match={match} location={location} {...props} />} />
      <Route path={`${match.url}/group`} render={({ match }) => <SelectGrourp match={match} location={location} {...props} />} />
      <Route path={`${match.url}/verification`} render={({ match }) => <VerifyDeployment match={match} location={location} {...props} />} />
      <Route path={`${match.url}/version`} render={({ match }) => <SelectVersion match={match} location={location} {...props} />} />
      <Redirect to={`${match.url}/service`} />
    </Switch>
  );
};

const mapStateToProps = ({
  deploy: { services, isLoading, servicesStacks, selectedServices, environment, environmentsStacks, versions, groups },
}) => ({ services, isLoading, servicesStacks, selectedServices, environment, environmentsStacks, versions, groups });

export const NewDeployment = connect(
  mapStateToProps,
  {
    getServices,
    getServicesStacks,
    getEnvironments,
    getEnvironmentsStacks,
    getServiceById,
    getDeployableVersionById,
    getDeployableVersionBySha,
    getLastCommitFromBranch,
    getGroups,
    selectServices,
  },
)(NewDeploymentComponent);
