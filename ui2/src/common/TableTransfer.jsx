import React, { useState } from 'react';
import { Transfer } from 'antd';
import _ from 'lodash';
import { AppTable } from './Table';
import { Link } from 'react-router-dom';
import { AppButton } from '../common/Button';
import { transferTableColumns } from '../utils/tableColumns';
import './TableTransfer.css';

export const TableTransfer = ({
  data,
  searchColumns,
  rightColTitles,
  leftColTitles,
  selectGroup,
  predefinedGroups,
  linkTo,
  addSearch,
  match,
  columnTitles,
  handleSelection,
  ...props
}) => {
  const [targetKeys, setTargetKeys] = useState([]);
  const [showSearch] = useState(!!searchColumns);
  const [selectedGroupService, setSelectedGroupService] = useState(null);
  const [selectedNonGroupService, setSelectedNonGroupService] = useState(false);
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
        onChange={targetKeys => setTargetKeys(targetKeys)}
        titles={[`Please select at least one ${currentTable}`, `Selected items`]}
        {...props}
      >
        {({ direction, filteredItems, onItemSelectAll, onItemSelect, selectedKeys }) => {
          const columns =
            direction === 'left'
              ? transferTableColumns(leftColTitles, columnTitles)
              : transferTableColumns(rightColTitles, columnTitles);
          const scroll = direction === 'left' ? { x: 900, y: 580 } : { x: 400, y: 580 };
          const rowSelection = {
            onSelectAll: (isSelected, allRows) => {
              const allRowsKeys = allRows && allRows.map(item => item.key);
              const currentKeysSelection = isSelected
                ? _.difference(allRowsKeys, selectedKeys)
                : _.difference(selectedKeys, allRowsKeys);
              onItemSelectAll(currentKeysSelection, isSelected);
            },
            onSelect: (item, isSelected) => {
              if (item.isPartOfGroup) {
                setSelectedGroupService(selectedGroupService ? null : item.key);
                return onItemSelect(item.key, isSelected);
              }
              setSelectedNonGroupService(true);
              return onItemSelect(item.key, isSelected);
            },
            selectedRowKeys: selectedKeys,
            getCheckboxProps: record => ({
              disabled:
                (record.isPartOfGroup === true && targetKeys.length > 0) ||
                (selectedGroupService && selectedGroupService !== record.key) ||
                (record.isPartOfGroup === true && selectedNonGroupService),
            }),
          };
          return (
            <div>
              {direction === 'left' && (
                <div className="header-left-transfer-table">
                  {predefinedGroups ? (
                    predefinedGroups.map(({ id, name }) => (
                      <AppButton
                        key={id}
                        label={name}
                        className={'table-button'}
                        onClick={() => handleGroupSelection(id)}
                        icon={'block'}
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
              {direction === 'right' && (
                <div>
                  <AppButton
                    label={'Reset'}
                    className={'table-button'}
                    onClick={() => {
                      setTargetKeys([]);
                      setSelectedGroupService(null);
                      setSelectedNonGroupService(false);
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
                // rowSelection={direction === 'left' ? rowSelection : null}
                rowSelection={rowSelection}
                {...props}
              />
            </div>
          );
        }}
      </Transfer>
    </>
  );
};
