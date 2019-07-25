import React, { useState } from 'react';
import { Transfer } from 'antd';
import _ from 'lodash';
import Table from './Table';
import tableColumns from '../utils/tableColumns';

const TableTransfer = ({ data, searchColumns, rightColTitles, leftColTitles, ...props }) => {
  const [targetKeys, setTargetKeys] = useState();
  const [showSearch] = useState(!!searchColumns);

  data.forEach((dataItem, index) => (dataItem.key = index.toString()));

  const handleSearch = (inputValue, item) => {
    let searchedItem = false;
    searchColumns.map(searchCol => (searchedItem = searchedItem || item[searchCol].indexOf(inputValue) !== -1));
    return searchedItem;
  };

  return (
    <Transfer
      dataSource={data}
      filterOption={(inputValue, item) => handleSearch(inputValue, item)}
      showSelectAll={true}
      targetKeys={targetKeys}
      showSearch={showSearch}
      onChange={targetKeys => setTargetKeys(targetKeys)}
      {...props}
    >
      {({ direction, filteredItems, onItemSelectAll, onItemSelect, selectedKeys }) => {
        const columns = direction === 'left' ? tableColumns(leftColTitles) : tableColumns(rightColTitles);
        const rowSelection = {
          onSelectAll: (isSelected, allRows) => {
            const allRowsKeys = allRows.map(item => item.key);
            const currentKeysSelection = isSelected
              ? _.difference(allRowsKeys, selectedKeys)
              : _.difference(selectedKeys, allRowsKeys);
            onItemSelectAll(currentKeysSelection, isSelected);
          },
          onSelect: (item, isSelected) => onItemSelect(item.key, isSelected),
          selectedRowKeys: selectedKeys,
        };

        return (
          <Table
            columns={columns}
            filteredItems={filteredItems}
            rowSelection={rowSelection}
            listSelectedKeys={selectedKeys}
            onItemSelect={onItemSelect}
          />
        );
      }}
    </Transfer>
  );
};

export default TableTransfer;
