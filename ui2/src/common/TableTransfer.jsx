import React from 'react';
import { Tag, Transfer } from 'antd';
import _ from 'lodash';
import Table from './Table';
import TableColumns from './TableColumns';

const TableTransfer = ({ leftColumns, rightColumns, ...props }) => (
  <Transfer showSelectAll={true} {...props}>
    {({
      direction,
      filteredItems,
      onItemSelectAll,
      onItemSelect,
      selectedKeys: listSelectedKeys,
      disabled: listDisabled,
    }) => {
      const columns = direction === 'left' ? leftColumns : rightColumns;

      const rowSelection = {
        getCheckboxProps: item => ({ disabled: listDisabled || item.disabled }),
        onSelectAll(selected, selectedRows) {
          const treeSelectedKeys = selectedRows.filter(item => !item.disabled).map(({ key }) => key);
          const diffKeys = selected
            ? _.difference(treeSelectedKeys, listSelectedKeys)
            : _.difference(listSelectedKeys, treeSelectedKeys);
          onItemSelectAll(diffKeys, selected);
        },
        onSelect({ key }, selected) {
          onItemSelect(key, selected);
        },
        selectedRowKeys: listSelectedKeys,
      };

      return (
        <Table
          columns={columns}
          filteredItems={filteredItems}
          rowSelection={rowSelection}
          listDisabled={listDisabled}
          listSelectedKeys={listSelectedKeys}
          onItemSelect={onItemSelect}
        />
      );
    }}
  </Transfer>
);

export default TableTransfer;
