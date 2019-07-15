import React from "react";
import {Form, Input, Icon } from 'antd';
import './FormField.css';

export const FormField = ({
                                  field, // { name, value, onChange, onBlur }
                                  form: { touched, errors }, // also values, setXXXX, handleXXXX, dirty, isValid, status, etc.
                                  type,
                                  placeholder,
                                  ...props
                              }) => (
    <Form.Item>
        <Input
            className="field-input"
            name={field.name}
            type={"string"}
            prefix={<Icon type={type} className="field-icon"/>}
            placeholder={placeholder}
            onChange={field.onChange}
            onBlur={field.onBlur}
            value={field.value}
        />
        {touched[field.name] && errors[field.name] && <div className="field-error">{errors[field.name]}</div>}
    </Form.Item>
);
