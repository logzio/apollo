import React from 'react';
import { AppTag } from '../common/Tag';
import { deploymentStatus, category } from './tableConfig';

const getStatusTag = status => {
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
      return <AppTag color={'#F18D04'}>{status}</AppTag>;
    case deploymentStatus.DONE:
      return <AppTag color={'#49A446'}>{status}</AppTag>;
    default:
      return <AppTag>{status}</AppTag>;
  }
};

const getGroupTag = group => {
  if (group === 'Non') {
    return <div>Non</div>;
  } else {
    return <AppTag tooltipText={group}>{group}</AppTag>;
  }
};

const getActionTags = (tagList, { status, ...rest }) =>
  tagList && (
    <div>
      {tagList.map(({ color, title, onClick }, index) => {
        const isRevertable = (status === deploymentStatus.STARTED && title === 'Revert') || title !== 'Revert'; //Antd table doesn't support disabling of tags
        return (
          isRevertable && (
            <AppTag
              color={color}
              key={index}
              onClick={() => {
                onClick(rest);
              }}
            >
              {title}
            </AppTag>
          )
        );
      })}
    </div>
  );

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
      return getStatusTag(record.status);
    case category.GROUP:
      return getGroupTag(record.group);
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
    render: (text, record) => costumeRender(dataCategory, index, tagList, record, text),
  }));
