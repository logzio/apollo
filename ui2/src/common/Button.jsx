import React from 'react';
import {Button} from 'antd';
import './Button.css';
import Spinner from "./Spinner";

const AppButton = ({label, type, htmlType, className, disabled, isLoading}) => (
    <Button type={type} htmlType={htmlType} className={`${className} app-btn`} disabled={disabled}>
        {!isLoading?
            label
            :
            <Spinner theme={'bright'}/>
        }
    </Button>
);

export default AppButton;
