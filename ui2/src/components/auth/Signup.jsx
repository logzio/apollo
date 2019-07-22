import React, { useEffect } from 'react';
import SignupForm from './SignupForm';
import { notification } from 'antd';
import { connect } from 'react-redux';
import { signup, getDeploymentRoles, login } from './authActions';
import Spinner from '../../common/Spinner';
import './Signup.css';

const SignupComponent = ({ signup, getDeploymentRoles, depRoles, error, isLoading }) => {
  const handleSubmit = async (userDetails, resetForm, setSubmitting) => {
    try {
      await signup(userDetails);
      resetForm();
      notification.open({
        message: `${userDetails.firstName} ${userDetails.lastName} was added`,
      });
    } catch (error) {
      setSubmitting(false);
    }
  };

  useEffect(() => {
    getDeploymentRoles();
  }, [getDeploymentRoles]);

  if (!depRoles) {
    return <Spinner />;
  }

  return (
    <div className="signup">
      <div className="form-error">{error}</div>
      <SignupForm handleSubmit={handleSubmit} options={depRoles} isLoading={isLoading} />
    </div>
  );
};

const mapStateToProps = state => {
  const { auth } = state;
  return {
    depRoles: auth.depRoles,
    error: auth.error,
    isLoading: auth.isLoading,
  };
};

const Signup = connect(
  mapStateToProps,
  { signup, getDeploymentRoles, login },
)(SignupComponent);

export default Signup;
