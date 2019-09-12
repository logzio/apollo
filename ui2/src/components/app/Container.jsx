import React, { useState } from 'react';
import { Breadcrumb } from 'antd';
import { Link } from 'react-router-dom';
import { parse } from 'url';
import { parseSearchUrl } from '../../utils/parse';
import './Container.css';

export const Container = ({ title, component: Component, match, location: { search, pathname }, ...props }) => {
  const breadcrumbHomePath = [{ path: '/home', title: 'Home' }];
  const [breadcrumbs, setBreadcrumbs] = useState(breadcrumbHomePath);

  const handleBreadcrumbs = title => {
    const path = `${pathname}${search}`;
    let prevBreadcrumbs = null;
    if (search.length) {
      const { query } = parse(path);
      const searchTitles = parseSearchUrl(query);
      prevBreadcrumbs = searchTitles.map((searchTitle, index) => {
        const currentPath = `${match.url}/${searchTitle}`;
        const searchParamas = search.split(`&${searchTitle}`).shift();
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
          search={search}
          {...props}
        />
      </div>
    </div>
  );
};
