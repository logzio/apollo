import React, { useEffect, useState } from 'react';
import { AppTable } from '../../../common/Table';
import { AppModal } from '../../../common/Modal';
import { AppInput } from '../../../common/FormFields';
import { AppButton } from '../../../common/Button';
import { Spinner } from '../../../common/Spinner';
import { tableColumns } from '../../../utils/tableColumns';
import moment from 'moment';
import './SelectVersion.css';

export const SelectVersion = ({
  handleBreadcrumbs,
  getDeployableVersionById,
  getLastCommitFromBranch,
  versions,
  location,
}) => {
  const [, servicesId] = location.search
    .split('&')
    .shift()
    .split('=');
  useEffect(() => {
    handleBreadcrumbs(`${window.location.href}`, 'version');
    getDeployableVersionById(servicesId);
  }, []);
  const [showModal, toggleShowModal] = useState(false);
  const [branchName, setBranchName] = useState(null);

  const formattedData =
    versions &&
    versions.map(({ commitDate, commitMessage, id, gitCommitSha, committerAvatarUrl, committerName, ...dataItem }) => {
      const author = [committerAvatarUrl, committerName];
      return {
        ...dataItem,
        key: id.toString(),
        commitDate: moment(commitDate).format('DD/MM/YY, h:mm:ss'),
        shortendCommitSha: gitCommitSha.slice(0, 7),
        gitCommitSha: gitCommitSha,
        commitMessage: commitMessage.split('*').shift(),
        commitAuthor: author,
      };
    });

  const handleBranchSelection = branchName => {
    const versionSampleId = versions[0].id;
    getLastCommitFromBranch(branchName, versionSampleId);
    setBranchName(null);
  };

  if (!versions) {
    return <Spinner />;
  }
  return (
    <>
      <div className="header">
        <AppButton
          type="primary"
          onClick={() => toggleShowModal(true)}
          label="Find latest commit from branch"
          className="table-submit-button"
        />
        <AppButton
          type="primary"
          label="Find latest commit on master"
          className="table-submit-button"
          onClick={() => {
            handleBranchSelection('master');
          }}
        />
      </div>
      <AppModal
        visible={showModal}
        toggleModal={toggleShowModal}
        title="Find my commit"
        onOk={() => {
          handleBranchSelection(branchName);
          toggleShowModal(false);
          setBranchName(null);
        }}
        okDisabled={!branchName}
      >
        <AppInput
          placeholder="Enter branch name"
          onChange={({ target: { value } }) => setBranchName(value)}
          value={branchName}
        />
      </AppModal>
      <AppTable
        columns={tableColumns(
          ['commitDate', 'shortendCommitSha', 'commitMessage', 'commitAuthor'],
          ['Date', 'Commit', 'Message', 'Author'],
          3,
        )}
        data={formattedData}
        linkTo={'group'}
        scroll={{ y: 750 }}
        addSearch={`${location.search}&version=`}
        showSearch={true}
        searchColumns={['commitDate', 'gitCommitSha', 'commitMessage', 'commitAuthor']}
        showSelection={false}
      />
    </>
  );
};
