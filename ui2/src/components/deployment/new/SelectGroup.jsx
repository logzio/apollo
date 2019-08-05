import React, { useEffect } from 'react';

const SelectGrourp = ({ handleBreadcrumbs, match }) => {
  useEffect(() => {
    handleBreadcrumbs(`${window.location.href}`, 'group');
  }, []);

  return <div>hi</div>;
};

export default SelectGrourp;
