import React, { useEffect, useState } from 'react';
import { AppButton } from '../../../../common/Button';
import { ConfirmModal } from './ConfirmModal';
import { Spinner } from '../../../../common/Spinner';
import { parse } from 'query-string';
import { DeploymentDetailsContainer } from './DeploymentDetailsContainer';
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
  newDeploymentStatus,
}) => {
  const [showModal, toggleShowModal] = useState(false);
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
  };

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
          toggleShowModal(true);
        }}
      />
      <ConfirmModal
        isLoading={isLoading}
        handleDeploy={handleDeploy}
        showModal={showModal}
        toggleShowModal={toggleShowModal}
      />
    </div>
  );
};
