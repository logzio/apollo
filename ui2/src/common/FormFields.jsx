import React from "react";
import {Form, Input, Icon, Select} from 'antd';
import './FormFields.css';

export const InputField = ({
                               field,
                               form: {touched, errors},
                               type,
                               placeholder
                           }) => (
    <Form.Item>
        <Input
            className={`field-input input-${field.name}`}
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

export const SelectField = ({
                                field,
                                options
                            }) => (
    <Form.Item>
        <Select name={field.name} defaultValue={options && options[0].name}>
            {options && options.map((option) => (
                    <Select.Option value={option.id} key={option.id}>{option.name}</Select.Option>
                )
            )}
        </Select>
    </Form.Item>
);

