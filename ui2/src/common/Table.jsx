import React from 'react';
import { Table } from 'antd';
import Spinner from './Spinner';
// import './Table.css';

const AppTable = ({ columns, filteredItems, rowSelection, listDisabled, listSelectedKeys, onItemSelect }) => {
  // debugger;
  return (
    <Table
      columns={columns}
      dataSource={filteredItems}
      rowSelection={rowSelection}
      size="small"
      style={{ pointerEvents: listDisabled ? 'none' : null }}
      onRow={({ key, disabled: itemDisabled }) => ({
        onClick: () => {
          if (itemDisabled || listDisabled) return;
          onItemSelect(key, !listSelectedKeys.includes(key));
        },
      })}
    />
  );
};

export default AppTable;
