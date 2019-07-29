import React, { useState } from 'react';
import { Transfer } from 'antd';
import _ from 'lodash';
import Table from './Table';
import tableColumns from '../utils/tableColumns';
import './TableTransfer.css';

const TableTransfer = ({
  data,
  searchColumns,
  rightColTitles,
  leftColTitles,
  selectGroup,
  predefinedGroups,
  ...props
}) => {
  const [targetKeys, setTargetKeys] = useState([]);
  const [showSearch] = useState(!!searchColumns);
  const [isSelectAll, toggleSelectAll] = useState(false);
  const formattedData = data.map(dataItem => ({ ...dataItem, key: dataItem.id.toString() }));

  const handleSearch = (inputValue, item) => {
    let searchedItem = false;
    searchColumns.map(searchCol => (searchedItem = searchedItem || item[searchCol].indexOf(inputValue) !== -1));
    return searchedItem;
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
        // onSelectChange={(sourceSelectedKeys, targetSelectedKeys) => {
        //   const diffe = _.difference(targetSelectedKeys, targetKeys);
        //   targetSelectedKeys = [];
        //   return targetSelectedKeys;
        //
        // }}
        // footer={() => (
        //   <button size="small" style={{ float: 'right', margin: 5 }} onClick={() => setTargetKeys([])}>
        //     reload
        //   </button>
        // )}
        {...props}
      >
        {({ direction, filteredItems, onItemSelectAll, onItemSelect, selectedKeys }) => {
          let rowSelection = {
            onSelectAll: (isSelected, allRows) => {
              const allRowsKeys = allRows && allRows.map(item => item.key);
              // debugger;
              const currentKeysSelection = isSelected
                ? _.difference(allRowsKeys, selectedKeys)
                : _.difference(selectedKeys, allRowsKeys); //here change the selected keys and remove the group if needed
              onItemSelectAll(currentKeysSelection, isSelected);
            },
            onSelect: (item, isSelected) => onItemSelect(item.key, isSelected),
            selectedRowKeys: selectedKeys,
          };

          const handleGroupSelection = predefinedGroup => {
            const keys = selectGroup(predefinedGroup.id);
            const addedKeys = _.difference(keys, targetKeys);
            if (addedKeys.length) {
              setTargetKeys([...targetKeys, ...addedKeys]);
              // toggleSelectAll(false);
            } else {
              setTargetKeys(_.difference(targetKeys, keys));
              // toggleSelectAll(true);
            }
          };
          const columns = direction === 'left' ? tableColumns(leftColTitles) : tableColumns(rightColTitles);

          // debugger

          return (
            <div>
              {direction === 'left' &&
                predefinedGroups.map(predefinedGroup => (
                  <button
                    key={predefinedGroup.id}
                    onClick={() => {
                      handleGroupSelection(predefinedGroup);
                      // debugger;
                    }}
                  >
                    {predefinedGroup.name}
                  </button>
                ))}
              {direction === 'right' && <button onClick={() => setTargetKeys([])}>Reset</button>}
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
