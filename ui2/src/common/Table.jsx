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
  ...props
}) => {
  const [searchValue, setSearchValue] = useState(null);
  const [filteredData, setFilteredData] = useState(data);

  // debugger

  useEffect(() => {
    setFilteredData(data);
  }, [data]);

  // const rowSelection = {
  //   onSelectAll: (isSelected, allRows) => {
  //     const allRowsKeys = allRows && allRows.map(item => item.key);
  //     const currentKeysSelection = isSelected
  //       ? _.difference(allRowsKeys, selectedKeys)
  //       : _.difference(selectedKeys, allRowsKeys);
  //     onItemSelectAll(currentKeysSelection, isSelected);
  //   },
  //   onSelect: (item, isSelected) => {
  //     if(item.isPartOfGroup){
  //       setSelectedGroupService(selectedGroupService ? null : item.key);
  //       return onItemSelect(item.key, isSelected);
  //     }
  //     return onItemSelect(item.key, isSelected);
  //   },
  //   selectedRowKeys: selectedKeys,
  //   getCheckboxProps: record => ({
  //     disabled: record.isPartOfGroup === true && targetKeys.length > 0 || selectedGroupService && selectedGroupService !== record.key,
  //   }),
  // };

  const handleSearch = value => {
    setSearchValue(value);
    const filteredData = data.filter(dataItem => {
      const test = searchColumns.map(colName => {
        if (dataItem[colName].includes(value)) {
          return true;
        }
      });
      return test.includes(true);
    });
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
        // onRow={({ key }) => ({
        //   onClick: () => {
        //     onItemSelect && onItemSelect(key, !selectedKeys.includes(key));
        //     // setTargetKeys && setTargetKeys([...targetKeys, key]);
        //   },
        //   onDoubleClick: () => {
        //     const keys = targetKeys ? targetKeys : [];
        //     setTargetKeys && setTargetKeys([...keys, key]);
        //     onItemSelect && onItemSelect([...keys, key], !selectedKeys.includes(key));
        //     setTimeout(
        //       () =>
        //         historyBrowser.push({
        //           pathname: `${linkTo}`,
        //           search: `${addSearch}${[...keys, key]}`,
        //         }),
        //       100,
        //     );
        //   },
        // })}
        locale={{
          emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={<div>{emptyMsg}</div>} />,
        }}
        {...props}
      />
    </>
  );
};
