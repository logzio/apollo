import React, { useEffect, useState } from 'react';
import { AppButton } from '../../../common/Button';
import { AppModal } from '../../../common/Modal';
import { Spinner } from '../../../common/Spinner';
import './VerifyDeployment.css';

export const VerifyDeployment = ({
  handleBreadcrumbs,
  resetBreadcrumbs,
  location,
  getEnvironments,
  getServices,
  environments,
  services,
}) => {
  const [showModal, toggleShowModal] = useState(false);

  useEffect(() => {
    resetBreadcrumbs();
    handleBreadcrumbs(`${location.pathname}${location.search}`, 'Deployment details');
    getEnvironments();
    getServices();
    // getGroups();
    // getDeployableVersionsById(servicesId);
  }, []);

  // const getDeploymentDetails = () => {
  //
  //     return({
  //         versionId: ,
  //         servicesNames: ,
  //         environmentsNames: ,
  //         groupsNames: ,
  //     })
  // };

  if (!environments || !services) {
    return <Spinner />;
  }

  return (
    <div className="verify-deployment">
      <div>So just making sure</div>
      <div></div>
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
