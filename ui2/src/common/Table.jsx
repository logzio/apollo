import React from 'react';
import { Table } from 'antd';
import _ from 'lodash';
import './Table.css';

export const AppTable = ({ filteredItems, onItemSelectAll, onItemSelect, selectedKeys, ...props }) => {
  const rowSelection = {
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

  return (
    <Table
      className="app-table"
      dataSource={filteredItems}
      rowSelection={rowSelection}
      size={'small'}
      pagination={false}
      onRow={item => ({
        onClick: () => onItemSelect(item.key, !selectedKeys.includes(item.key)),
      })}
      {...props}
    />
  );
};
