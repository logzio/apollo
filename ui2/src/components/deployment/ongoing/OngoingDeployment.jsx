import React, { useEffect, useState } from 'react';
import { connect } from 'react-redux';
import { getEnvironment, getOngoingDeployments, getServices } from '../../../store/actions/deploymentActions';
import { Spinner } from '../../../common/Spinner';
import { AppTable } from '../../../common/Table';
import { tableColumns } from '../../../utils/tableColumns';
import moment from 'moment';
import { Tag } from 'antd';

const OngoingDeploymentComponent = ({
  getOngoingDeployments,
  ongoingDeployments,
  getServices,
  services,
  getEnvironment,
  environment,
  match,
  resetBreadcrumbs,
}) => {
  useEffect(() => {
    resetBreadcrumbs();
    // handleBreadcrumbs(`${match.url}`, 'service');
    getServices();
    getEnvironment();
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
    ongoingDeployments.map(({ id, lastUpdate, serviceId, environmentId, groupName, environment, ...dataItem }) => {
      return {
        ...dataItem,
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
        ['lastUpdate', 'serviceId', 'environmentId', 'groupName', 'userEmail', 'status', 'actions'],
        ['Last Update', 'Service', 'Environment', 'Group', 'Initiated By', 'Status', 'Actions'],
        null,
        [{ title: 'View logs', color: '#465BA4' }, { title: 'Revert', color: '#E6B5AD' }],
      )}
      data={formattedData}
      // linkTo={'verification'}
      scroll={{ y: 750 }}
      // addSearch={`${location.search}&version=`}
      showSearch={true}
      searchColumns={['lastUpdate', 'serviceId', 'environmentId', 'groupName', 'userEmail', 'status']}
      showSelection={false}
      emptyMsg={'There aren\'t ongoing deployments'}
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
    getEnvironment,
  },
)(OngoingDeploymentComponent);
