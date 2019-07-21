import React from 'react';
import {Button} from 'antd';
import './Button.css';

const AppButton = ({label, type, htmlType, className, disabled}) => (
    <Button type={type} htmlType={htmlType} className={`${className} app-btn`} disabled={disabled}>
        {label}
    </Button>
);

export default AppButton;
