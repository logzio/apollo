import React from 'react';
import { Formik, Field } from 'formik';
import {SignupSchema} from '../../utils/validations/authValidation';
import {FormField} from '../../utils/FormField';
import { Form } from 'antd';
import Button from '../../utils/Button';
import './SignupForm.css';

const SignupForm = () => (
        <Formik
            className="signup-form"
            initialValues={{ email: '', password: '' }}
            validationSchema={SignupSchema}
            onSubmit={values => {
                console.log(values);
            }}
        >
            {({
                  values,
                  errors,
                  touched,
                  handleChange,
                  handleBlur,
                  handleSubmit,
              }) => (
                <Form onSubmit={handleSubmit} className="signup-form">
                    <Field name="firstName" type="user" placeholder={"First Name"} component={FormField} />
                    <Field name="lastName" type="user" placeholder={"Last Name"}  component={FormField} />
                    <Field name="email" type="mail" placeholder={"Email"}  component={FormField} />
                    <Field name="password" type="lock" placeholder={"Password"}  component={FormField} />
                    <Button label="Add me!" type="primary" htmlType="submit" className="login-form-button" />
                </Form>
            )}
        </Formik>
);

export default SignupForm;
