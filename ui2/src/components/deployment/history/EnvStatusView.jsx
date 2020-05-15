import React, { useEffect } from 'react';
import { AppModal } from '../../../common/Modal';
import { AppButton } from '../../../common/Button';
import { Spinner } from '../../../common/Spinner';
import { tableColumns } from '../../../utils/tableColumns';
import { AppTable } from '../../../common/Table';
import { forEach, isString } from 'lodash';

export const EnvStatusView = ({
  toggleShowModal,
  envStatus,
  getServices,
  services,
  selectedEnv,
  getAllGroups,
  groups,
}) => {
  useEffect(() => {
    getServices();
    getAllGroups();
  }, []);

  const renderEnvStatus = () => {
    const envStatusObj = JSON.parse(envStatus);
    let data = [];

    forEach(envStatusObj, (lastVersion, serviceId) => {
      services.map(({ id, name: serviceName }) => {
        if (serviceId === id.toString()) {
          if (!isString(lastVersion)) {
            forEach(lastVersion, (lastGroupVersion, groupId) => {
              groups.map(({ id, name: groupName }) => {
                if (groupId === id.toString()) {
                  data = [
                    ...data,
                    {
                      id: id,
                      key: serviceId,
                      serviceName: serviceName,
                      groupName: groupName,
                      lastVersion: lastGroupVersion,
                    },
                  ];
                }
              });
            });
          } else {
            data = [
              ...data,
              {
                id: id,
                key: serviceId,
                serviceName: serviceName,
                lastVersion: lastVersion,
              },
            ];
          }
        }
      });
    });

    return data;
  };

  const columns = tableColumns(
    ['serviceName', 'groupName', 'lastVersion'],
    ['Service Name', 'Group Name', 'Last Commit'],
  );

  return (
    <AppModal
      className={'env-status'}
      visible={true}
      toggleModal={toggleShowModal}
      onClose={() => {
        toggleShowModal(false);
      }}
      title={`Environment status - ${selectedEnv}`}
      footer={[<AppButton label={'Close'} className={'modal-btn'} key="back" onClick={() => toggleShowModal(false)} />]}
    >
      {envStatus && services && groups ? (
        <AppTable
          columns={columns}
          data={() => renderEnvStatus()}
          scroll={{ y: 300 }}
          showSelection={false}
          pagination={false}
        />
      ) : (
        <div className="modal-spinner-wrapper">
          <Spinner />
        </div>
      )}
    </AppModal>
  );
};
