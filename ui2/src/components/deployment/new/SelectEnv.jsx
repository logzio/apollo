import React, { useEffect } from 'react';
import { TableTransfer } from '../../../common/TableTransfer';
import { Spinner } from '../../../common/Spinner';

export const SelectEnvironment = ({
  getEnvironments,
  getEnvironmentsStacks,
  handleBreadcrumbs,
  environment,
  environmentsStacks,
}) => {
  useEffect(() => {
    handleBreadcrumbs('environment');
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
    <div>
      <TableTransfer
        data={environment}
        searchColumns={['name', 'geoRegion', 'availability', 'kubernetesMaster']}
        leftColTitles={['name', 'geoRegion', 'availability', 'kubernetesMaster']}
        rightColTitles={['name']}
        predefinedGroups={environmentsStacks}
        selectGroup={stackSelection}
        linkTo={'group'}
        addSearch={`${window.location.search}&environment`}
      />
    </div>
  );
};
