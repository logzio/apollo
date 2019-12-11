import React from 'react';
import { AppModal } from '../../../common/Modal';
import { AppButton } from '../../../common/Button';
import { Spinner } from '../../../common/Spinner';
import { CommitDetailsCard } from '../CommitDetailsCard';

export const DeploymentDetailsView = ({ toggleShowModal, deployableVersion }) => {
  const { commitMessage, committerAvatarUrl, committerName, gitCommitSha, commitUrl } = deployableVersion;
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
      {deployableVersion ? (
        <CommitDetailsCard
          commitMessage={commitMessage}
          committerAvatarUrl={committerAvatarUrl}
          committerName={committerName}
          gitCommitSha={gitCommitSha}
          commitUrl={commitUrl}
        />
      ) : (
        <div className="modal-spinner-wrapper">
          <Spinner />
        </div>
      )}
    </AppModal>
  );
};
