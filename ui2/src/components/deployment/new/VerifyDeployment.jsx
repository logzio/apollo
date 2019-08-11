import React, { useEffect } from 'react';
import { TableTransfer } from '../../../common/TableTransfer';
import { Spinner } from '../../../common/Spinner';

export const VerifyDeployment = ({ handleBreadcrumbs, resetBreadcrumbs, match, location }) => {
  useEffect(() => {
    resetBreadcrumbs();
    handleBreadcrumbs(`${location.pathname}${location.search}`, 'verify-deployment');
  }, []);

  return (
    <div>
        bi
    </div>
  );
};
