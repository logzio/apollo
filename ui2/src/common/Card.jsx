import React from 'react';
import { Card } from 'antd';
import './Card.css';

export const AppCard = ({ children, className, ...props }) => (
  <Card bordered={false} className={`${className} app-card`} {...props}>
    {children}
  </Card>
);
