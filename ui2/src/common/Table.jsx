import React from 'react';
import { Table } from 'antd';
// import AppTable from './Spinner';
// import './Table.css';

const AppTable = ({ columns, filteredItems, rowSelection, listSelectedKeys, onItemSelect }) => {
  return (
    <Table
      columns={columns}
      dataSource={filteredItems}
      rowSelection={rowSelection}
      size={"small"}
      onRow={item => ({
        onClick: () => {
          onItemSelect(item.key, !listSelectedKeys.includes(item.key));
        },
      })}
    />
  );
};

export default AppTable;
