import React, { useEffect } from 'react';
import { AppModal } from '../../../common/Modal';
import { AppButton } from '../../../common/Button';
import { Spinner } from '../../../common/Spinner';
import _ from 'lodash';

export const EnvStatusView = ({ toggleShowModal, envStatus, getServices, services }) => {
  const envStatusObj = envStatus && JSON.parse(envStatus);

  useEffect(() => {
    getServices();
  }, []);

  const renderEnvStatus = envStatusObj => {
    _.forEach(envStatusObj, (serviceId, key) => {
      services.map(({id, name}) => {
        debugger;
        console.log('hi');
      });
    });
    debugger;
  };

  return (
    <AppModal
      visible={true}
      toggleModal={toggleShowModal}
      onClose={() => {
        toggleShowModal(false);
      }}
      title={'Deployment details'}
      footer={[<AppButton label={'Close'} className={'modal-btn'} key="back" onClick={() => toggleShowModal(false)} />]}
    >
      {envStatus && services ? renderEnvStatus(envStatusObj) : <Spinner />}
    </AppModal>
  );
};
