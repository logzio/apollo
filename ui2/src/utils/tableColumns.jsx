import React from 'react';

export const transferTableColumns = (dataCategories, columnTitles) =>
  dataCategories.map((dataCategory, index) => ({
    dataIndex: dataCategory,
    title: columnTitles[index],
  }));

//Changed in future version
export const tableColumns = (dataCategories, columnTitles, imgIndex) =>
  dataCategories.map((dataCategory, index) => ({
    dataIndex: dataCategory,
    title: columnTitles[index],
    render:
      imgIndex === index &&
      (dataCategory => (
        <div className="user-profile">
          <img className="image-table" src={dataCategory[0]} alt={'user-profile'} />
          <div>{dataCategory[1]}</div>
        </div>
      )),
  }));
