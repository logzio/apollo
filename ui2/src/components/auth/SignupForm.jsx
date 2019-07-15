import React from 'react';
import { Formik, Field } from 'formik';
import {SignupSchema} from '../../utils/validations/authValidation';
import {FormField} from '../../common/FormField';
import { Form } from 'antd';
import Button from '../../common/Button';
import './SignupForm.css';

const SignupForm = ({handleSubmit}) => (
        <Formik
            className="signup-form"
            initialValues={{ firstName: '', lastName: '', userEmail: '', password: '' }}
            validationSchema={SignupSchema}
            onSubmit={(values)=>{
                handleSubmit(values)}
            }
        >
            {({
                  values,
                  errors,
                  touched,
                  handleChange,
                  handleBlur,
                  handleSubmit
              }) => (
                <Form onSubmit={handleSubmit} className="signup-form">
                    <Field name="firstName" type="user" placeholder={"First Name"} component={FormField} />
                    <Field name="lastName" type="user" placeholder={"Last Name"}  component={FormField} />
                    <Field name="userEmail" type="mail" placeholder={"Email"}  component={FormField} />
                    <Field name="password" type="lock" placeholder={"Password"}  component={FormField} />
                    <Button label="Add user" type="primary" htmlType="submit" className="submit-form" />
                </Form>
            )}
        </Formik>
);

export default SignupForm;
