import React from 'react';
import { Table, Search } from 'antd';
import _ from 'lodash';
import './Table.css';
import { historyBrowser } from '../utils/history';

export const AppTable = ({ columns, data, onItemSelectAll, onItemSelect, selectedKeys, scroll, linkTo, addSearch, ...props }) => {
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
      columns={columns}
      dataSource={data}
      rowSelection={rowSelection}
      size={'small'}
      pagination={false}
      onRow={({key}) => ({
        onClick: () => onItemSelect && onItemSelect(key, !selectedKeys.includes(key)),
        onDoubleClick: () => {
          onItemSelect && onItemSelect(key, !selectedKeys.includes(key));
          historyBrowser.push({
            pathname: `${linkTo}`,
            search: `${addSearch}${key}`,
          });
        },
      })}
      {...props}
    />
  );
};
