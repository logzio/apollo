import React from 'react';
import { AppCard } from '../../common/Card';

export const DeploymentDetailsCard = ({
  committerAvatarUrl,
  committerName,
  gitCommitSha,
  commitUrl,
  commitMessage,
  title,
}) => (
  <AppCard title={title}>
    <div className="card-details">
      <div className={'extra-small-title'}>Author:</div>
      <div className="card-user-profile">
        {committerAvatarUrl && <img className="card-user-image" src={committerAvatarUrl} alt={'user profile'} />}
        <div className="card-user-title">{committerName}</div>
      </div>
    </div>
    <div className="card-details">
      <div className={'extra-small-title'}>Commit Sha: </div>
      <a href={commitUrl} rel="noopener noreferrer" target="_blank">
        {gitCommitSha && gitCommitSha.slice(0, 7)}
      </a>
    </div>
    <div className="card-details">
      <div className={'extra-small-title'}>Message:</div>
      {commitMessage && commitMessage.split('*').shift()}
    </div>
  </AppCard>
);
