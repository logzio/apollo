import React, { useEffect } from 'react';
import { AppTransfer } from '../../../common/Transfer';
import { Spinner } from '../../../common/Spinner';
import { parse } from 'query-string';

export const SelectGroup = ({ handleBreadcrumbs, match, search, getGroups, groups, selectGroups }) => {
  const { service: serviceId, environment: environmentsId } = parse(search);

  useEffect(() => {
    handleBreadcrumbs('group');
    environmentsId.split(',').map(environmentId => getGroups(environmentId, serviceId));
  }, []);

  const handleGroupsSelection = groupsId => {
    selectGroups(groupsId.map(groupId => groups.find(service => service.id.toString() === groupId)));
  };

  if (!groups) {
    return <Spinner />;
  }

  return (
    <div>
      <AppTransfer
        data={groups}
        searchColumns={['name', 'scalingFactor', 'jsonParams']}
        leftColTitles={['name', 'scalingFactor', 'jsonParams', 'environmentId']}
        rightColTitles={['name']}
        columnTitles={['Name', 'Scaling Factor', 'Parameters', 'Environment']}
        linkTo={'version'}
        addSearch={`${search}&group`}
        match={match}
        handleSelection={handleGroupsSelection}
      />
    </div>
  );
};
