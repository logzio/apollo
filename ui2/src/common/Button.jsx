import React from 'react';
import { Button } from 'antd';
import './Button.css';

export const AppButton = ({ label, type, htmlType, className, disabled, isLoading, onClick, key }) => (
  <Button
    type={type}
    htmlType={htmlType}
    className={`${className} app-btn`}
    disabled={disabled}
    onClick={onClick}
    key={key}
    loading={isLoading}
  >
    {label}
  </Button>
);
