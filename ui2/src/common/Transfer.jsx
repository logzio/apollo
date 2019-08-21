import React, { useState } from 'react';
import _ from 'lodash';
import { AppTable } from './Table';
import { AppButton } from '../common/Button';
import { transferTableColumns } from '../utils/tableColumns';
import './TableTransfer.css';
import { historyBrowser } from '../utils/history';

export const AppTransfer = ({
  direction,
  filteredItems,
  onItemSelectAll,
  onItemSelect,
  selectedKeys,
  disabledPredefinedGroups,
  selectedGroupService,
  rightColTitles,
  leftColTitles,
  columnTitles,
  toggleDisabledPredefinedGroups,
  setSelectedGroupService,
  targetKeys,
  linkTo,
  setTargetKeys,
  addSearch,
  predefinedGroups,
  handleGroupSelection,
  formattedData,
  ...props
}) => {
  const [selectedNonGroupService, toggleSelectedNonGroupService] = useState(false);
  const leftPanel = direction === 'left';

  const columns = leftPanel
    ? transferTableColumns(leftColTitles, columnTitles)
    : transferTableColumns(rightColTitles, columnTitles);

  const scroll = leftPanel ? { x: 900, y: 580 } : { x: 400, y: 580 };

  const handleOnSelect = (key, isPartOfGroup, isSelected) => {
    if (isPartOfGroup) {
      toggleDisabledPredefinedGroups(leftPanel ? !disabledPredefinedGroups : true);
      setSelectedGroupService(selectedGroupService && leftPanel ? null : key);
      return onItemSelect(key, isSelected);
    }
    toggleSelectedNonGroupService(leftPanel);
    return onItemSelect(key, isSelected);
  };

  const handleDisabledRaws = record => {
    return (
      (record.isPartOfGroup === true && targetKeys.length > 0) ||
      (selectedGroupService && selectedGroupService !== record.key) ||
      (record.isPartOfGroup === true && selectedNonGroupService && selectedKeys.length > 0)
    );
  };

  const rowSelection = {
    onSelectAll: (isSelected, allRows) => {
      const allRowsKeys = allRows && allRows.map(item => item.key);
      const currentKeysSelection = isSelected
        ? _.difference(allRowsKeys, selectedKeys)
        : _.difference(selectedKeys, allRowsKeys);
      onItemSelectAll(currentKeysSelection, isSelected);
    },
    onSelect: ({ key, isPartOfGroup }, isSelected) => {
      handleOnSelect(key, isPartOfGroup, isSelected);
    },
    selectedRowKeys: selectedKeys,
    getCheckboxProps: record => ({
      disabled: leftPanel ? handleDisabledRaws(record) : null,
    }),
  };

  const handleRowSelection = ({ key, isPartOfGroup }) => ({
    onClick: () => {
      handleOnSelect(key, isPartOfGroup, !selectedKeys.includes(key));
    },
    onDoubleClick: () => {
      const keys = targetKeys ? targetKeys : [];
      onItemSelect && onItemSelect([...keys, key], !selectedKeys.includes(key));
      setTargetKeys && setTargetKeys([...keys, key]);
      setTimeout(
        () =>
          historyBrowser.push({
            pathname: `${linkTo}`,
            search: `${addSearch}${[...keys, key]}`,
          }),
        100,
      );
    },
  });

  return (
    <div>
      {leftPanel && (
        <div className="header-left-transfer-table">
          {predefinedGroups ? (
            predefinedGroups.map(({ id, name }) => (
              <AppButton
                key={id}
                label={name}
                className={'table-button'}
                onClick={() => handleGroupSelection(id)}
                icon={'block'}
                disabled={disabledPredefinedGroups}
              />
            ))
          ) : (
            <AppButton
              label={'Select all'}
              className={'table-button'}
              onClick={() => setTargetKeys(formattedData.map(({ key }) => key))}
              icon={'block'}
            />
          )}
        </div>
      )}
      {!leftPanel && (
        <div>
          <AppButton
            label={'Reset'}
            className={'table-button'}
            onClick={() => {
              setTargetKeys([]);
              setSelectedGroupService(null);
              toggleSelectedNonGroupService(false);
              toggleDisabledPredefinedGroups(false);
            }}
          />
        </div>
      )}
      <AppTable
        columns={columns}
        data={filteredItems}
        onItemSelectAll={onItemSelectAll}
        onItemSelect={onItemSelect}
        selectedKeys={selectedKeys}
        scroll={scroll}
        linkTo={linkTo}
        addSearch={`${addSearch}=`}
        setTargetKeys={setTargetKeys}
        targetKeys={targetKeys}
        rowSelection={rowSelection}
        handleOnSelect={handleOnSelect}
        handleRowSelection={handleRowSelection}
        {...props}
      />
    </div>
  );
};
