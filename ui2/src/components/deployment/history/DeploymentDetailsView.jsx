import React from 'react';
import { AppModal } from '../../../common/Modal';
import { AppButton } from '../../../common/Button';
import { Spinner } from '../../../common/Spinner';
import { DeploymentDetailsCard } from '../DeploymentDetailsCard';

export const DeploymentDetailsView = ({ toggleShowModal, deployableVersion }) => (
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
      <DeploymentDetailsCard
        commitMessage={deployableVersion.commitMessage}
        committerAvatarUrl={deployableVersion.committerAvatarUrl}
        committerName={deployableVersion.committerName}
        gitCommitSha={deployableVersion.gitCommitSha}
        commitUrl={deployableVersion.commitUrl}
      />
    ) : (
      <Spinner />
    )}
  </AppModal>
);
