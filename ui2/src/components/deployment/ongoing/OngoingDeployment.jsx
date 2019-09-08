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
import { tableColumns } from '../../../utils/tableColumns';
import { LiveLogsView } from './LiveLogsView';
import { GroupView } from './GroupView';
import { tagListTitles } from '../../../utils/tableConfig';
import _ from 'lodash';
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
  getGroupContainers,
  containers,
  lastCreatedPod,
  revertDeployment,
}) => {
  const [showModal, toggleShowModal] = useState(false);
  const [showGroupModal, toggleShowGroupModal] = useState(false);
  const [environmentId, setEnvironmentId] = useState(null);
  const [groupRecords, setGroupRecords] = useState(null);

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
    //TODO ORGANIZE AND NAMES
    const [groupsData, ungroupedData] = _.partition(
      ongoingDeployments,
      ongoingDeployment => ongoingDeployment.groupName !== null,
    );
    const groupsRecords = _.chain(groupsData)
      .groupBy('deployableVersionId')
      .mapValues(sortedGroupsRecords => _.groupBy(sortedGroupsRecords, groupRecord => groupRecord.environmentId))
      .value();

    let formattedDataByGroups = [...ungroupedData];
    _.forEach(groupsRecords, groupRecords => {
      _.forEach(groupRecords, records => {
        formattedDataByGroups.push({
          ...records[0],
          groupRecords: records,
        });
      });
    });
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
    toggleShowGroupModal(false);
    toggleShowModal(true);
    setEnvironmentId(environmentId);
    !groupName ? getContainers(environmentId, serviceId) : getGroupContainers(environmentId, serviceId, groupName);
  };

  const handleRevertDeploymentAction = ({ id }) => {
    revertDeployment(id);
  };

  const handleViewSelectedGroup = ({ groupRecords }) => {
    toggleShowGroupModal(true);
    setGroupRecords(groupRecords);
  };

  const columns = tableColumns(
    ['lastUpdate', 'serviceName', 'environmentName', 'userEmail', 'deploymentMessage', 'status', 'actions'],
    ['Last Update', 'Service', 'Environment', 'Initiated By', 'Deployment Message', 'Status', 'Actions'],
    [
      { title: tagListTitles.LOGS, color: '#465BA4', onClick: handleViewLogsAction },
      { title: tagListTitles.REVERT, color: '#BD656A', onClick: handleRevertDeploymentAction },
      { title: tagListTitles.GROUP, color: '#33C737', onClick: handleViewSelectedGroup },
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
      <AppTable
        columns={columns}
        data={formattedData}
        scroll={{ y: 650 }} //600
        showSearch={true}
        searchColumns={['lastUpdate', 'serviceName', 'environmentName', 'groupName', 'userEmail', 'status']}
        showSelection={false}
        emptyMsg={"There aren't ongoing deployments"}
        rowClassName={({ group }) => (group === 'Non' ? 'hide-row-expand-icon' : '')} //TODO EXPAND ROWS
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
