import React, { useEffect, useState } from 'react';
import { AppButton } from '../../../common/Button';
import { AppModal } from '../../../common/Modal';
import { Spinner } from '../../../common/Spinner';
import { AppCard } from '../../../common/Card';
import { Col, Row, List, Avatar } from 'antd';
import { parse } from 'query-string';
import symbol from '../../../assets/images/apollo-symbol.svg';
import { DeploymentDetailsCard } from '../DeploymentDetailsCard';
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
}) => {
  const [showModal, toggleShowModal] = useState(false);
  const { service: serviceId, group, environment: environmentsId } = parse(search);
  const deployItems = [
    { title: 'Services to deploy:', dataSource: selectedServices, isPartOfGroup: false },
    { title: 'Environments to deploy:', dataSource: selectedEnvironments, isPartOfGroup: false },
    { title: 'Groups to deploy:', dataSource: selectedGroups, isPartOfGroup: true },
  ];

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
      <Row className={'verify-deployment-content'}>
        <Col span={5} offset={1}>
          <DeploymentDetailsCard
            title={'Version to deploy: '}
            commitMessage={selectedVersion.commitMessage}
            committerAvatarUrl={selectedVersion.committerAvatarUrl}
            committerName={selectedVersion.committerName}
            gitCommitSha={selectedVersion.gitCommitSha}
            commitUrl={selectedVersion.commitUrl}
          />
        </Col>
        {deployItems.map(({ isPartOfGroup, dataSource, title }, index) => {
          const isSelectedGroups = !isPartOfGroup || !!group;
          return (
            isSelectedGroups && (
              <Col span={group ? 5 : 4} offset={group ? 1 : 4} key={index}>
                <AppCard title={title}>
                  <List
                    itemLayout="horizontal"
                    dataSource={dataSource}
                    renderItem={item => (
                      <List.Item>
                        <List.Item.Meta
                          avatar={<Avatar className={'list-icon'} src={symbol} />}
                          title={<div>{item.name}</div>}
                        />
                      </List.Item>
                    )}
                  />
                </AppCard>
              </Col>
            )
          );
        })}
      </Row>
      <AppButton
        label={`Confirm deployment details`}
        className={'deploy table-submit-button'}
        type="primary"
        onClick={() => {
          toggleShowModal(true);
        }}
      />
      <AppModal
        visible={showModal}
        toggleModal={toggleShowModal}
        title="Fashizle?"
        footer={[
          <AppButton
            label={'Emergency'}
            className={'modal-btn emergency-modal-btn'}
            key="emergency"
            type="danger"
            onClick={() => handleDeploy(true)}
            tooltipText={"Deploy while ignoring environment's concurrency limits!"}
            isLoading={isLoading}
          />,
          <AppButton label={'Cancel'} className={'modal-btn'} key="back" onClick={() => toggleShowModal(false)} />,
          <AppButton
            label={'Deploy'}
            className={'modal-btn'}
            key="submit"
            type="primary"
            onClick={() => handleDeploy(false)}
            isLoading={isLoading}
          />,
        ]}
      >
        <div>Confirm here will actually deploy your commit. </div>
      </AppModal>
    </div>
  );
};
