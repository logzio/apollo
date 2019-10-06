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

  const defineSelection = (key, isPartOfGroup) => {
    if (isPartOfGroup) {
      toggleSelectedNonGroupService(false);
      toggleDisabledPredefinedGroups(leftPanel ? !disabledPredefinedGroups : true);
      setSelectedGroupService(selectedGroupService && leftPanel ? null : key);
    } else {
      toggleSelectedNonGroupService(leftPanel);
    }
  };

  const handleOnSelect = (key, isPartOfGroup, isSelected) => {
    defineSelection(key, isPartOfGroup);
    return onItemSelect(key, isSelected);
  };

  const handleDisabledRaws = (key, isPartOfGroup) => {
    return (
      (isPartOfGroup === true && targetKeys.length > 0 && leftPanel) ||
      (selectedGroupService && selectedGroupService !== key && leftPanel) ||
      (isPartOfGroup === true && selectedNonGroupService && selectedKeys.length > 0 && leftPanel)
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
    getCheckboxProps: ({ key, isPartOfGroup }) => ({
      disabled: leftPanel ? handleDisabledRaws(key, isPartOfGroup) : null,
    }),
  };

  const handleRowSelection = ({ key, isPartOfGroup }) => {
    const isDisabled = handleDisabledRaws(key, isPartOfGroup);

    return {
      onClick: () => {
        if (!isDisabled) {
          handleOnSelect(key, isPartOfGroup, !selectedKeys.includes(key));
        }
      },
      onDoubleClick: () => {
        if (!isDisabled) {
          defineSelection(key, isPartOfGroup);
          if (leftPanel) {
            const keys = targetKeys ? targetKeys : [];
            setTargetKeys && setTargetKeys([...keys, key]);
          } else {
            const remainTargetKeys = targetKeys.filter(targetKey => targetKey !== key);
            setTargetKeys && setTargetKeys(remainTargetKeys);
            isPartOfGroup && setSelectedGroupService(null);
          }
        }
      },
    };
  };

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
              toggleSelectedNonGroupService(false);
              onItemSelectAll(targetKeys, false);
              setSelectedGroupService(null);
              setTargetKeys([]);
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
