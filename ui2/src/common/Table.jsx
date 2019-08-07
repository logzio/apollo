import React, { useState } from 'react';
import { Table } from 'antd';
import _ from 'lodash';
import { historyBrowser } from '../utils/history';
import { AppSearch } from '../common/Search';
import './Table.css';
import { historyBrowser } from '../utils/history';

export const AppTable = ({
  columns,
  filteredItems,
  onItemSelectAll,
  onItemSelect,
  selectedKeys,
  scroll,
  linkTo, addSearch,  ...props
}) => {
  // debugger;
  const rowSelection = {
    onSelectAll: (isSelected, allRows) => {
      const allRowsKeys = allRows && allRows.map(item => item.key);
      const currentKeysSelection = isSelected
        ? _.difference(allRowsKeys, selectedKeys)
        : _.difference(selectedKeys, allRowsKeys);
      onItemSelectAll(currentKeysSelection, isSelected);
    },
    onSelect: (item, isSelected) => onItemSelect(item.key, isSelected),
    selectedRowKeys: selectedKeys,
  };

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
    <Table
      className="app-table"
      columns={columns}
      dataSource={data}
      rowSelection={rowSelection}
      size={'small'}
      pagination={false}
      onRow={({key}) => ({
        onClick: () => onItemSelect && onItemSelect(key, !selectedKeys.includes(key)),
        onDoubleClick: () => {
          onItemSelect && onItemSelect(key, !selectedKeys.includes(key));
          historyBrowser.push({
            pathname: `${linkTo}`,
            search: `${addSearch}${key}`,
          });
        },
      })}
      scroll={scroll}
    />
  );
};
