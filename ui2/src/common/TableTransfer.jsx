import React, { useState } from 'react';
import { Transfer } from 'antd';
import _ from 'lodash';
import Table from './Table';
import tableColumns from '../utils/tableColumns';
import './TableTransfer.css';
import { Link } from 'react-router-dom';
import Button from '../common/Button';

const TableTransfer = ({
  data,
  searchColumns,
  rightColTitles,
  leftColTitles,
  selectGroup,
  predefinedGroups,
  linkTo,
  addSearch,
  ...props
}) => {
  const [targetKeys, setTargetKeys] = useState([]);
  const [showSearch] = useState(!!searchColumns);
  const formattedData = data.map(dataItem => ({ ...dataItem, key: dataItem.id.toString() }));
  const handleSearch = (inputValue, item) => {
    return searchColumns.map(searchCol => item[searchCol] && item[searchCol].indexOf(inputValue) !== -1).includes(true);
  };

  return (
    <div className="table-transfer">
      <Transfer
        className="transfer"
        dataSource={formattedData}
        filterOption={(inputValue, item) => handleSearch(inputValue, item)}
        showSelectAll={false}
        targetKeys={targetKeys}
        showSearch={showSearch}
        onChange={targetKeys => setTargetKeys(targetKeys)}
        {...props}
      >
        {({ direction, filteredItems, onItemSelectAll, onItemSelect, selectedKeys }) => {
          let rowSelection = {
            onSelectAll: (isSelected, allRows) => {
              const allRowsKeys = allRows && allRows.map(item => item.key);
              const currentKeysSelection = isSelected
                ? _.difference(allRowsKeys, selectedKeys)
                : _.difference(selectedKeys, allRowsKeys);
              onItemSelectAll(currentKeysSelection, isSelected);
            },
            onSelect: (item, isSelected) => onItemSelect(item.key, isSelected),
            selectedRowKeys: selectedKeys,
          };

          const handleGroupSelection = predefinedGroup => {
            const keys = selectGroup(predefinedGroup.id);
            const addedKeys = _.difference(keys, targetKeys);
            // debugger;
            if (addedKeys.length) {
              setTargetKeys([...targetKeys, ...addedKeys]);
            } else {
              setTargetKeys(_.difference(targetKeys, keys));
            }
          };
          const columns = direction === 'left' ? tableColumns(leftColTitles) : tableColumns(rightColTitles);

          return (
            <div>
              {direction === 'left' &&
                predefinedGroups.map(predefinedGroup => (
                  <button
                    key={predefinedGroup.id}
                    onClick={() => {
                      handleGroupSelection(predefinedGroup);
                    }}
                  >
                    {predefinedGroup.name}
                  </button>
                ))}
              {direction === 'right' && (
                <div>
                  <button onClick={() => setTargetKeys([])}>Reset</button>
                  <Link
                    to={{
                      pathname: linkTo,
                      search: `${addSearch}=${targetKeys}`,
                    }}
                  >
                    <Button label={'NEXT'} disabled={!targetKeys.length} className={'table-button'} />
                  </Link>
                </div>
              )}
              <Table
                columns={columns}
                filteredItems={filteredItems}
                rowSelection={rowSelection}
                listSelectedKeys={selectedKeys}
                onItemSelect={onItemSelect}
              />
            </div>
          );
        }}
      </Transfer>
    </div>
  );
};

export default TableTransfer;
