import React, { useEffect } from 'react';

const SelectGrourp = ({ handleBreadcrumbs, match }) => {
  useEffect(() => {
    handleBreadcrumbs(`${match.url}`, 'group');
  }, []);

  return <div>hi</div>;
};

export default SelectGrourp;
