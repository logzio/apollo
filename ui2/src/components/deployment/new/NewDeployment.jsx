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
  deploy,
  deployGroup,
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

const NewDeploymentComponent = ({
  match,
  search,
  services,
  environments,
  versions,
  groups,
  selectServices,
  selectEnvironments,
  selectVersion,
  selectGroups,
  ...props
}) => {
  const { service, environment, version, group } = parse(search);

  const getSelected = (selectedFrom, cachedKey, urlParams, setSelected, saveAsObject) => {
    const selectedFromCache = getFromCache(cachedKey);
    const urlParamsId = urlParams.split(',');
    const selectedItems =
      !saveAsObject &&
      selectedFrom &&
      urlParamsId.map(urlParamId => selectedFrom.find(selected => urlParamId === selected.id.toString()));
    const selectedItem =
      saveAsObject && selectedFrom && selectedFrom.find(selected => urlParams === selected.id.toString());
    (selectedFromCache || selectedItems || selectedItem) &&
      setSelected(selectedFromCache ? selectedFromCache : selectedItems || selectedItem);
  };

  const getSelectedEnv = () =>
    getSelected(environments, cacheKeys.SELECTED_ENVIRONMENTS, environment, selectEnvironments, selectEnvironments);
  const getSelectedServices = () => getSelected(services, cacheKeys.SELECTED_SERVICES, service, selectServices);
  const getSelectedGroups = () => getSelected(groups, cacheKeys.SELECTED_GROUPS, group, selectGroups);
  const getSelectedVersion = () => getSelected(versions, cacheKeys.SELECTED_VERSION, version, selectVersion, true);

  return (
    <Switch>
      <Route
        path={`${match.url}/service`}
        render={({ match }) => (
          <SelectService match={match} search={search} services={services} selectServices={selectServices} {...props} />
        )}
      />
      <Route
        path={`${match.url}/environment`}
        render={({ match }) => (
          <SelectEnvironment
            getSelectedServices={getSelectedServices}
            match={match}
            search={search}
            services={services}
            environments={environments}
            selectEnvironments={selectEnvironments}
            {...props}
          />
        )}
      />
      <Route
        path={`${match.url}/group`}
        render={({ match }) => (
          <SelectGroup match={match} search={search} selectGroups={selectGroups} groups={groups} {...props} />
        )}
      />
      <Route
        path={`${match.url}/version`}
        render={({ match }) => (
          <SelectVersion match={match} search={search} selectVersion={selectVersion} versions={versions} {...props} />
        )}
      />
      <Route
        path={`${match.url}/verification`}
        render={({ match }) => (
          <VerifyDeployment
            match={match}
            search={search}
            services={services}
            environments={environments}
            getSelectedServices={getSelectedServices}
            getSelectedEnv={getSelectedEnv}
            getSelectedVersion={getSelectedVersion}
            versions={versions}
            getSelectedGroups={getSelectedGroups}
            groups={groups}
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
    deploy,
    deployGroup,
  },
)(NewDeploymentComponent);
