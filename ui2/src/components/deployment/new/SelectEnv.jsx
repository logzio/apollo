import React, { useEffect, useState } from 'react';

const NewDeploymentComponent = ({ addBreadcrumb }) => {
  useEffect(() => {
    addBreadcrumb('/environment', 'env');
  }, []);

  return <div>hi</div>;
};

export default NewDeploymentComponent;
