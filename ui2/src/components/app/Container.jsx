import React, { useState } from 'react';
import { Breadcrumb } from 'antd';
import { Link } from 'react-router-dom';
import './Container.css';

export const Container = ({ title, component: Component, match, location, ...props }) => {
  const breadcrumbHomePath = [{ path: '/home', title: 'Home' }];
  const [breadcrumbs, setBreadcrumbs] = useState(breadcrumbHomePath);

  const parseSearchUrl = searchUrl =>
    searchUrl
      .split('?')
      .pop()
      .split('&')
      .map(searchParam => searchParam.split('=').shift());

  const handleBreadcrumbs = title => {
    const path = `${location.pathname}${location.search}`;
    let prevBreadcrumbs = null;
    if (location.search.length) {
      const searchTitles = parseSearchUrl(path);
      prevBreadcrumbs = searchTitles.map((searchTitle, index) => {
        const currentPath = `${match.url}/${searchTitle}`;
        const searchParamas = location.search.split(`&${searchTitle}`).shift();
        return {
          path: searchTitles[index - 1] ? `${currentPath}${searchParamas}` : `${currentPath}`,
          title: searchTitle,
        };
      });
    }

    const breadcrumbIndex = breadcrumbs.findIndex(breadcrumb => breadcrumb.path === path);
    if (~breadcrumbIndex) {
      setBreadcrumbs(breadcrumbs.slice(0, breadcrumbIndex + 1));
    } else {
      const currentBreadcrumb = { path, title };
      prevBreadcrumbs
        ? setBreadcrumbs([...breadcrumbHomePath, ...prevBreadcrumbs, currentBreadcrumb])
        : setBreadcrumbs([...breadcrumbHomePath, currentBreadcrumb]);
    }
  };

  const resetBreadcrumbs = () => {
    setBreadcrumbs(breadcrumbHomePath);
  };

  return (
    <div className="container">
      {title && (
        <>
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
        </>
      )}
      <div className="container-content">
        <Component
          handleBreadcrumbs={handleBreadcrumbs}
          resetBreadcrumbs={resetBreadcrumbs}
          match={match}
          search={location.search}
          location={location}
          {...props}
        />
      </div>
    </div>
  );
};
