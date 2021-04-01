import React from 'react';
import { connect } from 'react-redux';
import { login } from '../../store/actions/authActions';
import { LoginForm } from './LoginForm';
import Logo from '../../assets/images/apollo-logo.svg';
import './Login.css';

const LoginComponent = ({ isLoading, error, login }) => {
  const handleSubmit = async (userDetails, setSubmitting) => {
    try {
      await login(userDetails);
    } catch (error) {
      setSubmitting(false);
    }
  };

  return (
    <div className="login">
      <div className="login-title">
        <img className="login-logo" src={Logo} alt="Apollo logo" />
      </div>
      <div className="form-error">{error}</div>
      <LoginForm handleSubmit={handleSubmit} isLoading={isLoading} />
    </div>
  );
};

const mapStateToProps = ({ auth: { isLoading, error } }) => ({ isLoading, error });

export const Login = connect(
  mapStateToProps,
  { login },
)(LoginComponent);
