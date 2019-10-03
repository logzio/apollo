import React, { useState } from 'react';
import _ from 'lodash';
import { AppTable } from './Table';
import { AppButton } from '../common/Button';
import { tableColumns } from '../utils/tableColumns';

export const AppTableTransfer = ({
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
  setTargetKeys,
  predefinedGroups,
  handleGroupSelection,
  formattedData,
  showDefaultSelection,
  ...props
}) => {
  const [selectedNonGroupService, toggleSelectedNonGroupService] = useState(false);
  const [selectedButton, toggleSelectedButton] = useState(false);
  const leftPanel = direction === 'left';
  const scroll = leftPanel ? { x: 900, y: 580 } : { x: 200, y: 580 };
  const columns = leftPanel ? tableColumns(leftColTitles, columnTitles) : tableColumns(rightColTitles, columnTitles);

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
      setTargetKeys && setTargetKeys([...keys, key]);
    },
  });

  return (
    <div>
      {leftPanel && (
        <div className="header-left-transfer-table">
          {predefinedGroups &&
            predefinedGroups.map(({ id, name }) => (
              <AppButton
                key={id}
                label={name}
                className={'table-button'}
                onClick={() => {
                  toggleSelectedButton(!selectedButton);
                  handleGroupSelection(id);
                }}
                icon={'block'}
                disabled={disabledPredefinedGroups}
              />
            ))}
          {showDefaultSelection && (
            <AppButton
              label={'Select all'}
              className={'table-button'}
              onClick={() => {
                setTargetKeys(filteredItems.map(({ key }) => key));
              }}
              icon={'block'}
            />
          )}
        </div>
      )}
      {!leftPanel && (
        <div>
          <AppButton
            label={'Remove all'}
            className={'table-button'}
            onClick={() => {
              onItemSelectAll(targetKeys, false);
              setTargetKeys([]);
              setSelectedGroupService(null);
              toggleSelectedNonGroupService(false);
              toggleDisabledPredefinedGroups(false);
            }}
            disabled={_.isEmpty(targetKeys)}
          />
        </div>
      )}
      <AppTable
        columns={columns}
        data={filteredItems}
        scroll={scroll}
        rowSelection={rowSelection}
        handleOnSelect={handleOnSelect}
        handleRowSelection={handleRowSelection}
        pagination={false}
        {...props}
      />
    </div>
  );
};
