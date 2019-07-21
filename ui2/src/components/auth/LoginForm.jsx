import React from 'react';
import {Formik, Field} from 'formik';
import {loginSchema} from '../../utils/validations/authValidation';
import {InputField} from '../../common/FormFields';
import {Form} from 'antd';
import Button from '../../common/Button';
import './LoginForm.css';

const LoginForm = ({handleSubmit}) => (
    <Formik
        className="login-form"
        initialValues={{userEmail: '', password: ''}}
        validationSchema={loginSchema}
        onSubmit={(values, {resetForm, setSubmitting}) => {
            handleSubmit(values, resetForm, setSubmitting);
        }}
    >
        {({
              handleSubmit,
              isSubmitting
          }) => (
            <Form onSubmit={handleSubmit} className="login-form">
                <Field name="username" type="mail" placeholder={"Email"} component={InputField}/>
                <Field name="password" type="lock" placeholder={"Password"} component={InputField}/>
                <Button label="Login" type="primary" htmlType="submit" className="submit-form"
                        disabled={isSubmitting}/>
            </Form>
        )}
    </Formik>
);

export default LoginForm;
