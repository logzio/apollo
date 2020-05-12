import React from 'react';
import { AppModal } from '../../../../common/Modal';
import { AppTable } from '../../../../common/Table';
import { tableColumns } from '../../../../utils/tableColumns';
import { AppButton } from '../../../../common/Button';
import { historyBrowser } from '../../../../utils/history';

export const BlockedDeploymentsModal = ({
  blockedDeployments,
  showModal,
  toggleShowModal,
  showOngoingDeploymentsLink,
}) => {
  const columns = tableColumns(
    ['environmentId', 'serviceId', 'groupId', 'exception'],
    ['Environment', 'Service', 'Group', 'Exception'],
  );
  const modalTitle = showOngoingDeploymentsLink
    ? 'Some of your deployments were blocked'
    : 'Your deployments were blocked';

  return (
    <AppModal
      visible={showModal}
      toggleModal={toggleShowModal}
      title={modalTitle}
      footer={[
        showOngoingDeploymentsLink && (
          <AppButton
            label={'Ongoing deployments'}
            className={'modal-btn'}
            key="submit"
            type="primary"
            onClick={() =>
              historyBrowser.push({
                pathname: '/deployment/ongoing',
              })
            }
          />
        ),
        <AppButton label={'Close'} className={'modal-btn'} key="back" onClick={() => toggleShowModal(false)} />,
      ]}
    >
      <AppTable columns={columns} data={blockedDeployments} scroll={{ y: 300 }} pagination={false} />
    </AppModal>
  );
};
