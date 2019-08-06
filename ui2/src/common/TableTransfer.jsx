import React, { useState } from 'react';
import { Transfer } from 'antd';
import _ from 'lodash';
import { AppTable } from './Table';
import { Link } from 'react-router-dom';
import { AppButton } from '../common/Button';
import { tableColumns } from '../utils/tableColumns';
import './TableTransfer.css';

export const TableTransfer = ({
  data,
  searchColumns,
  rightColTitles,
  leftColTitles,
  selectGroup,
  predefinedGroups,
  linkTo,
  addSearch,
  ...props
}) => {
  const [targetKeys, setTargetKeys] = useState([]);
  const [showSearch] = useState(!!searchColumns);
  const formattedData = data.map(dataItem => ({ ...dataItem, key: dataItem.id.toString() }));

  const handleSearch = (inputValue, item) => {
    return searchColumns.map(searchCol => item[searchCol] && item[searchCol].indexOf(inputValue) !== -1).includes(true);
  };
  const handleGroupSelection = predefinedGroupId => {
    const keys = selectGroup(predefinedGroupId);
    const addedKeys = _.difference(keys, targetKeys);
    if (addedKeys.length) {
      setTargetKeys([...targetKeys, ...addedKeys]);
    } else {
      setTargetKeys(_.difference(targetKeys, keys));
    }
  };

  return (
    <Transfer
      className="table-transfer"
      dataSource={formattedData}
      filterOption={(inputValue, item) => handleSearch(inputValue, item)}
      showSelectAll={false}
      targetKeys={targetKeys}
      showSearch={showSearch}
      onChange={targetKeys => setTargetKeys(targetKeys)}
      {...props}
    >
      {({ direction, filteredItems, onItemSelectAll, onItemSelect, selectedKeys }) => {
        const columns = direction === 'left' ? tableColumns(leftColTitles) : tableColumns(rightColTitles);
        const scroll = direction === 'left' ? { x: 900, y: 600 } : { x: 400, y: 600 };

        return (
          <div>
            {direction === 'left' && (
              <>
                {predefinedGroups.map(({ id, name }) => (
                  <AppButton key={id} label={name} className={'table-button'} onClick={() => handleGroupSelection(id)} />
                ))}
                <Link
                  to={{
                    pathname: linkTo,
                    search: `${addSearch}=${targetKeys}`,
                  }}
                >
                  <AppButton label={'NEXT'} disabled={!targetKeys.length} className={'table-button'} />
                </Link>
              </>
            )}
            {direction === 'right' && (
              <div>
                <AppButton label={'Reset'} className={'table-button'} onClick={() => setTargetKeys([])} />
              </div>
            )}
            <AppTable
              columns={columns}
              filteredItems={filteredItems}
              onItemSelectAll={onItemSelectAll}
              onItemSelect={onItemSelect}
              selectedKeys={selectedKeys}
              scroll={scroll}
              linkTo={linkTo}
            />
          </div>
        );
      }}
    </Transfer>
  );
};
