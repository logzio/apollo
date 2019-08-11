import React from 'react';
import { Input } from 'antd';
import './Search.css';

export const AppSearch = ({ onSearch, onChange, value }) => {
  return (
    <Input.Search
      className="app-search"
      placeholder="input search text"
      enterButton="Search"
      onSearch={(value) => onSearch(value)}
      onChange={({ target: { value } }) => onChange(value)}
      value={value}
      size="large"
    />
  );
};
