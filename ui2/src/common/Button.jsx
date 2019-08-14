import React from 'react';
import { Button, Tooltip } from 'antd';
import './Button.css';

export const AppButton = ({ label, className, isLoading,tooltipText, ...props }) => (
    <Tooltip title={tooltipText}>
  <Button className={`${className} app-btn`} loading={isLoading} {...props}>
    {label}
  </Button>
    </Tooltip>
);
