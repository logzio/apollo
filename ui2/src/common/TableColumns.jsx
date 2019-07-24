import React from 'react';
import { Button, Tag } from 'antd';
import './Button.css';
import Spinner from './Spinner';

const TableColumns = ( columns ) => {
  const list = columns.map((column, index) => ({
    dataIndex: column.title,
    title: column.title,
    key: index,
  }));

  return list;
};

export default TableColumns;
// render: '',

// columns.tag && (tag => <Tag>{tag}</Tag>)
