import React, { useEffect } from 'react';
import TableTransfer from '../../../common/TableTransfer';
import Spinner from '../../../common/Spinner';
import { Link } from 'react-router-dom';

const SelectService = ({ getServices, services, handleBreadcrumbs, match }) => {
  useEffect(() => {
    getServices();
  }, [getServices]);

  useEffect(() => {
    handleBreadcrumbs(`${match.url}`, 'service');
  }, []);

  if (!services) {
    return <Spinner />;
  }

  return (
    <div>
      <button>
        <Link to={'environment'}>NEXT</Link>
      </button>
      <TableTransfer data={services} searchColumns={['name']} rightColTitles={['name']} leftColTitles={['name']} />
    </div>
  );
};

export default SelectService;
