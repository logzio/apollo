import React from "react";
import {Form, Input, Icon } from 'antd';

export const FormField = ({
                                  field, // { name, value, onChange, onBlur }
                                  form: { touched, errors }, // also values, setXXXX, handleXXXX, dirty, isValid, status, etc.
                                  type,
                                  placeholder,
                                  ...props
                              }) => (
    <Form.Item>
        <Input
            prefix={<Icon type={type} style={{ color: 'rgba(0,0,0,.25)' }} />}
            placeholder={placeholder}
        />
    </Form.Item>
);


{/*<input*/}
    // type="email"
    // name="email"
    // onChange={handleChange}
    // onBlur={handleBlur}
    // value={values.email}
// />
// {errors.email && touched.email && errors.email}


{/*<input type={type} {...field} {...props} />*/}
// {touched[field.name] &&
// errors[field.name] && <div className="error">{errors[field.name]}</div>}
