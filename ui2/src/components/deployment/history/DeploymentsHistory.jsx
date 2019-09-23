import React, { useEffect, useState } from 'react';
import { connect } from 'react-redux';
import moment from 'moment';
import {
  getDeploymentHistory,
  getDeployableVersionById,
  getDeploymentEnvStatus,
  getServices,
  getDeploymentById,
  deploy,
} from '../../../store/actions/deploymentActions';
import { AppTable } from '../../../common/Table';
import { tableColumns } from '../../../utils/tableColumns';
import { tagListTitles } from '../../../utils/tableConfig';
import { DeploymentDetailsView } from './DeploymentDetailsView';
import { EnvStatusView } from './EnvStatusView';
import { AppModal } from '../../../common/Modal';
import { AppButton } from '../../../common/Button';
import './DeploymentsHistory.css';

const PlainHistoryDeployment = ({
  getDeploymentHistory,
  deploymentsHistory,
  handleBreadcrumbs,
  deploymentsHistoryDetails,
  getDeployableVersionById,
  deployableVersion,
  getDeploymentEnvStatus,
  envStatus,
  getServices,
  services,
  deploymentDetails,
  getDeploymentById,
  deploy,
}) => {
  const [showModalInfo, toggleShowModalInfo] = useState(false);
  const [showModalEnv, toggleShowModalEnv] = useState(false);
  const [showModalRevert, toggleShowModalRevert] = useState(false);
  const [searchValue, setSearchValue] = useState(null);
  const [selectedEnv, setSelectedEnv] = useState(null);
  const pageSize = 15;
  const defaultCurrentPage = 1;

  useEffect(() => {
    getDeploymentHistory(true, defaultCurrentPage, pageSize);
    handleBreadcrumbs('history');
  }, []);

  const formattedData = () => {
    return (
      deploymentsHistory &&
      deploymentsHistory.map(({ id, lastUpdate, ...dataItem }) => {
        return {
          ...dataItem,
          id: id,
          key: id.toString(),
          lastUpdate: moment(lastUpdate).format('DD/MM/YY, hh:mm:ss'),
        };
      })
    );
  };

  const handlePageSelection = (page, pageSize) => {
    pageSize && getDeploymentHistory(true, page, pageSize, searchValue);
  };

  const handleViewCommitDetails = ({ deployableVersionId }) => {
    toggleShowModalInfo(true);
    getDeployableVersionById(deployableVersionId);
  };

  const handleViewEnvStatus = ({ id, environmentName }) => {
    setSelectedEnv(environmentName);
    toggleShowModalEnv(true);
    getDeploymentEnvStatus(id);
  };

  const handleRevert = ({ id }) => {
    toggleShowModalRevert(true);
    getDeploymentById(id);
  };

  const handleSearch = searchValue => {
    setSearchValue(searchValue);
    getDeploymentHistory(true, defaultCurrentPage, pageSize, searchValue);
  };

  const columns = tableColumns(
    ['id', 'lastUpdate', 'serviceName', 'environmentName', 'groupName', 'userEmail', 'status', 'actions'],
    ['#', 'Last Update', 'Service', 'Environment', 'Group', 'Initiated By', 'Status', 'Actions'],
    [
      { title: tagListTitles.DETAILS, color: '#465BA4', onClick: handleViewCommitDetails },
      { title: tagListTitles.BACK, color: '#BD656A', onClick: handleRevert },
      { title: tagListTitles.STATUS, color: '#33C737', onClick: handleViewEnvStatus },
    ],
  );

  return (
    <div>
      {showModalInfo && (
        <DeploymentDetailsView toggleShowModal={toggleShowModalInfo} deployableVersion={deployableVersion} />
      )}
      {showModalEnv && (
        <EnvStatusView
          toggleShowModal={toggleShowModalEnv}
          envStatus={envStatus}
          services={services}
          getServices={getServices}
          selectedEnv={selectedEnv}
        />
      )}
      {showModalRevert && (
        <AppModal
          title={'Are you sure you want to revert this deployment?'}
          visible={true}
          toggleModal={toggleShowModalRevert}
          footer={[
            <AppButton
              label={'Cancel'}
              className={'modal-btn'}
              key="back"
              onClick={() => toggleShowModalRevert(false)}
            />,
            <AppButton
              label={"I'm 100% sure, and I know I can't break anything!"}
              className={'modal-btn'}
              key="submit"
              type="primary"
              onClick={() => {
                deploy(
                  deploymentDetails.serviceId,
                  deploymentDetails.environmentId,
                  deploymentDetails.deployableVersionId,
                  deploymentDetails.deploymentMessage,
                  false,
                );
                toggleShowModalRevert(false);
              }}
            />,
          ]}
        >
          <div>
            <div>This will revert the current version to that version.</div>
            <div className="warning-text"> THIS CAN SERIOUSLY BREAK THINGS!</div>
            <div>
              Please, double check with at least 2 people that this is really,{' '}
              <span className="warning-text">really</span> what you want to do.
            </div>
          </div>
        </AppModal>
      )}
      <AppTable
        columns={columns}
        data={formattedData}
        scroll={{ y: 800 }}
        showSearch={true}
        searchColumns={['id', 'lastUpdate', 'serviceName', 'environmentName', 'groupName', 'userEmail', 'status']}
        showSelection={false}
        emptyMsg={"There aren't deployments"}
        pagination={{
          pageSize: pageSize,
          total: deploymentsHistoryDetails && deploymentsHistoryDetails.recordsFiltered,
          onChange: handlePageSelection,
        }}
        handleSearch={handleSearch}
        searchValue={searchValue}
      />
    </div>
  );
};

const mapStateToProps = ({
  deploy: { deploymentsHistory, deploymentsHistoryDetails, deployableVersion, envStatus, services, deploymentDetails },
}) => ({
  deploymentsHistory,
  deploymentsHistoryDetails,
  deployableVersion,
  envStatus,
  services,
  deploymentDetails,
});

export const DeploymentsHistory = connect(
  mapStateToProps,
  {
    getDeploymentHistory,
    getDeployableVersionById,
    getDeploymentEnvStatus,
    getServices,
    getDeploymentById,
    deploy,
  },
)(PlainHistoryDeployment);
