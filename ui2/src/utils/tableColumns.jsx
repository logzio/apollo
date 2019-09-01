import React from 'react';
import { AppTag } from '../common/Tag';

const getStatusTag = status => {
  switch (status) {
    case 'PENDING':
      return <AppTag color={'#0983F6'}>{status}</AppTag>;
    case 'STARTED':
      return <AppTag color={'#0983F6'}>{status}</AppTag>;
    case 'CANCELED':
      return <AppTag color={'#F60935'}>{status}</AppTag>;
    case 'DONE':
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

const getActionTags = (tagList, environmentId, serviceId) =>
  tagList && (
    <div>
      {tagList.map(({ color, title, onClick }, index) => (
        <AppTag
          color={color}
          key={index}
          onClick={() => {
            onClick(environmentId, serviceId);
          }}
        >
          {title}
        </AppTag>
      ))}
    </div>
  );

const costumeRender = (dataCategory, index, tagList, record, text) => {
  switch (dataCategory) {
    case 'actions':
      return getActionTags(tagList, record.environmentId, record.serviceId);
    case 'status':
      return getStatusTag(record.status);
    case 'groupName':
      return getGroupTag(record.groupName);
    default:
      return <div>{text}</div>;
  }

  // if (imgIndex === index) {
  //   return userProfile => (
  //     <div className="user-profile">
  //       <img className="image-table" src={userProfile[0]} alt="user-profile" />
  //       <div>{userProfile[1]}</div>
  //     </div>
  //   );
  // }
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
