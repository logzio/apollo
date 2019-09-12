import React, { useState, useEffect } from 'react';
import { Table, Empty } from 'antd';
import { AppSearch } from '../common/Search';
import './Table.css';

export const AppTable = ({ data, searchColumns, showSearch, emptyMsg, rowSelection, handleRowSelection, ...props }) => {
  const [searchValue, setSearchValue] = useState(null);
  const [filteredData, setFilteredData] = useState(data);

  useEffect(() => {
    setFilteredData(data);
  }, [data]);

  const handleSearch = value => {
    setSearchValue(value);
    const filteredData = data.filter(dataItem =>
      searchColumns
        .map(
          colName =>
            !!dataItem[colName]
              .toString()
              .toLowerCase()
              .includes(value.toLowerCase()),
        )
        .includes(true),
    );
    setFilteredData(filteredData);
  };

  return (
    <>
      {showSearch && <AppSearch onSearch={handleSearch} onChange={handleSearch} value={searchValue} />}
      <Table
        className="app-table"
        dataSource={filteredData}
        rowSelection={rowSelection}
        size={'small'}
        pagination={false}
        onRow={handleRowSelection}
        locale={{
          emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={<span>{emptyMsg}</span>} />,
        }}
        {...props}
      />
    </>
  );
};
