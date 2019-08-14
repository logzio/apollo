import React, { useState, useEffect } from 'react';
import { Table, Empty } from 'antd';
import _ from 'lodash';
import { historyBrowser } from '../utils/history';
import { AppSearch } from '../common/Search';
import './Table.css';

export const AppTable = ({
  data,
  onItemSelectAll,
  onItemSelect,
  selectedKeys,
  linkTo,
  addSearch,
  setTargetKeys,
  targetKeys,
  showSelection,
  searchColumns,
  showSearch,
  emptyMsg,
  rowSelection,
  handleOnSelect,
  ...props
}) => {
  const [searchValue, setSearchValue] = useState(null);
  const [filteredData, setFilteredData] = useState(data);

  useEffect(() => {
    setFilteredData(data);
  }, [data]);

  const handleSearch = value => {
    setSearchValue(value);
    const filteredData = data.filter(dataItem => {
      return searchColumns
        .map(colName => {
          return !!dataItem[colName].includes(value);
        })
        .includes(true);
    });
    setFilteredData(filteredData);
  };


    // const handleSearch = value => {
    //     setSearchValue(value);
    //     const filteredData = data.filter(dataItem => {
    //         const test = searchColumns.map(colName => {
    //             if (dataItem[colName].toString().includes(value)) {
    //                 return true;
    //             }
    //         });
    //         return test.includes(true);
    //     });
    //     setFilteredData(filteredData);
    // };

  return (
    <>
      {showSearch && <AppSearch onSearch={handleSearch} onChange={handleSearch} value={searchValue} />}
      <Table
        className="app-table"
        dataSource={filteredData}
        rowSelection={rowSelection}
        size={'small'}
        pagination={false}
        onRow={({ key, isPartOfGroup }) => ({
          onClick: () => {
            handleOnSelect(key, isPartOfGroup, !selectedKeys.includes(key));
          },
          // onDoubleClick: () => {
          // const keys = targetKeys ? targetKeys : [];
          // onItemSelect && onItemSelect([...keys, key], !selectedKeys.includes(key));
          // setTargetKeys && setTargetKeys([...keys, key]);
          // setTimeout(
          //   () =>
          //     historyBrowser.push({
          //       pathname: `${linkTo}`,
          //       search: `${addSearch}${[...keys, key]}`,
          //     }),
          //   100,
          // );
          // },
        })}
        locale={{
          emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={<span>{emptyMsg}</span>} />,
        }}
        {...props}
      />
    </>
  );
};
