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
<<<<<<< HEAD
  rowSelection,
  handleOnSelect,
=======
>>>>>>> 21fb84b4660cf6c09a558a25820ea90d74c9772e
  rowClassName,
  expandableColumn,
  expandIconAsCell,
  ...props
}) => {
  const [searchValue, setSearchValue] = useState(null);
  const [filteredData, setFilteredData] = useState(data);
  const [expandableRows, setExpandableRows] = useState(data); //TODO

  useEffect(() => {
    setFilteredData(data);
  }, [data]);
<<<<<<< HEAD
=======

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
>>>>>>> 21fb84b4660cf6c09a558a25820ea90d74c9772e

  const handleSearch = value => {
    setSearchValue(value);
    const filteredData = data.filter(dataItem => {
      const test = searchColumns.map(colName => {
        if (dataItem[colName].toString().includes(value)) {
          return true;
        }
      });
      return test.includes(true);
    });
    setFilteredData(filteredData);
  };

<<<<<<< HEAD
  // const handleSearch = value => {
  //     setSearchValue(value);
  //     const filteredData = data.filter(dataItem => {
  //         return searchColumns
  //             .map(colName => {
  //                 return !!dataItem[colName].includes(value);
  //             })
  //             .includes(true);
  //     });
  //     setFilteredData(filteredData);
  // };

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

=======
>>>>>>> 21fb84b4660cf6c09a558a25820ea90d74c9772e
  const onExpand = (expanded, { key }) => {
    //TODO
    const rowKeyInd = expandableRows.findIndex(({ key: rowKey }) => rowKey === key);
    if (~rowKeyInd) {
      setExpandableRows(expandableRows.splice(rowKeyInd, 1));
    } else {
      setExpandableRows([...expandableRows, key]);
    }
  };
<<<<<<< HEAD
=======

>>>>>>> 21fb84b4660cf6c09a558a25820ea90d74c9772e
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
        expandedRowRender={(record, index) => index === 3 && <p style={{ margin: 0 }}>hi</p>}
        expandIconColumnIndex={expandableColumn}
        expandIconAsCell={expandIconAsCell}
        onExpand={onExpand}
        rowClassName={rowClassName}
        {...props}
      />
    </>
  );
};
