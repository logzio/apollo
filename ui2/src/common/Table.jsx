import React, { useState, useEffect } from 'react';
import { Table, Empty } from 'antd';
import { AppSearch } from '../common/Search';
import './Table.css';
import { AppSkeleton } from './Skeleton';

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
  ...props
}) => {
  const [searchValue, setSearchValue] = useState(null);
  const [filteredData, setFilteredData] = useState(data);
  const [expandableRows, setExpandableRows] = useState(data); //TODO

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
