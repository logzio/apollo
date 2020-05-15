import React from 'react';
import { Formik, Field } from 'formik';
import { signupSchema } from '../../utils/validations/authValidation';
import { InputField, SelectField } from '../../common/FormFields';
import { Form } from 'antd';
import { AppButton } from '../../common/Button';
import './SignupForm.css';

export const SignupForm = ({ handleSubmit, options, isLoading }) => (
  <Formik
    className="signup-form"
    initialValues={{ firstName: '', lastName: '', userEmail: '', password: '', deploymentRoleId: options[0].id }}
    validationSchema={signupSchema}
    onSubmit={(values, { resetForm, setSubmitting }) => {
      handleSubmit(values, resetForm, setSubmitting);
    }}
  >
    {({ handleSubmit, isSubmitting, setFieldValue }) => (
      <Form onSubmit={handleSubmit} className="signup-form">
        <Field name="firstName" iconType="user" placeholder={'First Name'} component={InputField} />
        <Field name="lastName" iconType="user" placeholder={'Last Name'} component={InputField} />
        <Field name="userEmail" iconType="mail" placeholder={'Email'} component={InputField} />
        <Field name="password" iconType="lock" placeholder={'Password'} component={InputField} />
        <Field name="deploymentRoleId" component={SelectField} options={options} setFieldValue={setFieldValue} />
        <AppButton
          label="Add user"
          type="primary"
          htmlType="submit"
          className="submit-form"
          disabled={isSubmitting}
          isLoading={isLoading}
        />
      </Form>
    )}
  </Formik>
);
