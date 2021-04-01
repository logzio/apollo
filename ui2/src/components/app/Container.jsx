import React, { useState } from 'react';
import { Breadcrumb } from 'antd';
import { Link } from 'react-router-dom';
import './Container.css';

export const Container = ({ title, component: Component, match, ...props }) => {
  const breadcrumbHomePath = [{ path: '/home', title: 'Home' }];
  const [breadcrumbs, setBreadcrumbs] = useState(breadcrumbHomePath);

  const handleBreadcrumbs = (path, title) => {
    let prevBreadcrumbs = null;
    const [url, searchUrl] = path.split('?');
    const currentBreadcrumb = url.split('/').pop();
    const currentBreadcrumbPath = `${match.url}/${currentBreadcrumb}`;

    if (searchUrl) {
      const searchParams = searchUrl.split('&');
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
            prevBreadcrumbs = null;
          }
        });
      });
    }

    const breadcrumbInd = breadcrumbs.findIndex(({ path }) => path === currentBreadcrumbPath);
    if (~breadcrumbInd) {
      setBreadcrumbs(breadcrumbs.slice(0, breadcrumbInd + 1));
    } else {
      prevBreadcrumbs
        ? setBreadcrumbs([...breadcrumbs, ...prevBreadcrumbs, { path: `${match.url}/${currentBreadcrumb}`, title }])
        : setBreadcrumbs([...breadcrumbs, { path: `${match.url}/${currentBreadcrumb}`, title }]);
    }
  };

  const resetBreadcrumbs = () => {
    setBreadcrumbs(breadcrumbHomePath);
  };

  return (
    <div className="container">
      <div className="container-title large-title">{title}</div>
      <div className="container-breadcrumbs">
        <Breadcrumb>
          {breadcrumbs.map(({ path, title }, index) => (
            <Breadcrumb.Item key={index} className="container-breadcrumb">
              <Link to={path}>{title}</Link>
            </Breadcrumb.Item>
          ))}
        </Breadcrumb>
      </div>
      <div className="container-content">
        <Component handleBreadcrumbs={handleBreadcrumbs} resetBreadcrumbs={resetBreadcrumbs} match={match} {...props} />
      </div>
    </div>
  );
};
