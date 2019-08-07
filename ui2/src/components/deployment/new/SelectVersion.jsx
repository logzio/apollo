import React, { useEffect } from 'react';
import { AppTable } from '../../../common/Table';
import { AppSearch } from '../../../common/Search';
import { Spinner } from '../../../common/Spinner';
import { tableColumns } from '../../../utils/tableColumns';
import moment from 'moment';

export const SelectVersion = ({
  handleBreadcrumbs,
  getDeployableVersionById,
  getDeployableVersionBySha,
  versions,
  match,
  location,
}) => {
  const [, servicesId] = location.search.split('&')[0].split('=');
  useEffect(() => {
    handleBreadcrumbs(`${window.location.href}`, 'version');
    getDeployableVersionById(servicesId);
  }, []);

  const formattedData =
    versions &&
    versions.map(({ commitDate, gitCommitSha, commitMessage, id, ...dataItem }) => ({
      ...dataItem,
      key: id.toString(),
      commitDate: moment(commitDate).format('DD/MM/YY, h:mm:ss'),
      gitCommitSha: gitCommitSha.slice(0, 7),
      commitMessage: commitMessage.split('*').shift(),
    }));

  if (!versions) {
    return <Spinner />;
  }

  return (
    <>
      {/*<AppSearch />*/}
      <AppTable
        columns={tableColumns(
          ['commitDate', 'gitCommitSha', 'commitMessage', 'committerAvatarUrl', 'committerName'],
          ['Date', 'Commit', 'Message', 'Author'],
          3,
        )}
        data={formattedData}
        // searchColumns={['name', 'geoRegion', 'availability', 'kubernetesMaster']}
        linkTo={'group'}
        scroll={{ y: 750 }}
        addSearch={`${location.search}&version=`}
      />
    </>
  );
};
