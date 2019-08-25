import React, { useEffect, useState, useCallback } from 'react';
import { AppButton } from '../../../common/Button';
import { AppModal } from '../../../common/Modal';
import { AppTable } from '../../../common/Table';
import { Spinner } from '../../../common/Spinner';
import './VerifyDeployment.css';
import { Timeline } from 'antd';
import { useUserService } from '../deploymentCustomHooks';

export const VerifyDeployment = ({
  handleBreadcrumbs,
  resetBreadcrumbs,
  location,
  getEnvironments,
  getServices,
  environments,
  services,
  groups,
  versions,
  isLoading,
  getSelectedServices,
  selectedServices,
}) => {
  const [showModal, toggleShowModal] = useState(false);

  useEffect(() => {
    resetBreadcrumbs();
    handleBreadcrumbs(`${location.pathname}${location.search}`, 'deployment');
    getEnvironments();
    getServices();
  }, []);

  useEffect(() => {
    getSelectedServices();
  }, [services]);

  if (!services || !environments) {
    return <Spinner />;
  }

  return (
    <div className="verify-deployment">
      <div>So just making sure {selectedServices[1] && selectedServices[1].name}</div>
      {/*<div>*/}
      {/*  <Timeline>*/}
      {/*    {getSelectedServices().map(service => {*/}
      {/*      return <Timeline.Item key={service.id}>{service.name}</Timeline.Item>;*/}
      {/*    })}*/}
      {/*  </Timeline>*/}
      {/*  <Timeline>*/}
      {/*    {getSelectedEnv().map(env => {*/}
      {/*      return <Timeline.Item key={env.id}>{env.name}</Timeline.Item>;*/}
      {/*    })}*/}
      {/*  </Timeline>*/}
      {/*</div>*/}
      <AppButton
        label={`Confirm deployment details`}
        className={'table-submit-button'}
        type="primary"
        onClick={() => {
          toggleShowModal(true);
        }}
      />
      <AppModal
        visible={showModal}
        toggleModal={toggleShowModal}
        title="Fashizle?"
        onOk={() => {
          //api new deployment
          toggleShowModal(false);
        }}
        okText={'Deploy'}
      >
        <div>Confirm here will actually deploy your commit. </div>
      </AppModal>
    </div>
  );
};

// return({
//     versionId: ,
//     servicesNames: ,
//     environmentsNames: ,
//     groupsNames: ,
// })
