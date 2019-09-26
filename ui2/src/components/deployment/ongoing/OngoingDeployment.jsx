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
import { AppTable } from '../../../common/Table';
import { AppModal } from '../../../common/Modal';
import { tableColumns } from '../../../utils/tableColumns';
import { LiveLogsView } from './LiveLogsView';
import { GroupView } from './GroupView';
import { category, tagListTitles } from '../../../utils/tableConfig';
import _ from 'lodash';
import './OngoingDeployment.css';

const PlainOngoingDeployment = ({
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
  const [showRevertModal, toggleShowRevertModal] = useState(false);
  const [environmentId, setEnvironmentId] = useState(null);
  const [versionId, setVersionId] = useState(null);
  const [groupRecords, setGroupRecords] = useState(null);

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

  const filteredData = () => {
    const [groupsRecords, nonGroupsRecords] = _.partition(
      ongoingDeployments,
      ongoingDeployment => ongoingDeployment.groupName !== null,
    );
    const groupedRecords = _.chain(groupsRecords)
      .groupBy(category.VERSION_ID)
      .mapValues(sortedGroupsRecords => _.groupBy(sortedGroupsRecords, groupRecord => groupRecord.environmentId))
      .value();

    let formattedData = [...nonGroupsRecords];
    _.forEach(groupedRecords, groupedRecordsByVersionId => {
      _.forEach(groupedRecordsByVersionId, groupedRecordsByEnvId => {
        formattedData.push({
          ...groupedRecordsByEnvId[0],
          groupRecords: groupedRecordsByEnvId,
        });
      });
    });
    return formattedData;
  };

  const formattedData = () => {
    const sortedData = _.reverse(_.sortBy(filteredData(), category.LAST_UPDATED));
    return (
      ongoingDeployments &&
      sortedData.map(({ id, lastUpdate, serviceId, environmentId, groupName, ...dataItem }) => {
        return {
          ...dataItem,
          id: id,
          key: id.toString(),
          lastUpdate: moment(lastUpdate).format('DD/MM/YY, hh:mm:ss'),
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
    toggleShowGroupModal(false);
    toggleShowModal(true);
    setEnvironmentId(environmentId);
    !groupName ? getContainers(environmentId, serviceId) : getGroupContainers(environmentId, serviceId, groupName);
  };

  const handleRevertDeploymentAction = async ({ id }) => {
    await setVersionId(id);
    toggleShowRevertModal(true);
  };

  const handleViewSelectedGroup = ({ groupRecords }) => {
    toggleShowGroupModal(true);
    setGroupRecords(groupRecords);
  };

  const columns = tableColumns(
    ['lastUpdate', 'serviceName', 'environmentName', 'userEmail', 'deploymentMessage', 'status', 'actions'],
    ['Last Update', 'Service', 'Environment', 'Initiated By', 'Deployment Message', 'Status'],
    [
      { title: tagListTitles.REVERT, onClick: handleRevertDeploymentAction },
      { title: tagListTitles.LOGS, onClick: handleViewLogsAction },
      { title: tagListTitles.GROUP, onClick: handleViewSelectedGroup },
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
        <GroupView
          toggleShowGroupModal={toggleShowGroupModal}
          groupRecords={groupRecords}
          handleRevertDeploymentAction={handleRevertDeploymentAction}
          handleViewLogsAction={handleViewLogsAction}
        />
      )}
      {showRevertModal && (
        <AppModal
          title={'Are you sure you want to revert this deployment?'}
          visible={true}
          toggleModal={toggleShowRevertModal}
          onOk={() => {
            revertDeployment(versionId);
            toggleShowRevertModal(false);
          }}
        >
          Are you sure you want to revert this deployment?
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
        pagination={false}
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
)(PlainOngoingDeployment);
