import React, { useEffect } from 'react';
import { TableTransfer } from '../../../common/TableTransfer';
import { Spinner } from '../../../common/Spinner';

export const SelectGroup = ({
  handleBreadcrumbs,
  resetBreadcrumbs,
  match,
  location,
  getGroups,
  groups,
  selectGroups,
}) => {
  useEffect(() => {
    // resetBreadcrumbs();
    // handleBreadcrumbs(`${location.pathname}${location.search}`, 'group');
    const [serviceParams, environmentParams] = location.search.split('&');
    const serviceId = serviceParams.split('=').pop();
    const environmentsId = environmentParams
      .split('=')
      .pop()
      .split(',');
    environmentsId.map(environmentId => getGroups(environmentId, serviceId));
  }, []);

  const handleGroupsSelection = groupsId =>
    selectGroups(groupsId.map(groupId => groups.find(service => service.id.toString() === groupId)));

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
