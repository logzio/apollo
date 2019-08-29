import React, { useEffect, useState } from 'react';
import { AppTable } from '../../../common/Table';
import { AppModal } from '../../../common/Modal';
import { AppInput } from '../../../common/FormFields';
import { AppButton } from '../../../common/Button';
import { Spinner } from '../../../common/Spinner';
import { tableColumns } from '../../../utils/tableColumns';
import moment from 'moment';
import { parse } from 'query-string';
import { historyBrowser } from '../../../utils/history';
import './SelectVersion.css';

export const SelectVersion = ({
  handleBreadcrumbs,
  getDeployableVersionsById,
  getLastCommitFromBranch,
  versions,
  search,
  selectVersion,
}) => {
  const [showModal, toggleShowModal] = useState(false);
  const [branchName, setBranchName] = useState(null);
  const [selectedVersion, setSelectedVersion] = useState(null);
  const { service: servicesId } = parse(search);

  useEffect(() => {
    handleBreadcrumbs('version');
    getDeployableVersionsById(servicesId);
  }, []);

  const formattedData =
    versions &&
    versions.map(({ commitDate, commitMessage, id, gitCommitSha, committerAvatarUrl, committerName, ...dataItem }) => {
      const author = [committerAvatarUrl, committerName];
      return {
        ...dataItem,
        id: id,
        key: id.toString(),
        commitDate: moment(commitDate).format('DD/MM/YY, h:mm:ss'),
        shortendCommitSha: gitCommitSha.slice(0, 7),
        gitCommitSha: gitCommitSha,
        commitMessage: commitMessage.split('*').shift(),
        committerAvatarUrl: committerAvatarUrl,
        committerName: committerName,
        commitAuthor: author,
      };
    });

  const handleVersionSelection = () => {
    selectVersion(selectedVersion);
    historyBrowser.push({
      pathname: 'verification',
      search: `${search}&version=${selectedVersion.key}`,
    });
  };

  const handleBranchSelection = branchName => {
    const versionSampleId = versions[0].id;
    getLastCommitFromBranch(branchName, versionSampleId);
    setBranchName(null);
    setSelectedVersion(null);
  };

  const handleRowSelection = version => ({
    onClick: () => {
      setSelectedVersion(version);
    },
    onDoubleClick: () => {
      setSelectedVersion(version);
      handleVersionSelection();
    },
  });

  const rowSelection = {
    onSelect: version => {
      setSelectedVersion(selectedVersion ? null : version);
    },
    selectedRowKeys: selectedVersion && selectedVersion.key,
    type: 'radio',
  };

  if (!versions) {
    return <Spinner />;
  }
  return (
    <>
      <div className="header">
        <AppButton
          onClick={() => toggleShowModal(true)}
          label="Find latest commit from branch"
          className="table-button"
        />
        <AppButton
          label="Find latest commit from master"
          className="table-button"
          onClick={() => {
            handleBranchSelection('master');
          }}
        />
        <AppButton
          type="primary"
          label="Select Version"
          className="table-submit-button"
          onClick={() => {
            handleVersionSelection();
          }}
          disabled={!selectedVersion}
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
        scroll={{ y: 750 }}
        showSearch={true}
        searchColumns={['commitDate', 'gitCommitSha', 'commitMessage', 'commitAuthor']}
        handleRowSelection={handleRowSelection}
        rowSelection={rowSelection}
      />
    </>
  );
};
