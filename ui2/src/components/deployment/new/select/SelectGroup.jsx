import React, { useEffect } from 'react';
import { AppTransfer } from '../../../../common/Transfer';
import { parse } from 'query-string';
import './SelectGroup.css';

export const SelectGroup = ({
  handleBreadcrumbs,
  match,
  search,
  getGroups,
  groups,
  selectGroups,
  environments,
  getEnvironments,
}) => {
  const { service: serviceId, environment: environmentsId } = parse(search);

  useEffect(() => {
    handleBreadcrumbs('group');
    getEnvironments();
    environmentsId.split(',').map(environmentId => getGroups(environmentId, serviceId));
  }, []);

  const findNameById = (itemId, itemsList) => {
    let itemName = null;
    itemsList &&
      itemsList.map(({ id, name }) => {
        if (id === itemId) {
          itemName = name;
        }
      });
    return itemName;
  };

  const formattedData = () => {
    return (
      groups &&
      environments &&
      groups.map(({ environmentId, ...dataItem }) => {
        return {
          ...dataItem,
          environmentId: environmentId,
          environmentName: findNameById(environmentId, environments),
        };
      })
    );
  };

  const handleGroupsSelection = groupsId => {
    selectGroups(groupsId.map(groupId => groups.find(service => service.id.toString() === groupId)));
  };

  return (
    <div className={'select-group'}>
      <AppTransfer
        data={formattedData()}
        searchColumns={['name', 'environmentName', 'scalingFactor', 'jsonParams']}
        leftColTitles={['name', 'environmentName', 'scalingFactor', 'jsonParams']}
        rightColTitles={['name']}
        columnTitles={['Name', 'Environment', 'Scaling Factor', 'Parameters']}
        linkTo={'version'}
        addSearch={`${search}&group`}
        match={match}
        handleSelection={handleGroupsSelection}
        showDefaultSelection={true}
      />
    </div>
  );
};
