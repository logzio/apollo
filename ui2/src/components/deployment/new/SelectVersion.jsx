import React, { useEffect } from 'react';
import { AppTable } from '../../../common/Table';
import { Spinner } from '../../../common/Spinner';

export const SelectVersion = ({ handleBreadcrumbs, getDeployableVersion, versions, match, location }) => {
    const servicesId = location.search.split('&')[0].split('=')[1];
    // debugger
  useEffect(() => {
    handleBreadcrumbs(`${window.location.href}`, 'environment');
      getDeployableVersion(servicesId);
  }, []);

  if (!versions) {
    return <Spinner />;
  }

  return (
    <AppTable
      data={versions}
      // searchColumns={['name', 'geoRegion', 'availability', 'kubernetesMaster']}
      // leftColTitles={['name', 'geoRegion', 'availability', 'kubernetesMaster']}
      // rightColTitles={['name']}
      linkTo={'group'}
      addSearch={`${location.search}&version`}
    />
  );
};
