import React, { useState } from 'react';
<<<<<<< HEAD
import { Table, Transfer } from 'antd';
=======
import {Table, Transfer} from 'antd';
>>>>>>> 21fb84b4660cf6c09a558a25820ea90d74c9772e
import _ from 'lodash';
import { AppTable } from './Table';
import { Link } from 'react-router-dom';
import { AppButton } from '../common/Button';
import { transferTableColumns } from '../utils/tableColumns';
import './TableTransfer.css';
<<<<<<< HEAD
import { historyBrowser } from '../utils/history';
=======
import {historyBrowser} from "../utils/history";
>>>>>>> 21fb84b4660cf6c09a558a25820ea90d74c9772e

export const TableTransfer = ({
  data,
  searchColumns,
  rightColTitles,
  leftColTitles,
  selectGroup,
  predefinedGroups,
  linkTo,
  addSearch,
  match,
  columnTitles,
  handleSelection,
  ...props
}) => {
  const [targetKeys, setTargetKeys] = useState([]);
  const [showSearch] = useState(!!searchColumns);
  const [selectedGroupService, setSelectedGroupService] = useState(null);
  const [selectedNonGroupService, toggleSelectedNonGroupService] = useState(false);
  const [disabledPredefinedGroups, toggleDisabledPredefinedGroups] = useState(false);
  const formattedData = data.map(({ id, ...rest }) => ({ ...rest, key: id.toString() }));

  // const handleSearch = (inputValue, item) =>
  //   searchColumns.map(searchCol => item[searchCol] && item[searchCol].indexOf(inputValue) !== -1).includes(true);

  const handleSearch = (inputValue, item) => {
    return searchColumns
      .map(searchCol => {
        const stringifiedItem = item[searchCol].toString();
        return stringifiedItem && stringifiedItem.indexOf(inputValue) !== -1;
      })
      .includes(true);
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

  const currentTable = match.url.split('/').pop();

<<<<<<< HEAD
=======
  // const onRow={({key}) => ({
  //   onClick: () => {
  //     onItemSelect && onItemSelect(key, !selectedKeys.includes(key));
  //     // setTargetKeys && setTargetKeys([...targetKeys, key]);
  //   },
  //   onDoubleClick: () => {
  //     const keys = targetKeys ? targetKeys : [];
  //     setTargetKeys && setTargetKeys([...keys, key]);
  //     onItemSelect && onItemSelect([...keys, key], !selectedKeys.includes(key));
  //     setTimeout(
  //         () =>
  //             historyBrowser.push({
  //               pathname: `${linkTo}`,
  //               search: `${addSearch}${[...keys, key]}`,
  //             }),
  //         100,
  //     );
  //   },
  // })};

>>>>>>> 21fb84b4660cf6c09a558a25820ea90d74c9772e
  return (
    <>
      <div className="submit-transfer-table">
        <Link
          to={{
            pathname: linkTo,
            search: `${addSearch}=${targetKeys}`,
          }}
        >
          <AppButton
            label={`Select your ${currentTable}`}
            disabled={!targetKeys.length}
            className={'table-submit-button'}
            type="primary"
            onClick={() => handleSelection(targetKeys)}
          />
        </Link>
      </div>
      <Transfer
        className="table-transfer"
        dataSource={formattedData}
        filterOption={handleSearch}
        showSelectAll={false}
        targetKeys={targetKeys}
        showSearch={showSearch}
        onChange={(targetKeys, direction) => {
          if (direction === 'left') {
            toggleDisabledPredefinedGroups(false);
            setSelectedGroupService(null);
          }
          setTargetKeys(targetKeys);
        }}
        titles={[`Please select at least one ${currentTable}`, `Selected items`]}
        {...props}
      >
        {({ direction, filteredItems, onItemSelectAll, onItemSelect, selectedKeys }) => {
          const leftPanel = direction === 'left';

          const columns = leftPanel
            ? transferTableColumns(leftColTitles, columnTitles)
            : transferTableColumns(rightColTitles, columnTitles);

          const scroll = leftPanel ? { x: 900, y: 580 } : { x: 400, y: 580 };

          const handleOnSelect = (key, isPartOfGroup, isSelected) => {
            if (isPartOfGroup) {
              toggleDisabledPredefinedGroups(leftPanel ? !disabledPredefinedGroups : true);
              setSelectedGroupService(selectedGroupService && leftPanel ? null : key);
              return onItemSelect(key, isSelected);
            }
            toggleSelectedNonGroupService(leftPanel);
            return onItemSelect(key, isSelected);
          };

          const handleDisabledRaws = record => {
            return (
              (record.isPartOfGroup === true && targetKeys.length > 0) ||
              (selectedGroupService && selectedGroupService !== record.key) ||
              (record.isPartOfGroup === true && selectedNonGroupService && selectedKeys.length > 0)
            );
          };

          const rowSelection = {
            onSelectAll: (isSelected, allRows) => {
              const allRowsKeys = allRows && allRows.map(item => item.key);
              const currentKeysSelection = isSelected
                ? _.difference(allRowsKeys, selectedKeys)
                : _.difference(selectedKeys, allRowsKeys);
              onItemSelectAll(currentKeysSelection, isSelected);
            },
            onSelect: ({ key, isPartOfGroup }, isSelected) => {
              handleOnSelect(key, isPartOfGroup, isSelected);
            },
            selectedRowKeys: selectedKeys,
            getCheckboxProps: record => ({
              disabled: leftPanel ? handleDisabledRaws(record) : null,
            }),
          };

          return (
            <div>
              {leftPanel && (
                <div className="header-left-transfer-table">
                  {predefinedGroups ? (
                    predefinedGroups.map(({ id, name }) => (
                      <AppButton
                        key={id}
                        label={name}
                        className={'table-button'}
                        onClick={() => handleGroupSelection(id)}
                        icon={'block'}
                        disabled={disabledPredefinedGroups}
                      />
                    ))
                  ) : (
                    <AppButton
                      label={'Select all'}
                      className={'table-button'}
                      onClick={() => setTargetKeys(formattedData.map(({ key }) => key))}
                      icon={'block'}
                    />
                  )}
                </div>
              )}
              {!leftPanel && (
                <div>
                  <AppButton
                    label={'Reset'}
                    className={'table-button'}
                    onClick={() => {
                      setTargetKeys([]);
                      setSelectedGroupService(null);
                      toggleSelectedNonGroupService(false);
                      toggleDisabledPredefinedGroups(false);
                    }}
                  />
                </div>
              )}
              <AppTable
                columns={columns}
                data={filteredItems}
                onItemSelectAll={onItemSelectAll}
                onItemSelect={onItemSelect}
                selectedKeys={selectedKeys}
                scroll={scroll}
                linkTo={linkTo}
                addSearch={`${addSearch}=`}
                setTargetKeys={setTargetKeys}
                targetKeys={targetKeys}
                rowSelection={rowSelection}
                handleOnSelect={handleOnSelect}
                {...props}
              />
            </div>
          );
        }}
      </Transfer>
    </>
  );
};
