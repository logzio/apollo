import React, { useState, useEffect } from 'react';
import { Table, Empty } from 'antd';
import { AppSearch } from '../common/Search';
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

  const onExpand = (expanded, { key }) => {
    //TODO
    const rowKeyInd = expandableRows.findIndex(({ key: rowKey }) => rowKey === key);
    if (~rowKeyInd) {
      setExpandableRows(expandableRows.splice(rowKeyInd, 1));
    } else {
      setExpandableRows([...expandableRows, key]);
    }
  };
debugger
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
        expandedRowRender={(record, index) => index === 3 && <p style={{ margin: 0 }}>hi</p>}
        expandIconColumnIndex={expandableColumn ? expandableColumn : null}
        expandIconAsCell={expandIconAsCell}
        onExpand={onExpand}
        rowClassName={rowClassName}
        {...props}
      />
    </>
  );
};
