import React from 'react';
import { Icon, Tooltip } from 'antd';

export const AppIcon = ({ type, className, theme, tooltipText, ...props }) => (
  <Tooltip title={tooltipText}>
    <Icon className={`${type}-icon ${className}`} type={type} theme={theme} {...props} />
  </Tooltip>
);
