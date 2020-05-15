import React from 'react';
import { Form, Input, Icon, Select } from 'antd';
import './FormFields.css';

export const InputField = ({ field: { name, ...rest }, form: { touched, errors }, type, iconType, placeholder }) => (
  <Form.Item>
    <Input
      {...rest}
      className={`field-input input-${name}`}
      name={name}
      type={type}
      prefix={<Icon type={iconType} className="field-icon" />}
      placeholder={placeholder}
    />
    {touched[name] && errors[name] && <div className="field-error">{errors[name]}</div>}
  </Form.Item>
);

export const SelectField = ({ field, options, setFieldValue }) => {
  return (
    <Form.Item>
      <Select
        {...field}
        onChange={value => {
          setFieldValue(field.name, value);
        }}
      >
        {options &&
          options.map(({ id, name }) => (
            <Select.Option value={id} key={id}>
              {name}
            </Select.Option>
          ))}
      </Select>
    </Form.Item>
  );
};

export const AppInput = ({ placeholder, onChange, value }) => (
  <Input placeholder={placeholder} onChange={onChange} value={value} />
);
