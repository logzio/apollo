import React, { useState } from 'react';
import { connect } from 'react-redux';
import { Switch, Redirect, Route } from 'react-router-dom';
import {
  getServices,
  getServicesStacks,
  getEnvironments,
  getEnvironmentsStacks,
  getDeployableVersionsById,
  getLastCommitFromBranch,
  getGroups,
  selectServices,
  selectEnvironments,
  selectGroups,
  selectVersion,
} from '../../../store/actions/deploymentActions';
import { SelectService } from './SelectService';
import { SelectEnvironment } from './SelectEnv';
import { SelectGroup } from './SelectGroup';
import { SelectVersion } from './SelectVersion';
import { VerifyDeployment } from './VerifyDeployment';

const NewDeploymentComponent = ({ match, ...props }) => {
  return (
    <Switch>
      <Route path={`${match.url}/service`} render={({ match }) => <SelectService match={match} {...props} />} />
      <Route path={`${match.url}/environment`} render={({ match }) => <SelectEnvironment match={match} {...props} />} />
      <Route path={`${match.url}/group`} render={({ match }) => <SelectGroup match={match} {...props} />} />
      <Route path={`${match.url}/verification`} render={({ match }) => <VerifyDeployment match={match} {...props} />} />
      <Route path={`${match.url}/version`} render={({ match }) => <SelectVersion match={match} {...props} />} />
      <Redirect to={`${match.url}/service`} />
    </Switch>
  );
};

const mapStateToProps = ({
  deploy: { services, isLoading, servicesStacks, selectedServices, environments, environmentsStacks, versions, groups },
}) => ({ services, isLoading, servicesStacks, selectedServices, environments, environmentsStacks, versions, groups });

export const NewDeployment = connect(
  mapStateToProps,
  {
    getServices,
    getServicesStacks,
    getEnvironments,
    getEnvironmentsStacks,
    getDeployableVersionsById,
    getLastCommitFromBranch,
    getGroups,
    selectServices,
    selectEnvironments,
    selectGroups,
    selectVersion,
  },
)(NewDeploymentComponent);
