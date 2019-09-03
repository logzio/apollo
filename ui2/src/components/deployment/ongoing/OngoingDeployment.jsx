import React, { useEffect, useState } from 'react';
import { connect } from 'react-redux';
import moment from 'moment';
import {
  getEnvironments,
  getOngoingDeployments,
  getServices,
  getContainers,
} from '../../../store/actions/deploymentActions';
import { Spinner } from '../../../common/Spinner';
import { AppTable } from '../../../common/Table';
import { tableColumns } from '../../../utils/tableColumns';
import { LiveLogsView } from './LiveLogsView';
import './OngoingDeployment.css';

const OngoingDeploymentComponent = ({
  getOngoingDeployments,
  ongoingDeployments,
  getServices,
  services,
  getEnvironments,
  environments,
  handleBreadcrumbs,
  getContainers,
  containers,
  isLoading,
  lastCreatedPod,
}) => {
  const [showModal, toggleShowModal] = useState(false);
  const [environmentId, setEnvironmentId] = useState(null);

  useEffect(() => {
    handleBreadcrumbs('ongoing');
    getServices();
    getEnvironments();

    const intervalId = setInterval(() => {
      getOngoingDeployments();
    }, 1000 * 10);

    return () => clearInterval(intervalId);
  }, []);

  const findNameById = (itemId, itemsList) => {
    let itemName = null;
    itemsList &&
      itemsList.map(({ id, name }) => {
        if (id === itemId) {
          itemName = name;
        }
      });
    return itemName;
  };

  const formattedData =
    ongoingDeployments &&
    ongoingDeployments.map(({ id, lastUpdate, serviceId, environmentId, groupName, ...dataItem }) => {
      return {
        ...dataItem,
        id: id,
        key: id.toString(),
        lastUpdate: moment(lastUpdate).format('DD/MM/YY, h:mm:ss'),
        serviceId: serviceId,
        serviceName: findNameById(serviceId, services),
        environmentId: environmentId,
        environmentName: findNameById(environmentId, environments),
        groupName: groupName ? groupName : 'Non',
      };
    });

  const handleViewLogsAction = (environmentId, serviceId) => {
    toggleShowModal(true);
    setEnvironmentId(environmentId);
    getContainers(environmentId, serviceId);
  };

  const columns = tableColumns(
    ['lastUpdate', 'serviceName', 'environmentName', 'groupName', 'userEmail', 'deploymentMessage', 'status', 'actions'],
    ['Last Update', 'Service', 'Environment', 'Group', 'Initiated By', 'Deployment Message', 'Status', 'Actions'],
    [{ title: 'View logs', color: '#465BA4', onClick: handleViewLogsAction }, { title: 'Revert', color: '#BD656A' }],
  );

  if (!ongoingDeployments || !services || !environments) {
    return <Spinner />;
  }

  return (
    <div>
      {showModal && (
        <LiveLogsView
          toggleShowModal={toggleShowModal}
          environmentId={environmentId}
          containers={containers}
          lastCreatedPod={lastCreatedPod}
          isLoading={isLoading}
        />
      )}
      <AppTable
        columns={columns}
        data={formattedData}
        scroll={{ y: 650 }} //600
        showSearch={true}
        searchColumns={['lastUpdate', 'serviceName', 'environmentName', 'groupName', 'userEmail', 'status']}
        showSelection={false}
        emptyMsg={"There aren't ongoing deployments"}
        rowClassName={({ groupName }) => (groupName ? 'hide-row-expand-icon' : '')} //TODO
        expandableColumn={3}
        expandIconAsCell={false}
        handleViewLogsAction={handleViewLogsAction}
      />
    </div>
  );
};

const mapStateToProps = ({
  deploy: { isLoading, ongoingDeployments, services, environments, lastCreatedPod, lastCreatedGroupPod, containers },
}) => ({
  isLoading,
  ongoingDeployments,
  services,
  environments,
  lastCreatedPod,
  lastCreatedGroupPod,
  containers,
});

export const OngoingDeployment = connect(
  mapStateToProps,
  {
    getOngoingDeployments,
    getServices,
    getEnvironments,
    getContainers,
  },
)(OngoingDeploymentComponent);
