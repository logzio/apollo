import React, { useEffect } from 'react';
import { TableTransfer } from '../../../common/TableTransfer';
import { Spinner } from '../../../common/Spinner';

export const SelectService = ({ getServices, services, handleBreadcrumbs, getServicesStack, servicesStacks, match }) => {
  useEffect(() => {
    handleBreadcrumbs(`${match.url}`, 'service');
    getServices();
    getServicesStack();
  }, []);

  const stackSelection = stackId => {
    const selectedStack = servicesStacks.find(servicesStack => servicesStack.id === stackId);
    return services
      .filter(service => selectedStack.services && selectedStack.services.includes(service.id))
      .map(selectedService => selectedService.id.toString());
  };

  if (!services || !servicesStacks) {
    return <Spinner />;
  }

  return (
    <div>
      <TableTransfer
        data={services}
        searchColumns={['name']}
        leftColTitles={['name']}
        rightColTitles={['name']}
        predefinedGroups={servicesStacks}
        selectGroup={stackSelection}
        linkTo={'environment'}
        addSearch={'service'}
      />
    </div>
  );
};
