import React from 'react';
import { Formik, Field } from 'formik';
import {SignupSchema} from '../../utils/validations/authValidation';
import {InputField, SelectField} from '../../common/FormFields';
import { Form } from 'antd';
import Button from '../../common/Button';
import './SignupForm.css';

const SignupForm = ({handleSubmit, options}) => (
        <Formik
            className="signup-form"
            initialValues={{ firstName: '', lastName: '', userEmail: '', password: '' }}
            validationSchema={SignupSchema}
            onSubmit={(values, {resetForm, setSubmitting}) => {
               handleSubmit(values, resetForm, setSubmitting);
            }}
        >
            {({
                  handleSubmit,
                  isSubmitting
              }) => (
                <Form onSubmit={handleSubmit} className="signup-form">
                    <Field name="firstName" type="user" placeholder={"First Name"} component={InputField} />
                    <Field name="lastName" type="user" placeholder={"Last Name"}  component={InputField} />
                    <Field name="userEmail" type="mail" placeholder={"Email"}  component={InputField} />
                    <Field name="password" type="lock" placeholder={"Password"}  component={InputField} />
                    <Field name="roleDeployment" component={SelectField} options={options}/>
                    <Button label="Add user" type="primary" htmlType="submit" className="submit-form" disabled={isSubmitting}/>
                </Form>
            )}
        </Formik>
);

export default SignupForm;
