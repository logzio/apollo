import React, { useEffect, useState } from 'react';
import { AppTable } from '../../../common/Table';
import { AppModal } from '../../../common/Modal';
import { AppInput } from '../../../common/FormFields';
import { AppButton } from '../../../common/Button';
import { tableColumns } from '../../../utils/tableColumns';
import { parse } from 'query-string';
import { historyBrowser } from '../../../utils/history';
import moment from 'moment';
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
    versions.map(({ commitDate, commitMessage, id, gitCommitSha, ...dataItem }) => {
      return {
        ...dataItem,
        id: id,
        key: id.toString(),
        commitDate: commitDate && moment(commitDate).format('DD/MM/YY, h:mm:ss'),
        shortendCommitSha: gitCommitSha && gitCommitSha.slice(0, 7),
        gitCommitSha: gitCommitSha,
        commitMessage: commitMessage && commitMessage.split('*').shift(),
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

  return (
    <div className={'select-version'}>
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
          ['commitDate', 'shortendCommitSha', 'commitMessage', 'author'],
          ['Date', 'Commit', 'Message', 'Author'],
          3,
        )}
        data={formattedData}
        scroll={{ y: 690 }}
        showSearch={true}
        searchColumns={['commitDate', 'gitCommitSha', 'commitMessage', 'committerName']}
        handleRowSelection={handleRowSelection}
        rowSelection={rowSelection}
        pagination={false}
      />
    </div>
  );
};
