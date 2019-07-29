import React, { useEffect } from 'react';
import TableTransfer from '../../../common/TableTransfer';
import Spinner from '../../../common/Spinner';
import { Link } from 'react-router-dom';

const SelectService = ({ getServices, services, handleBreadcrumbs, getServicesStack, servicesStacks, match }) => {
  // const [filteredServices, setFilteredServices] = useState();

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
      <button>
        <Link to={'environment'}>NEXT</Link>
      </button>
      <TableTransfer
        data={services}
        searchColumns={['name']}
        rightColTitles={['name']}
        leftColTitles={['name']}
        predefinedGroups={servicesStacks}
        selectGroup={stackSelection}
      />
    </div>
  );
};

export default SelectService;
