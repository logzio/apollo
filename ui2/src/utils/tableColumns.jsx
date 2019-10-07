import React from 'react';
import { AppTag } from '../common/Tag';
import { AppButton } from '../common/Button';
import { AppEllipsis } from '../common/Ellipsis';
import { deploymentStatus, category, tagListTitles } from './tableConfig';
import { countBy, isString } from 'lodash';

const getStatusTag = (status, groupRecords) => {
  if (groupRecords) {
    const finishedDeploymentSize = countBy(groupRecords, category.STATUS)[deploymentStatus.DONE];
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
      return <AppTag color={'#d9534f'}>{status}</AppTag>;
    case deploymentStatus.CANCELING:
      return <AppTag color={'#f0ad4e'}>{status}</AppTag>;
    case deploymentStatus.DONE:
      return <AppTag color={'#49A446'}>{status}</AppTag>;
    default:
      return <AppTag>{status}</AppTag>;
  }
};

const getActionBadges = (tagList, { status, groupName, ...rest }) => {
  return (
    tagList && (
      <div>
        {tagList.map(({ title, onClick }, index) => {
          const isRevertable = status === deploymentStatus.STARTED || status === deploymentStatus.PENDING;
          const isPartOfGroup = !!rest.groupRecords;
          const actionButton = (icon, className, tooltipText) => (
            <AppButton
              type={'primary'}
              shape={'circle'}
              icon={icon}
              size={'small'}
              key={index}
              onClick={() => {
                onClick(rest);
              }}
              className={`table-action-button ${className}`}
              tooltipText={tooltipText}
            />
          );
          switch (title) {
            case tagListTitles.LOGS:
              return !isPartOfGroup && actionButton('eye', 'view-logs', tagListTitles.LOGS);
            case tagListTitles.REVERT:
              return isRevertable && actionButton('undo', 'revert', tagListTitles.REVERT);
            case tagListTitles.GROUP:
              return isPartOfGroup && actionButton('info', 'group', tagListTitles.GROUP);
            case tagListTitles.DETAILS:
              return actionButton('info', 'details', tagListTitles.DETAILS);
            case tagListTitles.BACK:
              return actionButton('undo', 'back', tagListTitles.BACK);
            case tagListTitles.STATUS:
              return actionButton('history', 'env-status', tagListTitles.STATUS);
            default:
              return actionButton('check');
          }
        })}
      </div>
    )
  );
};

const getAuthorProfile = (userProfileUrl, userProfileName) => (
  <div className="user-profile">
    {userProfileUrl && <img className="image-table" src={userProfileUrl} alt="user-profile" />}
    {userProfileName && <div>{userProfileName}</div>}
  </div>
);

const handleEllipsisText = (text, limitLength) => (
  <AppEllipsis tooltipText={text} isEllipsis={text.length > limitLength}>
    {text}
  </AppEllipsis>
);

const costumeRender = (dataCategory, index, record, text, tagList) => {
  switch (dataCategory) {
    case category.ACTIONS:
      return getActionBadges(tagList, record);
    case category.STATUS:
      return getStatusTag(record.status, record.groupRecords);
    case category.AUTHOR:
      return getAuthorProfile(record.committerAvatarUrl, record.committerName);
    case category.MESSAGE:
      return handleEllipsisText(record.deploymentMessage, 30);
    case category.KUBERNETES:
      return handleEllipsisText(record.kubernetesMaster, 30);
    case category.PARAMS:
      return handleEllipsisText(record.jsonParams, 40);
    default:
      return <div>{text}</div>;
  }
};

const handleSort = (recordA, recordB, dataCategory) => {
  const valueA = recordA[dataCategory];
  const valueB = recordB[dataCategory];
  if (!isString(valueA) || !isString(valueB)) {
    return;
  }
  return valueA.localeCompare(valueB);
};

export const tableColumns = (dataCategories, columnTitles, tagList) =>
  dataCategories.map((dataCategory, index) => ({
    dataIndex: dataCategory,
    title: columnTitles[index],
    className: dataCategory,
    sorter: (recordA, recordB) => handleSort(recordA, recordB, dataCategory),
    render: (text, record) => costumeRender(dataCategory, index, record, text, tagList),
  }));
