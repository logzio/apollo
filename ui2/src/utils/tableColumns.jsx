import React from 'react';

export const transferTableColumns = dataCategories =>
  dataCategories.map(dataCategory => ({
    dataIndex: dataCategory,
    title: dataCategory.charAt(0).toUpperCase() + dataCategory.substring(1),
  }));

export const tableColumns = (dataCategories, columnTitles, imgIndex) =>
  dataCategories.map((dataCategory, index) => ({
    dataIndex: dataCategory,
    title: columnTitles[index],
    render: imgIndex === index && (dataCategory => <img className="image-table" src={dataCategory} />),
  }));
