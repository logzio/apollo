import React from 'react';
import { Form, Input, Icon, Select } from 'antd';
import './FormFields.css';

export const InputField = ({ field, form: { touched, errors }, type, iconType, placeholder }) => (
  <Form.Item>
    <Input
      className={`field-input input-${field.name}`}
      name={field.name}
      type={type}
      prefix={<Icon type={iconType} className="field-icon" />}
      placeholder={placeholder}
      onChange={field.onChange}
      onBlur={field.onBlur}
      value={field.value}
    />
    {touched[field.name] && errors[field.name] && <div className="field-error">{errors[field.name]}</div>}
  </Form.Item>
);

export const SelectField = ({ field: { name }, options }) => (
  <Form.Item>
    <Select name={name} defaultValue={options && options[0].name}>
      {options &&
        options.map(({ id, name }) => (
          <Select.Option value={id} key={id}>
            {name}
          </Select.Option>
        ))}
    </Select>
  </Form.Item>
);

export const AppInput = ({ placeholder, onChange, value }) => (
  <Input placeholder={placeholder} onChange={onChange} value={value} />
);
