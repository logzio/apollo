import React, { useEffect } from 'react';

export const SelectGrourp = ({ handleBreadcrumbs }) => {
  useEffect(() => {
    handleBreadcrumbs(`${window.location.href}`, 'group');
  }, []);

  return <div>hi</div>;
};
