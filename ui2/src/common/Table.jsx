import React, { useState, useEffect } from 'react';
import { Table, Empty } from 'antd';
import { AppSearch } from '../common/Search';
import { AppSkeleton } from './Skeleton';
import './Table.css';

export const AppTable = ({
  data,
  searchColumns,
  showSearch,
  emptyMsg,
  rowSelection,
  handleRowSelection,
  rowClassName,
  expandableColumn,
  expandIconAsCell,
  expandable,
  pagination,
  handleSearch,
  searchValue,
  ...props
}) => {
  const [defaultSearchValue, setDefaultSearchValue] = useState(null);
  const [filteredData, setFilteredData] = useState(data);

  useEffect(() => {
    setFilteredData(data);
  }, [data]);

  const defaultHandleSearch = searchValue => {
    setDefaultSearchValue(searchValue);
    const filteredData = data.filter(dataItem =>
      searchColumns
        .map(
          colName =>
            !!dataItem[colName]
              .toString()
              .toLowerCase()
              .includes(searchValue.toLowerCase()),
        )
        .includes(true),
    );
    setFilteredData(filteredData);
  };

  return (
    <>
      {showSearch && (
        <AppSearch
          onSearch={handleSearch ? handleSearch : defaultHandleSearch}
          onChange={handleSearch ? handleSearch : defaultHandleSearch}
          value={searchValue ? searchValue : defaultSearchValue}
        />
      )}
      {!filteredData ? (
        <AppSkeleton />
      ) : (
        <Table
          className="app-table"
          dataSource={filteredData}
          rowSelection={rowSelection}
          size={'small'}
          pagination={pagination}
          onRow={handleRowSelection}
          locale={{
            emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={<span>{emptyMsg}</span>} />,
          }}
          {...props}
        />
      )}
    </>
  );
};
