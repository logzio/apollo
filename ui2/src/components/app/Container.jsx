import React, { useEffect, useState } from 'react';
import { Breadcrumb } from 'antd';
import { Link } from 'react-router-dom';
import './Container.css';

export const Container = ({ title, component: Component, match, ...props }) => {
  const breadcrumbHomePath = [{ path: '/home', title: 'Home' }, { path: `${match.path}`, title: title }];

  const [breadcrumbs, setBreadcrumbs] = useState(breadcrumbHomePath);

  // useEffect(() => setBreadcrumbs(breadcrumbsInit), [Component]);

  const handleBreadcrumbs = (path, title) => {
    const pathSearch = path.split('?')[1];
    const currentBreadcrumb = path.split('/').pop();
    const breadcrumbInd = breadcrumbs.findIndex(breadcrumb => breadcrumb.path === `${match.url}/${currentBreadcrumb}`);
    let prevBreadcrumbs;
    let addPrevBreadcrumbs = true;
    if (pathSearch) {
      const searchParams = pathSearch.split('&');
      const searchTitles = searchParams.map(searchParam => searchParam.split('=')[0]);
      prevBreadcrumbs = searchTitles.map((searchTitle, index) => ({
        path: searchParams[index - 1]
          ? `${match.url}/${searchTitle}?${searchParams[index - 1]}`
          : `${match.url}/${searchTitle}`,
        title: searchTitle,
      }));
      prevBreadcrumbs.map(prevBreadcrumb => {
        breadcrumbs.map(breadcrumb => {
          if (breadcrumb.path === prevBreadcrumb.path) {
            addPrevBreadcrumbs = false;
          }
        });
      });
    }
    if (breadcrumbInd >= 0) {
      setBreadcrumbs(breadcrumbs.slice(0, breadcrumbInd + 1));
    } else {
      addPrevBreadcrumbs && prevBreadcrumbs
        ? setBreadcrumbs([...breadcrumbs, ...prevBreadcrumbs, { path: `${match.url}/${currentBreadcrumb}`, title }])
        : setBreadcrumbs([...breadcrumbs, { path: `${match.url}/${currentBreadcrumb}`, title }]);
    }



  return (
    <div className="container">
      <div className="container-title large-title">{title}</div>
      <div className="container-breadcrumbs">
        <Breadcrumb>
          {breadcrumbs.map((breadcrumb, index) => (
            <Breadcrumb.Item key={index} className="container-breadcrumb">
              <Link to={breadcrumb.path}>{breadcrumb.title}</Link>
            </Breadcrumb.Item>
          ))}
        </Breadcrumb>
      </div>
      <div className="container-content">

        <Component handleBreadcrumbs={handleBreadcrumbs} {...props} />
      </div>
    </div>
  );
};
