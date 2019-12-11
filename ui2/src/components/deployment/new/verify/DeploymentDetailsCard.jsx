import React from 'react';
import { Avatar, Col, List } from 'antd';
import { AppCard } from '../../../../common/Card';
import symbol from '../../../../assets/images/apollo-symbol.svg';

export const DeploymentDetailsCard = ({ deployItems, group }) => {
  return (
    <>
      {deployItems.map(({ isPartOfGroup, dataSource, title }, index) => {
        const isSelectedGroups = !isPartOfGroup || !!group;
        return (
          isSelectedGroups && (
            <Col span={group ? 5 : 4} offset={group ? 1 : 4} key={index}>
              <AppCard title={title}>
                <List
                  itemLayout="horizontal"
                  dataSource={dataSource}
                  renderItem={({ name }) => (
                    <List.Item>
                      <List.Item.Meta
                        avatar={<Avatar className={'list-icon'} src={symbol} />}
                        title={<div>{name}</div>}
                      />
                    </List.Item>
                  )}
                />
              </AppCard>
            </Col>
          )
        );
      })}
    </>
  );
};
