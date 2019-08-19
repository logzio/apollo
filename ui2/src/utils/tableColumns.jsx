import React from 'react';

export const transferTableColumns = (dataCategories, columnTitles) =>
  dataCategories.map((dataCategory, index) => ({
    dataIndex: dataCategory,
    title: columnTitles[index],
  }));

export const tableColumns = (dataCategories, columnTitles, imgIndex) =>
  dataCategories.map((dataCategory, index) => ({
    dataIndex: dataCategory,
    title: columnTitles[index],
    width: '140px',
    render:
      imgIndex === index &&
      (dataCategory => (
        <div className="user-profile">
          <img className="image-table" src={dataCategory[0]} alt={'user-profile'} />
          <div>{dataCategory[1]}</div>
        </div>
      )),
  }));
