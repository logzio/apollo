import React from 'react';
import { AppModal } from '../../../common/Modal';
import { AppTable } from '../../../common/Table';
import { tableColumns } from '../../../utils/tableColumns';
import { AppButton } from '../../../common/Button';
import { tagListTitles } from '../../../utils/tableConfig';
import './OngoingDeployment.css';

export const GroupView = ({
  toggleShowGroupModal,
  groupRecords,
  handleViewLogsAction,
  handleRevertDeploymentAction,
}) => {
  const columns = tableColumns(
    ['groupName', 'status', 'actions'],
    ['Group Name', 'Status', 'Actions'],
    [
      { title: tagListTitles.LOGS, onClick: handleViewLogsAction },
      { title: tagListTitles.REVERT, onClick: handleRevertDeploymentAction },
    ],
  );

  return (
    <AppModal
      visible={true}
      title="Status Per Group"
      toggleModal={toggleShowGroupModal}
      footer={[
        <AppButton label={'Cancel'} className={'modal-btn'} key="back" onClick={() => toggleShowGroupModal(false)} />,
      ]}
    >
      <AppTable columns={columns} data={groupRecords} scroll={{ y: 300 }} showSelection={false} pagination={false} />
    </AppModal>
  );
};
