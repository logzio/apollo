import React from 'react';
import { Button } from 'antd';
import './Button.css';

const AppButton = ({label, type, htmlType, className}) => (
        <Button type={type} htmlType={htmlType} className={`${className} app-btn`}>
            {label}
        </Button>
);

export default AppButton;
