import React, { useEffect, useState } from 'react';
import { AppButton } from '../../../common/Button';
import { AppModal } from '../../../common/Modal';
import './VerifyDeployment.css';
// import { AppInput } from '../../../common/FormFields';

export const VerifyDeployment = ({ handleBreadcrumbs, resetBreadcrumbs, match, location }) => {
  const [showModal, toggleShowModal] = useState(false);

  useEffect(() => {
    resetBreadcrumbs();
    handleBreadcrumbs(`${location.pathname}${location.search}`, 'verify-deployment');
  }, []);

  return (
    <div className="verify-deployment">
      <div>So just making sure</div>
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
      >
        hi
      </AppModal>
    </div>
  );
};
