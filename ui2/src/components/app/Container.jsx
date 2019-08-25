import React, { useState } from 'react';
import { Breadcrumb } from 'antd';
import { Link } from 'react-router-dom';
import { parseUrl } from 'query-string';
import _ from 'lodash';
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

  const handleBreadcrumbs = (path, title) => {
    let prevBreadcrumbs = null;
    const { query } = parseUrl(path);

    if (!_.isEmpty(query)) {
      const queryKeys = parseSearchUrl(path);
      prevBreadcrumbs = queryKeys.map((queryKey, index) => {
        const currentPath = `${match.url}/${queryKey}`;
        const prevQueryKey = queryKeys[index - 1];
        return {
          path: prevQueryKey ? `${currentPath}?${prevQueryKey}=${query[prevQueryKey]}` : `${currentPath}`,
          title: queryKey,
        };
      });
      prevBreadcrumbs.map(prevBreadcrumb => {
        return breadcrumbs.map(({ path }) => {
          if (path === prevBreadcrumb.path) {
            prevBreadcrumbs = null;
          }
        });
      });
    }

    const breadcrumbIndex = breadcrumbs.findIndex(breadcrumb => breadcrumb.path === path);
    if (~breadcrumbIndex) {
      setBreadcrumbs(breadcrumbs.slice(0, breadcrumbIndex + 1));
    } else {
      const currentBreadcrumb = { path, title };
      prevBreadcrumbs
        ? setBreadcrumbs([...breadcrumbs, ...prevBreadcrumbs, currentBreadcrumb])
        : setBreadcrumbs([...breadcrumbs, currentBreadcrumb]);
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
        <Component
          handleBreadcrumbs={handleBreadcrumbs}
          resetBreadcrumbs={resetBreadcrumbs}
          match={match}
          location={location}
          {...props}
        />
      </div>
    </div>
  );
};
