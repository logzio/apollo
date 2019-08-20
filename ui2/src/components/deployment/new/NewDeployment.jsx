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
  selectEnvironments,
  selectGroups,
  selectVersion,
} from '../../../store/actions/deploymentActions';
import { SelectService } from './SelectService';
import { SelectEnvironment } from './SelectEnv';
import { SelectGroup } from './SelectGroup';
import { SelectVersion } from './SelectVersion';
import { VerifyDeployment } from './VerifyDeployment';
import { getFromCache } from '../../../utils/cacheService';
import { Container } from '../../app/Container';

const NewDeploymentComponent = ({ match, location, selectedServices, ...props }) => {
  const GroupRoute = ({ path, ...rest }) => {
    const { isPartOfGroup = null } = getFromCache('selectedServices').shift();
    return isPartOfGroup ? (
      <Route path={path} render={({ match }) => <SelectGroup match={match} location={location} {...rest} />} />
    ) : (
      <Redirect to={`${match.url}/version${location.search}`} />
    );
  };

  return (
    <Switch>
      <Route
        path={`${match.url}/service`}
        render={({ match }) => <SelectService match={match} location={location} {...props} />}
      />
      <Route
        path={`${match.url}/environment`}
        render={({ match }) => <SelectEnvironment match={match} location={location} {...props} />}
      />
      <GroupRoute path={`${match.url}/group`} {...props} />
      <Route
        path={`${match.url}/verification`}
        render={({ match }) => <VerifyDeployment match={match} location={location} {...props} />}
      />
      <Route
        path={`${match.url}/version`}
        render={({ match }) => <SelectVersion match={match} location={location} {...props} />}
      />
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
    getServiceById,
    getDeployableVersionById,
    getDeployableVersionBySha,
    getLastCommitFromBranch,
    getGroups,
    selectServices,
    selectEnvironments,
    selectGroups,
    selectVersion,
  },
)(NewDeploymentComponent);
