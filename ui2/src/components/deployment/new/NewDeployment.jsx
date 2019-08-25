import React from 'react';
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
import { getFromCache } from '../../../utils/cacheService';
import { cacheKeys } from '../../../utils/cacheConfig';
import { parse } from 'query-string';
import './NewDeployment.css';

const NewDeploymentComponent = ({ match, location, services, environments, selectServices, ...props }) => {
  const { service, environment, version, group } = parse(location.search);

  const getSelected = (selectedFrom, cachedKey, urlParams) => {
    const cachedSelectedServices = getFromCache(cachedKey);
    debugger;

    if (cachedSelectedServices.length > 1) {
      return cachedSelectedServices;
    } else {
      const urlParamsId = urlParams.split(',');
      const selectedItems = urlParamsId.map(
        urlParamId => selectedFrom && selectedFrom.find(selected => urlParamId === selected.id.toString()),
      );
      selectedItems && selectServices(selectedItems);
    }
  };

  const getSelectedServices = () => getSelected(services, cacheKeys.SELECTED_SERVICES, service);
  // const getSelectedEnv = () => getSelected(environments, cacheKeys.SELECTED_ENVIRONMENTS, environment);
  // const selectedGroupsNames = getSelected(selectedGroups, groups, cacheKeys.SELECTED_SERVICES, group);
  // const selectedServicesNames = getSelected(selectedVersion, versions, cacheKeys.SELECTED_VERSION, service);

  return (
    <Switch>
      <Route
        path={`${match.url}/service`}
        render={({ match }) => (
          <SelectService
            match={match}
            location={location}
            services={services}
            selectServices={selectServices}
            {...props}
          />
        )}
      />
      <Route
        path={`${match.url}/environment`}
        render={({ match }) => (
          <SelectEnvironment
            getSelectedServices={getSelectedServices}
            match={match}
            location={location}
            services={services}
            environments={environments}
            {...props}
          />
        )}
      />
      <Route
        path={`${match.url}/group`}
        render={({ match }) => <SelectGroup match={match} location={location} {...props} />}
      />
      <Route
        path={`${match.url}/version`}
        render={({ match }) => <SelectVersion match={match} location={location} {...props} />}
      />
      <Route
        path={`${match.url}/verification`}
        render={({ match }) => (
          <VerifyDeployment
            match={match}
            location={location}
            services={services}
            environments={environments}
            getSelectedServices={getSelectedServices}
            {...props}
          />
        )}
      />
      <Redirect to={`${match.url}/service`} />
    </Switch>
  );
};

const mapStateToProps = ({
  deploy: {
    services,
    isLoading,
    servicesStacks,
    selectedServices,
    environments,
    environmentsStacks,
    versions,
    groups,
    selectedEnvironments,
    selectedGroups,
    selectedVersion,
  },
}) => ({
  services,
  isLoading,
  servicesStacks,
  selectedServices,
  environments,
  environmentsStacks,
  versions,
  groups,
  selectedEnvironments,
  selectedGroups,
  selectedVersion,
});

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
