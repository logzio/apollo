import React from 'react';
import { Input } from 'antd';

export const AppSearch = () => {
  return (
    <Input.Search
      placeholder="input search text"
      enterButton="Search"
      size="large"
      onSearch={(value, event) => console.log(value)}
    />
  );
};
