import React, { useEffect, useState } from 'react';
import { connect } from 'react-redux';
import moment from 'moment';
import {
  getEnvironments,
  getOngoingDeployments,
  getServices,
  getContainers,
  getGroupContainers,
  revertDeployment,
} from '../../../store/actions/deploymentActions';
import { AppModal } from '../../../common/Modal';
import { AppTable } from '../../../common/Table';
import { tableColumns } from '../../../utils/tableColumns';
import { LiveLogsView } from './LiveLogsView';
import _ from 'lodash';
import './OngoingDeployment.css';
import { AppButton } from '../../../common/Button';

const OngoingDeploymentComponent = ({
  getOngoingDeployments,
  ongoingDeployments,
  getServices,
  services,
  getEnvironments,
  environments,
  handleBreadcrumbs,
  getContainers,
  getGroupContainers,
  containers,
  lastCreatedPod,
  revertDeployment,
}) => {
  const [showModal, toggleShowModal] = useState(false);
  const [showGroupModal, toggleShowGroupModal] = useState(false);
  const [environmentId, setEnvironmentId] = useState(null);

  useEffect(() => {
    handleBreadcrumbs('ongoing');
    getServices();
    getEnvironments();
    getOngoingDeployments();

    // const intervalId = setInterval(() => {
    //   getOngoingDeployments();
    // }, 1000 * 10);
    //
    // return () => clearInterval(intervalId);
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

  const filteredData = () => {
    const dataOfGroups = _.filter(ongoingDeployments, obj => obj.groupName !== null);
    const dataWithoutGroups = _.filter(ongoingDeployments, obj => obj.groupName === null);
    const groupsRecords = _.chain(dataOfGroups)
      .groupBy('deployableVersionId')
      .mapValues(ageArr => _.groupBy(ageArr, ageObj => ageObj.environmentId))
      .value();

    let formattedDataByGroups = [...dataWithoutGroups];
    // debugger;
    const a = _.forEach(groupsRecords, groupRecords => {
      _.forEach(groupRecords, records => {
        formattedDataByGroups.push({
          ...records[0],
          groupRecords: records,
        });
      });
    });
    // debugger;
    return formattedDataByGroups;
  };

  const formattedData = () => {
    return (
      ongoingDeployments &&
      filteredData().map(({ id, lastUpdate, serviceId, environmentId, groupName, ...dataItem }) => {
        return {
          ...dataItem,
          id: id,
          key: id.toString(),
          lastUpdate: moment(lastUpdate).format('DD/MM/YY, h:mm:ss'),
          serviceId: serviceId,
          serviceName: findNameById(serviceId, services),
          environmentId: environmentId,
          environmentName: findNameById(environmentId, environments),
          groupName: groupName,
        };
      })
    );
  };

  const handleViewLogsAction = ({ environmentId, serviceId, groupName }) => {
    toggleShowModal(true);
    setEnvironmentId(environmentId);
    !groupName ? getContainers(environmentId, serviceId) : getGroupContainers(environmentId, serviceId, groupName);
  };

  const handleRevertDeploymentAction = ({ id }) => {
    revertDeployment(id);
  };

  const columns = tableColumns(
    ['lastUpdate', 'serviceName', 'environmentName', 'userEmail', 'deploymentMessage', 'status', 'actions'],
    ['Last Update', 'Service', 'Environment', 'Initiated By', 'Deployment Message', 'Status', 'Actions'],
    [
      { title: 'View logs', color: '#465BA4', onClick: handleViewLogsAction },
      { title: 'Revert', color: '#BD656A', onClick: handleRevertDeploymentAction },
      { title: 'Group status', color: '#33C737', onClick: toggleShowGroupModal },
    ],
  );

  return (
    <div>
      {showModal && (
        <LiveLogsView
          toggleShowModal={toggleShowModal}
          environmentId={environmentId}
          containers={containers}
          lastCreatedPod={lastCreatedPod}
        />
      )}
      {showGroupModal && (
        <AppModal
          visible={true}
          title="Status Per Group"
          toggleModal={toggleShowGroupModal}
          footer={[
            <AppButton label={'Cancel'} className={'modal-btn'} key="back" onClick={() => toggleShowGroupModal(false)}>
              hi
            </AppButton>,
          ]}
        >
          {'hi'}
        </AppModal>
      )}
      <AppTable
        columns={columns}
        data={formattedData}
        scroll={{ y: 650 }} //600
        showSearch={true}
        searchColumns={['lastUpdate', 'serviceName', 'environmentName', 'groupName', 'userEmail', 'status']}
        showSelection={false}
        emptyMsg={"There aren't ongoing deployments"}
        rowClassName={({ group }) => (group === 'Non' ? 'hide-row-expand-icon' : '')} //TODO
        expandableColumn={3}
        expandIconAsCell={false}
        expandable={true}
      />
    </div>
  );
};

const mapStateToProps = ({
  deploy: { ongoingDeployments, services, environments, lastCreatedPod, lastCreatedGroupPod, containers },
}) => ({
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
    getGroupContainers,
    revertDeployment,
  },
)(OngoingDeploymentComponent);
