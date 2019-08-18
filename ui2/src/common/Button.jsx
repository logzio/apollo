import React from 'react';
import { Button, Tooltip } from 'antd';
import './Button.css';

<<<<<<< HEAD
export const AppButton = ({ label, className, isLoading,tooltipText, ...props }) => (
    <Tooltip title={tooltipText}>
  <Button className={`${className} app-btn`} loading={isLoading} {...props}>
    {label}
  </Button>
    </Tooltip>
=======
export const AppButton = ({
  label,
  type,
  htmlType,
  className,
  disabled,
  isLoading,
  onClick,
  key,
  ghost,
  icon,
  shape,
  tooltipText,
}) => (
  <Tooltip title={tooltipText}>
    <Button
      type={type}
      htmlType={htmlType}
      className={`${className} app-btn`}
      disabled={disabled}
      onClick={onClick}
      key={key}
      loading={isLoading}
      ghost={ghost}
      icon={icon}
      shape={shape}
    >
      {label}
    </Button>
  </Tooltip>
>>>>>>> 8436fe65b69ff51cdba606535e3a2a8a6a8aed05
);
