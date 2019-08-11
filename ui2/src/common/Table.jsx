import React, { useState } from 'react';
import { Table } from 'antd';
import _ from 'lodash';
import { historyBrowser } from '../utils/history';
import { AppSearch } from '../common/Search';
import './Table.css';

export const AppTable = ({
  columns,
  data,
  onItemSelectAll,
  onItemSelect,
  selectedKeys,
  scroll,
  linkTo,
  addSearch,
  setTargetKeys,
  targetKeys,
  searchColumns,
  showSearch
    , ...props
}) => {
  const [searchValue, setSearchValue] = useState(null);
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

  const handleSearch = () => {
    data.map(test => {
      debugger;
      console.log(test[searchColumns[0]]);
    });
  };

  return (
    <>
      {showSearch && <AppSearch onSearch={handleSearch} onChange={setSearchValue} value={searchValue} />}
      <Table
        className="app-table"
        columns={columns}
        dataSource={data}
        rowSelection={rowSelection}
        size={'small'}
        pagination={false}
        onRow={({ key }) => ({
          onClick: () => {
            onItemSelect && onItemSelect(key, !selectedKeys.includes(key));
            // setTargetKeys && setTargetKeys([...targetKeys, key]);
          },
          onDoubleClick: () => {
            setTargetKeys && setTargetKeys([...targetKeys, key]);
            onItemSelect && onItemSelect([...targetKeys, key], !selectedKeys.includes(key));
            setTimeout(
              () =>
                historyBrowser.push({
                  pathname: `${linkTo}`,
                  search: `${addSearch}${[...targetKeys, key]}`,
                }),
              100,
            );
          },
        })}
        scroll={scroll}
        {...props}
      />
    </>
  );
};
