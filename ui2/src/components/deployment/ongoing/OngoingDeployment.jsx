import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import { getEnvironments, getOngoingDeployments, getServices } from '../../../store/actions/deploymentActions';
import { Spinner } from '../../../common/Spinner';
import { AppTable } from '../../../common/Table';
import { tableColumns } from '../../../utils/tableColumns';
import moment from 'moment';
import './OngoingDeployment.css';

const OngoingDeploymentComponent = ({
  getOngoingDeployments,
  ongoingDeployments,
  getServices,
  services,
  getEnvironments,
  environment,
  match,
  resetBreadcrumbs,
  handleBreadcrumbs,
}) => {
  useEffect(() => {
    resetBreadcrumbs();
    // handleBreadcrumbs(``, 'ongoing'); //TODO
    getServices();
    getEnvironments();
    getOngoingDeployments();
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
        serviceId: findNameById(serviceId, services),
        environmentId: findNameById(environmentId, environment),
        groupName: groupName ? groupName : 'Non',
      };
    });

  if (!ongoingDeployments || !services || !environment) {
    return <Spinner />;
  }

  return (
    <AppTable
      columns={tableColumns(
        ['lastUpdate', 'serviceId', 'environmentId', 'groupName', 'userEmail', 'deploymentMessage', 'status', 'actions'],
        ['Last Update', 'Service', 'Environment', 'Group', 'Initiated By', 'Deployment Message', 'Status', 'Actions'],
        null,
        [
          { title: 'View logs', color: '#465BA4', type: 'primary', icon: 'menu-unfold' },
          { title: 'Revert', color: '#BD656A', type: 'danger', icon: 'undo' },
        ],
      )}
      data={formattedData}
      scroll={{ y: 750 }}
      showSearch={true}
      searchColumns={['lastUpdate', 'serviceId', 'environmentId', 'groupName', 'userEmail', 'status']}
      showSelection={false}
      emptyMsg={"There aren't ongoing deployments"}
      rowClassName={({ groupName }) => (groupName ? 'hide-row-expand-icon' : '')} //TODO
      expandableColumn={3}
      expandIconAsCell={false}
    />
  );
};

const mapStateToProps = ({ deploy: { isLoading, ongoingDeployments, services, environment } }) => ({
  isLoading,
  ongoingDeployments,
  services,
  environment,
});

export const OngoingDeployment = connect(
  mapStateToProps,
  {
    getOngoingDeployments,
    getServices,
    getEnvironments,
  },
)(OngoingDeploymentComponent);
<<<<<<< HEAD

// [
//     { title: 'View logs', color: '#465BA4', type: 'primary', icon: 'menu-unfold' },
//     { title: 'Revert', color: '#BD656A', type: 'danger', icon: 'undo' },
// ],
=======
>>>>>>> 21fb84b4660cf6c09a558a25820ea90d74c9772e
