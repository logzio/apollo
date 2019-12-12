import { AppModal } from '../../../../common/Modal';
import { AppButton } from '../../../../common/Button';
import React from 'react';

export const ConfirmModal = ({ toggleShowModal, showModal, handleDeploy, isLoading }) => {
  return (
    <AppModal
      visible={showModal}
      toggleModal={toggleShowModal}
      title="Fashizle?"
      footer={[
        <AppButton
          label={'Emergency'}
          className={'modal-btn'}
          key="emergency"
          type="danger"
          onClick={() => handleDeploy(true)}
          tooltipText={"Deploy while ignoring environment's concurrency limits!"}
          isLoading={isLoading}
        />,
        <AppButton label={'Cancel'} className={'modal-btn cancel-modal-btn'} key="back" onClick={() => toggleShowModal(false)} />,
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
      <div>Confirm here will actually deploy your commit.</div>
    </AppModal>
  );
};
