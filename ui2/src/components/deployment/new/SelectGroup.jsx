import React, { useEffect } from 'react';
import { TableTransfer } from '../../../common/TableTransfer';
import { Spinner } from '../../../common/Spinner';
import { parse } from 'query-string';

export const SelectGroup = ({
  handleBreadcrumbs,
  resetBreadcrumbs,
  match,
  location,
  getGroups,
  groups,
  selectGroups,
}) => {
  const { service: serviceId, environment: environmentsId } = parse(location.search);

  useEffect(() => {
    resetBreadcrumbs();
    handleBreadcrumbs(`${location.pathname}${location.search}`, 'group');
    environmentsId.split(',').map(environmentId => getGroups(environmentId, serviceId));
  }, []);

  const handleGroupsSelection = groupsId => {
    const test = groupsId.map(groupId => groups.find(service => service.id.toString() === groupId));
    debugger;
    selectGroups(test);
  };

  if (!groups) {
    return <Spinner />;
  }

  return (
    <div>
      <TableTransfer
        data={groups}
        searchColumns={['name', 'scalingFactor', 'jsonParams']}
        leftColTitles={['name', 'scalingFactor', 'jsonParams', 'environmentId']}
        rightColTitles={['name']}
        columnTitles={['Name', 'Scaling Factor', 'Parameters', 'Environment']}
        linkTo={'version'}
        addSearch={`${location.search}&group`}
        match={match}
        handleSelection={handleGroupsSelection}
      />
    </div>
  );
};
