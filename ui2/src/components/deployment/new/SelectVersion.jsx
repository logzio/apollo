import React, { useEffect, useState } from 'react';
import { AppTable } from '../../../common/Table';
import { AppSearch } from '../../../common/Search';
import { AppModal } from '../../../common/Modal';
import { InputField } from '../../../common/FormFields';
import { AppButton } from '../../../common/Button';
import { Spinner } from '../../../common/Spinner';
import { tableColumns } from '../../../utils/tableColumns';
import moment from 'moment';
import { Input } from 'antd';
import './SelectVersion.css';

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
  const [showModal, toggleShowModal] = useState(false);
  const [gitCommitSha, setGitCommitSha] = useState(null);
  const [branchName, setBranchName] = useState(null);

  const formattedData =
    versions &&
    versions.map(({ commitDate, gitCommitSha, commitMessage, id, ...dataItem }) => ({
      ...dataItem,
      key: id.toString(),
      commitDate: moment(commitDate).format('DD/MM/YY, h:mm:ss'),
      gitCommitSha: gitCommitSha.slice(0, 7),
      commitMessage: commitMessage.split('*').shift(),
    }));

  const handleShaSubmission = () => {
    getDeployableVersionBySha(gitCommitSha);
    setGitCommitSha(null);
  };

  if (!versions) {
    return <Spinner />;
  }

  return (
    <>
      {/*<AppSearch />*/}
      {/*<AppButton type="primary" onClick={() => toggleShowModal(true)} label="Find my commit" className="table-button" />*/}
      <div className="header">
        <AppButton
          type="primary"
          onClick={() => toggleShowModal(true)}
          label="Find latest commit from branch"
          className="table-button"
        />
        <AppButton type="primary" label="Find latest commit on master" className="table-button" />
      </div>
      <AppModal visible={showModal} toggleModal={toggleShowModal} title="Find my commit" onOk={handleShaSubmission}>
        <Input
          placeholder="Enter commit Sha"
          onChange={({ target: { value } }) => setGitCommitSha(value)}
          value={gitCommitSha}
        />
      </AppModal>
      {/*<AppModal*/}
      {/*  visible={showModal}*/}
      {/*  toggleModal={toggleShowModal}*/}
      {/*  title="Deploy latest commit from branch"*/}
      {/*  onOk={handleShaSubmission}*/}
      {/*  customFooter={[*/}
      {/*    <AppButton onClick={toggleShowModal} label={'Find'} className="table-button" />,*/}
      {/*    <AppButton type="primary" onClick={toggleShowModal} label={'Submit'} className="table-button" />,*/}
      {/*  ]}*/}
      {/*>*/}
      {/*  <Input*/}
      {/*    placeholder="Branch name"*/}
      {/*    onChange={({ target: { value } }) => setBranchName(value)}*/}
      {/*    value={branchName}*/}
      {/*  />*/}
      {/*</AppModal>*/}
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
/*
<AppModal
    visible={showModal}
    toggleModal={toggleShowModal}
    title="TESTTTTTTT"
    customFooter={[
        <AppButton onClick={toggleShowModal} label={'Find'} className="table-button"/>,
        <AppButton type="primary" onClick={toggleShowModal} label={'Submit'} className="table-button"/>,
    ]}
>*/
