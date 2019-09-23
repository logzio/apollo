import React, { useEffect } from 'react';
import { AppModal } from '../../../common/Modal';
import { AppButton } from '../../../common/Button';
import { Spinner } from '../../../common/Spinner';
import _ from 'lodash';
import { tableColumns } from '../../../utils/tableColumns';
import { AppTable } from '../../../common/Table';

export const EnvStatusView = ({ toggleShowModal, envStatus, getServices, services, selectedEnv }) => {
  useEffect(() => {
    getServices();
  }, []);

  const renderEnvStatus = () => {
    const envStatusObj = JSON.parse(envStatus);
    let data = [];

    _.forEach(envStatusObj, (lastVersion, serviceId) => {
      services.map(({ id, name }) => {
        if (serviceId === id.toString()) {
          // if (!_.isString(lastVersion)) {
          //   _.forEach(lastVersion, (lastGroupVersion, groupId) => {});
          // }
          data = [
            ...data,
            {
              id: id,
              key: serviceId,
              serviceName: name,
              deployableVersion: lastVersion,
            },
          ];
        }
      });
    });

    debugger;
    return data;
  };

  const columns = tableColumns(['serviceName'], ['Service Name']);

  return (
    <AppModal
      visible={true}
      toggleModal={toggleShowModal}
      onClose={() => {
        toggleShowModal(false);
      }}
      title={`Environment status - ${selectedEnv}`}
      footer={[<AppButton label={'Close'} className={'modal-btn'} key="back" onClick={() => toggleShowModal(false)} />]}
    >
      {envStatus && services ? (
        <AppTable
          columns={columns}
          data={() => renderEnvStatus()}
          scroll={{ y: 300 }}
          showSelection={false}
          pagination={false}
        />
      ) : (
        <Spinner />
      )}
    </AppModal>
  );
};
