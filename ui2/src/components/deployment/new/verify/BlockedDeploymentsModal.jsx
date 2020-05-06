import React from 'react';
import { AppModal } from '../../../../common/Modal';
import { AppTable } from '../../../../common/Table';
import { tableColumns } from '../../../../utils/tableColumns';
import { AppButton } from '../../../../common/Button';

export const BlockedDeploymentsModal = ({ blockedDeployments, showModal, toggleShowModal }) => {
  const columns = tableColumns(
    ['environment', 'service', 'group', 'reason'],
    ['Environment', 'Service', 'Group', 'Reason'],
  );

  return (
    <AppModal
      visible={showModal}
      toggleModal={toggleShowModal}
      title="Some of your deployments were blocked"
      footer={[<AppButton label={'Close'} className={'modal-btn'} key="back" onClick={() => toggleShowModal(false)} />]}
    >
      <AppTable columns={columns} data={blockedDeployments} scroll={{ y: 300 }} pagination={false} />
    </AppModal>
  );
};
