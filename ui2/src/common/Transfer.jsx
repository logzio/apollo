import React, { useState } from 'react';
import { Transfer } from 'antd';
import { difference } from 'lodash';
import { Link } from 'react-router-dom';
import { AppButton } from '../common/Button';
import { AppTableTransfer } from './TableTransfer';
import { AppSkeleton } from './Skeleton';
import './Transfer.css';

export const AppTransfer = ({
  data,
  searchColumns,
  selectGroup,
  linkTo,
  addSearch,
  match,
  handleSelection,
  ...props
}) => {
  const [targetKeys, setTargetKeys] = useState([]);
  const [showSearch] = useState(!!searchColumns);
  const [selectedGroupService, setSelectedGroupService] = useState(null);
  const [disabledPredefinedGroups, toggleDisabledPredefinedGroups] = useState(false);
  const formattedData = data && data.map(({ id, ...rest }) => ({ ...rest, key: id.toString() }));

  const handleSearch = (inputValue, item) => {
    return searchColumns
      .map(searchCol => {
        const stringifiedItem = item[searchCol].toString().toLowerCase();
        return stringifiedItem && stringifiedItem.indexOf(inputValue.toLowerCase()) !== -1;
      })
      .includes(true);
  };

  const handleGroupSelection = predefinedGroupId => {
    const keys = selectGroup(predefinedGroupId);
    const addedKeys = difference(keys, targetKeys);
    if (addedKeys.length) {
      setTargetKeys([...targetKeys, ...addedKeys]);
    } else {
      setTargetKeys(difference(targetKeys, keys));
    }
  };

  const currentTable = match.url.split('/').pop();

  return (
    <>
      <div className="submit-transfer-table">
        <div className="small-title">{`Select your ${currentTable}`}</div>
        <Link
          to={{
            pathname: linkTo,
            search: `${addSearch}=${targetKeys}`,
          }}
        >
          <AppButton
            label={`Choose ${currentTable}`}
            disabled={!targetKeys.length}
            className={'table-submit-button'}
            type="primary"
            onClick={() => handleSelection(targetKeys)}
          />
        </Link>
      </div>
      {!data ? (
        <AppSkeleton />
      ) : (
        <Transfer
          className="table-transfer"
          dataSource={formattedData}
          filterOption={handleSearch}
          showSelectAll={false}
          targetKeys={targetKeys}
          showSearch={showSearch}
          onChange={(targetKeys, direction) => {
            if (direction === 'left') {
              toggleDisabledPredefinedGroups(false);
              setSelectedGroupService(null);
            }
            setTargetKeys(targetKeys);
          }}
          operations={['Select', 'Remove']}
          operationStyle={{ width: '100px' }}
          titles={[`Please select at least one ${currentTable}`, `Selected items`]}
          {...props}
        >
          {({ direction, filteredItems, onItemSelectAll, onItemSelect, selectedKeys }) => (
            <AppTableTransfer
              selectedGroupService={selectedGroupService}
              disabledPredefinedGroups={disabledPredefinedGroups}
              toggleDisabledPredefinedGroups={toggleDisabledPredefinedGroups}
              setSelectedGroupService={setSelectedGroupService}
              targetKeys={targetKeys}
              setTargetKeys={setTargetKeys}
              handleGroupSelection={handleGroupSelection}
              formattedData={formattedData}
              direction={direction}
              filteredItems={filteredItems}
              onItemSelectAll={onItemSelectAll}
              onItemSelect={onItemSelect}
              selectedKeys={selectedKeys}
              {...props}
            />
          )}
        </Transfer>
      )}
    </>
  );
};
