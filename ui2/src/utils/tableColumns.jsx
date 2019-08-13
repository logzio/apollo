import React from 'react';
import { AppButton } from '../common/Button';
import { Tag } from 'antd';

const renderStaff = (dataCategory, imgIndex, index, tagList) => {
  if (dataCategory === 'actions') {
    return (
      tagList &&
      (() =>
        tagList && (
          <div>
            {tagList.map(({ color, title }) => (
              <Tag color={color}>{title}</Tag>
            ))}
          </div>
        ))
    );
  }

  if (dataCategory === 'status') {
    return text => <Tag color={'#49A446'}>{text}</Tag>;
  }

  if (imgIndex === index) {
    return userProfile => (
      <div className="user-profile">
        <img className="image-table" src={userProfile[0]} alt="user-profile" />
        <div>{userProfile[1]}</div>
      </div>
    );
  }
};

export const transferTableColumns = (dataCategories, columnTitles) =>
  dataCategories.map((dataCategory, index) => ({
    dataIndex: dataCategory,
    title: columnTitles[index],
  }));

export const tableColumns = (dataCategories, columnTitles, imgIndex, tagList) =>
  dataCategories.map((dataCategory, index) => ({
    dataIndex: dataCategory,
    title: columnTitles[index],
    width: '140px',
    render: renderStaff(dataCategory, imgIndex, index, tagList),
  }));
