import React from 'react';
import { AppTag } from '../common/Tag';
import { deploymentStatus, category, tagListTitles } from './tableConfig';
import _ from 'lodash';

const getStatusTag = (status, groupRecords) => {
  if (groupRecords) {
    const finishedDeploymentSize = _.countBy(groupRecords, category.STATUS)[deploymentStatus.DONE];
    return <AppTag color={'#40C9BA'}>{`${finishedDeploymentSize}/${groupRecords.length}`}</AppTag>;
  }
  switch (status) {
    case deploymentStatus.PENDING_CANCELLATION:
      return <AppTag color={'#F18D04'}>{status}</AppTag>;
    case deploymentStatus.PENDING:
      return <AppTag color={'#F18D04'}>{status}</AppTag>;
    case deploymentStatus.STARTED:
      return <AppTag color={'#0983F6'}>{status}</AppTag>;
    case deploymentStatus.CANCELED:
      return <AppTag color={'#F60935'}>{status}</AppTag>;
    case deploymentStatus.CANCELING:
      return !<AppTag color={'#F18D04'}>{status}</AppTag>;
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
          const actionTag = (
            <AppTag
              color={color}
              key={index}
              onClick={() => {
                onClick(rest);
              }}
            >
              {title}
            </AppTag>
          );
          switch (title) {
            case tagListTitles.LOGS:
              return !isPartOfGroup && actionTag;
            case tagListTitles.REVERT:
              return isRevertable && actionTag;
            case tagListTitles.GROUP:
              return isPartOfGroup && actionTag;
            default:
              return actionTag;
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
    sorter: (a, b) => a[dataCategory].localeCompare(b[dataCategory]),
    render: (text, record) => costumeRender(dataCategory, index, tagList, record, text),
  }));
