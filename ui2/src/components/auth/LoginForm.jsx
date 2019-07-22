import React from 'react';
import {Formik, Field} from 'formik';
import {loginSchema} from '../../utils/validations/authValidation';
import {InputField} from '../../common/FormFields';
import {Form} from 'antd';
import Button from '../../common/Button';
import './LoginForm.css';

const LoginForm = ({handleSubmit, isLoading}) => (
    <Formik
        className="login-form"
        initialValues={{username: '', password: ''}}
        validationSchema={loginSchema}
        onSubmit={(values, {setSubmitting}) => {
            handleSubmit(values, setSubmitting);
        }}
    >
        {({
              handleSubmit,
              isSubmitting
          }) => (
            <Form onSubmit={handleSubmit} className="login-form">
                <Field name="username" type="email" iconType="mail" placeholder={"Email"} component={InputField}/>
                <Field name="password" type="password" iconType="lock" placeholder={"Password"} component={InputField}/>
                <Button label="Login" type="primary" htmlType="submit" className="submit-form"
                        disabled={isSubmitting} isLoading={isLoading}/>
            </Form>
        )}
    </Formik>
);

export default LoginForm;
