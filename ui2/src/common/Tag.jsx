import React from 'react';
import { Tag, Tooltip } from 'antd';
import './Tag.css';

export const AppTag = ({ children, color, tooltipText, ...props }) => (
  <Tooltip title={tooltipText}>
    <Tag color={color} className="app-tag" {...props}>
      {children}
    </Tag>
  </Tooltip>
);
