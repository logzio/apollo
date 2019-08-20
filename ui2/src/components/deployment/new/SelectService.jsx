import React, { useEffect } from 'react';
import { TableTransfer } from '../../../common/TableTransfer';
import { Spinner } from '../../../common/Spinner';

export const SelectService = ({
  getServices,
  services,
  handleBreadcrumbs,
  getServicesStacks,
  servicesStacks,
  resetBreadcrumbs,
  match,
<<<<<<< HEAD
  selectServices,
=======
>>>>>>> 21fb84b4660cf6c09a558a25820ea90d74c9772e
}) => {
  useEffect(() => {
    resetBreadcrumbs();
    handleBreadcrumbs(`${match.url}`, 'service');
    getServices();
    getServicesStacks();
  }, []);

  const stackSelection = stackId => {
    const selectedStack = servicesStacks.find(servicesStack => servicesStack.id === stackId);
    return services
      .filter(service => selectedStack.services && selectedStack.services.includes(service.id))
      .map(selectedService => selectedService.id.toString());
  };

  const handleServicesSelection = servicesId =>
    selectServices(servicesId.map(serviceId => services.find(service => service.id.toString() === serviceId)));

  if (!services || !servicesStacks) {
    return <Spinner />;
  }

  return (
    <TableTransfer
      data={services}
      searchColumns={['name']}
      leftColTitles={['name']}
      rightColTitles={['name']}
      columnTitles={['Name']}
      predefinedGroups={servicesStacks}
      selectGroup={stackSelection}
      linkTo={'environment'}
      addSearch={'service'}
      match={match}
<<<<<<< HEAD
      handleSelection={handleServicesSelection}
      emptyMsg={'Please select services from the left panel'}
=======
>>>>>>> 21fb84b4660cf6c09a558a25820ea90d74c9772e
    />
  );
};
