import React from 'react';
import { Table } from 'antd';
import './Table.css';

const AppTable = ({ columns, filteredItems, rowSelection, listSelectedKeys, onItemSelect }) => {
  return (
    <Table
      className="app-table"
      columns={columns}
      dataSource={filteredItems}
      rowSelection={rowSelection}
      size={'small'}
      pagination={false}
      onRow={item => ({
        onClick: () => onItemSelect(item.key, !listSelectedKeys.includes(item.key)),
      })}
      scroll={{ x: 400, y: 500 }}
    />
  );
};

export default AppTable;
