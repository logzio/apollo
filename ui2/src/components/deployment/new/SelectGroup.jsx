import React, { useEffect } from 'react';

const SelectGrourp = ({ handleBreadcrumbs }) => {
  useEffect(() => {
    handleBreadcrumbs('/group', 'group');
  }, []);

  return <div>hi</div>;
};

export default SelectGrourp;
