import React from 'react';
import { AppTag } from '../common/Tag';
import { AppButton } from '../common/Button';
import { deploymentStatus, category, tagListTitles } from './tableConfig';
import _ from 'lodash';

const getStatusTag = (status, groupRecords) => {
  if (groupRecords) {
    const finishedDeploymentSize = _.countBy(groupRecords, category.STATUS)[deploymentStatus.DONE];
    return <AppTag color={'#40C9BA'}>{`${finishedDeploymentSize}/${groupRecords.length}`}</AppTag>;
  }
  switch (status) {
    case deploymentStatus.PENDING_CANCELLATION:
      return <AppTag color={'#f0ad4e'}>{status}</AppTag>;
    case deploymentStatus.PENDING:
      return <AppTag color={'#f0ad4e'}>{status}</AppTag>;
    case deploymentStatus.STARTED:
      return <AppTag color={'#0983F6'}>{status}</AppTag>;
    case deploymentStatus.CANCELED:
      return <AppTag color={'#F60935'}>{status}</AppTag>;
    case deploymentStatus.CANCELING:
      return <AppTag color={'#f0ad4e'}>{status}</AppTag>;
    case deploymentStatus.DONE:
      return <AppTag color={'#49A446'}>{status}</AppTag>;
    default:
      return <AppTag>{status}</AppTag>;
  }
};

const getActionTags = (tagList, { status, groupName, ...rest }) => {
  return (
    tagList && (
      <div>
        {tagList.map(({ color, title, onClick }, index) => {
          const isRevertable = status === deploymentStatus.STARTED || status === deploymentStatus.PENDING;
          const isPartOfGroup = rest.groupRecords;
          const actionTag = icon => (
            <AppButton
              type={'primary'}
              shape={'circle'}
              icon={icon}
              size={'small'}
              key={index}
              onClick={() => {
                onClick(rest);
              }}
              className={'tag'}
            />
          );
          switch (title) {
            case tagListTitles.LOGS:
              return !isPartOfGroup && actionTag('eye');
            case tagListTitles.REVERT:
              return isRevertable && actionTag('download');
            case tagListTitles.GROUP:
              return isPartOfGroup && actionTag('download');
            default:
              return actionTag('eye');
          }
        })}
      </div>
    )
  );
};

const getAuthorProfile = (userProfileUrl, userProfileName) => (
  <div className="user-profile">
    <img className="image-table" src={userProfileUrl} alt="user-profile" />
    <div>{userProfileName}</div>
  </div>
);

const costumeRender = (dataCategory, index, tagList, record, text) => {
  switch (dataCategory) {
    case category.ACTIONS:
      return getActionTags(tagList, record);
    case category.STATUS:
      return getStatusTag(record.status, record.groupRecords);
    case category.AUTHOR:
      return getAuthorProfile(record.committerAvatarUrl, record.committerName);
    default:
      return <div>{text}</div>;
  }
};

export const transferTableColumns = (dataCategories, columnTitles) =>
  dataCategories.map((dataCategory, index) => ({
    dataIndex: dataCategory,
    title: columnTitles[index],
  }));

export const tableColumns = (dataCategories, columnTitles, tagList) =>
  dataCategories.map((dataCategory, index) => ({
    dataIndex: dataCategory,
    title: columnTitles[index],
    className: dataCategory,
    sorter: (recordA, recordB) => {
      const valueA = recordA[dataCategory];
      const valueB = recordB[dataCategory];
      if (!_.isString(valueA) || !_.isString(valueB)) {
        return;
      }
      return valueA.localeCompare(valueB);
    },
    render: (text, record) => costumeRender(dataCategory, index, tagList, record, text),
  }));
