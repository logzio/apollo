import React, { useEffect } from 'react';
import { TableTransfer } from '../../../common/TableTransfer';
import { Spinner } from '../../../common/Spinner';
import { parse } from 'query-string';
import { getFromCache } from '../../../utils/cacheService';

export const SelectEnvironment = ({
  getEnvironments,
  getEnvironmentsStacks,
  handleBreadcrumbs,
  environments,
  environmentsStacks,
  match,
  location,
  selectEnvironments,
  getServices,
  services,
  selectedServices,
}) => {
  useEffect(() => {
    handleBreadcrumbs(`${location.pathname}${location.search}`, 'environment');
    getEnvironments();
    getEnvironmentsStacks();
    getServices();
  }, []);

  const stackSelection = stackId => {
    const selectedStack = environmentsStacks.find(environmentsStack => environmentsStack.id === stackId);
    return environments
      .filter(env => selectedStack.environments && selectedStack.environments.includes(env.id))
      .map(selectedEnv => selectedEnv.id.toString());
  };

  const getLink = () => {
    const cachedSelectedServices = getFromCache('selectedServices');
    if (selectedServices.length || cachedSelectedServices) {
      return (
        (selectedServices.length && !!selectedServices[0].isPartOfGroup) ||
        (cachedSelectedServices && !!cachedSelectedServices[0].isPartOfGroup)
      );
    } else {
      const { service } = parse(location.search);
      const [selectedServiceId] = service.split(',');
      return !!services.find(({ id }) => id.toString() === selectedServiceId).isPartOfGroup;
    }
  };

  const handleEnvironmentsSelection = environmentsId => {
    selectEnvironments(
      environmentsId.map(environmentId => environments.find(service => service.id.toString() === environmentId)),
    );
  };

  if (!environments || !environmentsStacks || !services) {
    return <Spinner />;
  }

  return (
    <TableTransfer
      data={environments}
      searchColumns={['name', 'geoRegion', 'availability', 'kubernetesMaster']}
      leftColTitles={['name', 'geoRegion', 'availability', 'kubernetesMaster']}
      columnTitles={['Name', 'Region', 'Availability', 'Kubernetes Master']}
      rightColTitles={['name']}
      predefinedGroups={environmentsStacks}
      selectGroup={stackSelection}
      linkTo={getLink() ? 'group' : 'version'}
      addSearch={`${location.search}&environment`}
      match={match}
      emptyMsg={'Please select environments from the left panel'}
      handleSelection={handleEnvironmentsSelection}
    />
  );
};
