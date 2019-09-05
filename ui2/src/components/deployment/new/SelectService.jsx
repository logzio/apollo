import React, { useEffect } from 'react';
import { AppTransfer } from '../../../common/Transfer';
import { cacheKeys } from '../../../utils/cacheConfig';
import { removeFromCache } from '../../../utils/cacheService';

export const SelectService = ({
  getServices,
  services,
  handleBreadcrumbs,
  getServicesStacks,
  servicesStacks,
  match,
  selectServices,
}) => {
  useEffect(() => {
    removeFromCache(cacheKeys.DEPLOYABLE_VERSIONS);
    handleBreadcrumbs('service');
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

  return (
    <div>
      <AppTransfer
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
        handleSelection={handleServicesSelection}
        emptyMsg={'Please select services from the left panel'}
      />
    </div>
  );
};
