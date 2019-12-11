import React from 'react';
import { Col, Row } from 'antd';
import { CommitDetailsCard } from '../../CommitDetailsCard';
import { DeploymentDetailsCard } from './DeploymentDetailsCard';
import './DeploymentDetailsContainer.css';

export const DeploymentDetailsContainer = ({
  selectedServices,
  selectedEnvironments,
  selectedVersion,
  selectedGroups,
  group,
}) => {
  const deployItems = [
    { title: 'Services to deploy:', dataSource: selectedServices, isPartOfGroup: false },
    { title: 'Environments to deploy:', dataSource: selectedEnvironments, isPartOfGroup: false },
    { title: 'Groups to deploy:', dataSource: selectedGroups, isPartOfGroup: true },
  ];

  const { commitMessage, committerAvatarUrl, committerName, gitCommitSha, commitUrl } = selectedVersion;
  return (
    <div className="verify-deployment">
      <Row className={'verify-deployment-content'}>
        <Col span={5} offset={1}>
          <CommitDetailsCard
            title={'Version to deploy: '}
            commitMessage={commitMessage}
            committerAvatarUrl={committerAvatarUrl}
            committerName={committerName}
            gitCommitSha={gitCommitSha}
            commitUrl={commitUrl}
          />
        </Col>
        <DeploymentDetailsCard deployItems={deployItems} group={group} />
      </Row>
    </div>
  );
};
