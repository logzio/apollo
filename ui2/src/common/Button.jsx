import React from 'react';
import { Button } from 'antd';
import './Button.css';

export const AppButton = ({ label, className, isLoading, ...props }) => (
  <Button className={`${className} app-btn`} loading={isLoading} {...props}>
    {label}
  </Button>
);
