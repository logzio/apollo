import React, { useEffect } from 'react';

export const SelectGrourp = ({ handleBreadcrumbs }) => {
  useEffect(() => {
    handleBreadcrumbs('group');
  }, []);

  return <div>hi</div>;
};
