import React from 'react';
import { Table, Tag, Transfer } from 'antd';
// import difference from 'lodash/difference';

const TableTransfer = ({ leftColumns, rightColumns, ...props }) => (
  <Transfer showSelectAll={true} {...props}>
    {({
      direction,
      filteredItems,
      // onItemSelectAll,
      onItemSelect,
      selectedKeys: listSelectedKeys,
      disabled: listDisabled,
    }) => {
      const columns = direction === 'left' ? leftColumns : rightColumns;

      // const rowSelection = {
      //   getCheckboxProps: item => ({ disabled: listDisabled || item.disabled }),
      //   onSelectAll(selected, selectedRows) {
      //     const treeSelectedKeys = selectedRows.filter(item => !item.disabled).map(({ key }) => key);
      //     const diffKeys = selected
      //       // ? difference(treeSelectedKeys, listSelectedKeys)
      //       // : difference(listSelectedKeys, treeSelectedKeys);
      //     onItemSelectAll(diffKeys, selected);
      //   },
      //   onSelect({ key }, selected) {
      //     onItemSelect(key, selected);
      //   },
      //   selectedRowKeys: listSelectedKeys,
      // };

      return (
        <Table
          rowSelection={rowSelection}
          columns={columns}
          dataSource={filteredItems}
          size="small"
          style={{ pointerEvents: listDisabled ? 'none' : null }}
          onRow={({ key, disabled: itemDisabled }) => ({
            onClick: () => {
              if (itemDisabled || listDisabled) return;
              onItemSelect(key, !listSelectedKeys.includes(key));
            },
          })}
        />
      );
    }}
  </Transfer>
);

export default TableTransfer;
