import React, { useEffect } from 'react';
import { TableTransfer } from '../../../common/TableTransfer';
import { Spinner } from '../../../common/Spinner';

export const SelectEnvironment = ({
  getEnvironments,
  getEnvironmentsStacks,
  handleBreadcrumbs,
  environment,
  environmentsStacks,
  match,
  location,
}) => {
  useEffect(() => {
    handleBreadcrumbs(`${location.pathname}${location.search}`, 'environment');
      getEnvironments();
      getEnvironmentsStacks();
  }, []);

  const stackSelection = stackId => {
    const selectedStack = environmentsStacks.find(environmentsStack => environmentsStack.id === stackId);
    return environment
      .filter(env => selectedStack.environments && selectedStack.environments.includes(env.id))
      .map(selectedEnv => selectedEnv.id.toString());
  };

  if (!environment || !environmentsStacks) {
    return <Spinner />;
  }

  return (
    <TableTransfer
      data={environment}
      searchColumns={['name', 'geoRegion', 'availability', 'kubernetesMaster']}
      leftColTitles={['name', 'geoRegion', 'availability', 'kubernetesMaster']}
      columnTitles={['Name', 'Region', 'Availability', 'Kubernetes Master']}
      rightColTitles={['name']}
      predefinedGroups={environmentsStacks}
      selectGroup={stackSelection}
      linkTo={'group'}
      addSearch={`${location.search}&environment`}
      match={match}
    />
  );
};
