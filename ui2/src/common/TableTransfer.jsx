import React, { useState } from 'react';
import { Transfer } from 'antd';
import _ from 'lodash';
import { Link } from 'react-router-dom';
import { AppButton } from '../common/Button';
import './TableTransfer.css';
import { AppTransfer } from './Transfer';

export const TableTransfer = ({
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
  const formattedData = data.map(({ id, ...rest }) => ({ ...rest, key: id.toString() }));

  // const handleSearch = (inputValue, item) =>
  //   searchColumns.map(searchCol => item[searchCol] && item[searchCol].indexOf(inputValue) !== -1).includes(true);

  const handleSearch = (inputValue, item) => {
    return searchColumns
      .map(searchCol => {
        const stringifiedItem = item[searchCol].toString();
        return stringifiedItem && stringifiedItem.indexOf(inputValue) !== -1;
      })
      .includes(true);
  };

  const handleGroupSelection = predefinedGroupId => {
    const keys = selectGroup(predefinedGroupId);
    const addedKeys = _.difference(keys, targetKeys);
    if (addedKeys.length) {
      setTargetKeys([...targetKeys, ...addedKeys]);
    } else {
      setTargetKeys(_.difference(targetKeys, keys));
    }
  };

  const currentTable = match.url.split('/').pop();

  return (
    <>
      <div className="submit-transfer-table">
        <Link
          to={{
            pathname: linkTo,
            search: `${addSearch}=${targetKeys}`,
          }}
        >
          <AppButton
            label={`Select your ${currentTable}`}
            disabled={!targetKeys.length}
            className={'table-submit-button'}
            type="primary"
            onClick={() => handleSelection(targetKeys)}
          />
        </Link>
      </div>
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
        titles={[`Please select at least one ${currentTable}`, `Selected items`]}
        {...props}
      >
        {({ direction, filteredItems, onItemSelectAll, onItemSelect, selectedKeys }) => (
          <AppTransfer
            selectedGroupService={selectedGroupService}
            disabledPredefinedGroups={disabledPredefinedGroups}
            toggleDisabledPredefinedGroups={toggleDisabledPredefinedGroups}
            setSelectedGroupService={setSelectedGroupService}
            targetKeys={targetKeys}
            linkTo={linkTo}
            setTargetKeys={setTargetKeys}
            addSearch={addSearch}
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
    </>
  );
};
