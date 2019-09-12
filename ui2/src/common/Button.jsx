import React from 'react';
import { Button, Tooltip } from 'antd';
import './Button.css';

export const AppButton = ({ label, className, tooltipText, ...props }) => (
  <Tooltip title={tooltipText}>
    <Button className={`${className} app-btn`} {...props}>
      {label}
    </Button>
  </Tooltip>
);
