import React, { useEffect, useState } from 'react';
import { AppButton } from '../../../../common/Button';
import { ConfirmModal } from './ConfirmModal';
import { Spinner } from '../../../../common/Spinner';
import { parse } from 'query-string';
import { DeploymentDetailsContainer } from './DeploymentDetailsContainer';
import { BlockedDeploymentsModal } from './BlockedDeploymentsModal';
import { isEmpty } from 'lodash';
import './VerifyDeployment.css';

export const VerifyDeployment = ({
  search,
  handleBreadcrumbs,
  services,
  environments,
  groups,
  versions,
  getServices,
  getEnvironments,
  getGroups,
  getDeployableVersionsById,
  getSelectedServices,
  getSelectedEnv,
  getSelectedVersion,
  getSelectedGroups,
  selectedServices,
  selectedEnvironments,
  selectedVersion,
  selectedGroups,
  isLoading,
  deploy,
  deployGroup,
  blockedDeployments,
  newDeployments,
}) => {
  const [showConfirmModal, toggleShowConfirmModal] = useState(false);
  const [showBlockedDeploymentsModal, toggleShowBlockedDeploymentsModal] = useState(false);
  const { service: serviceId, group, environment: environmentsId } = parse(search);

  useEffect(() => {
    handleBreadcrumbs('deployment');
    getEnvironments();
    getServices();
    getDeployableVersionsById(serviceId);
    group && environmentsId.split(',').map(environmentId => getGroups(environmentId, serviceId));
  }, []);

  useEffect(() => {
    !selectedServices.length && getSelectedServices();
  }, [services]);

  useEffect(() => {
    !selectedEnvironments.length && getSelectedEnv();
  }, [environments]);

  useEffect(() => {
    !selectedVersion && getSelectedVersion();
  }, [versions]);

  useEffect(() => {
    !selectedGroups.length && group && getSelectedGroups();
  }, [groups]);

  useEffect(() => {
    blockedDeployments && toggleShowBlockedDeploymentsModal(true);
  }, [blockedDeployments]);

  const handleDeploy = isEmergencyDeployment => {
    if (!selectedGroups.length) {
      deploy(
        selectedServices.map(selectedService => selectedService.id).join(','),
        selectedEnvironments.map(selectedEnvironment => selectedEnvironment.id).join(','),
        selectedVersion.id,
        selectedVersion.commitMessage,
        isEmergencyDeployment,
      );
    } else {
      deployGroup(
        selectedServices.map(selectedService => selectedService.id).join(','),
        selectedEnvironments.map(selectedEnvironment => selectedEnvironment.id).join(','),
        selectedVersion.id,
        selectedVersion.commitMessage,
        selectedGroups.map(selectedGroup => selectedGroup.id).join(','),
        isEmergencyDeployment,
      );
    }
    toggleShowConfirmModal(false);
  };

  const findNameById = (itemId, itemsList) => {
    const item = itemsList.find(({ id }) => id === itemId);
    return item && item.name;
  };

  const formattedData =
    blockedDeployments &&
    blockedDeployments.map(({ groupId, environmentId, serviceId, exception, ...dataItem }) => {
      return {
        ...dataItem,
        groupId: findNameById(groupId, groups),
        environmentId: findNameById(environmentId, environments),
        serviceId: findNameById(serviceId, services),
        exception: exception.message,
      };
    });

  if (
    !services ||
    !environments ||
    !versions ||
    !selectedVersion ||
    !selectedEnvironments ||
    !selectedServices ||
    (group && !groups)
  ) {
    return <Spinner />;
  }

  return (
    <div className="verify-deployment">
      <DeploymentDetailsContainer
        selectedVersion={selectedVersion}
        selectedEnvironments={selectedEnvironments}
        selectedGroups={selectedGroups}
        selectedServices={selectedServices}
        group={group}
      />
      <AppButton
        label={`Confirm deployment details`}
        className={'deploy table-submit-button'}
        type="primary"
        onClick={() => {
          toggleShowConfirmModal(true);
        }}
      />
      <ConfirmModal
        isLoading={isLoading}
        handleDeploy={handleDeploy}
        showModal={showConfirmModal}
        toggleShowModal={toggleShowConfirmModal}
      />
      <BlockedDeploymentsModal
        showModal={showBlockedDeploymentsModal}
        toggleShowModal={toggleShowBlockedDeploymentsModal}
        blockedDeployments={formattedData}
        showOngoingDeploymentsLink={!isEmpty(newDeployments)}
      />
    </div>
  );
};
