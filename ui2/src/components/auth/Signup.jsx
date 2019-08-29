import React, { useEffect } from 'react';
import { SignupForm } from './SignupForm';
import { appNotification } from '../../common/notification';
import { connect } from 'react-redux';
import { signup, getDeploymentRoles, login } from '../../store/actions/authActions';
import { Spinner } from '../../common/Spinner';
import './Signup.css';

const SignupComponent = ({ signup, getDeploymentRoles, depRoles, error, isLoading, resetBreadcrumbs }) => {
  const handleSubmit = async (userDetails, resetForm, setSubmitting) => {
    try {
      await signup(userDetails);
      resetForm();
      appNotification(`${userDetails.firstName} ${userDetails.lastName} was added`);
    } catch (error) {
      setSubmitting(false);
    }
  };

  useEffect(() => {
    resetBreadcrumbs();
    getDeploymentRoles();
  }, [getDeploymentRoles]);

  if (!depRoles) {
    return <Spinner />;
  }

  return (
    <div className="signup">
      <div className="signup-title med-title">Please add new user details...</div>
      <div className="form-error">{error}</div>
      <SignupForm handleSubmit={handleSubmit} options={depRoles} isLoading={isLoading} />
    </div>
  );
};

const mapStateToProps = ({ auth: { depRoles, isLoading, error } }) => ({ depRoles, isLoading, error });

export const Signup = connect(
  mapStateToProps,
  { signup, getDeploymentRoles, login },
)(SignupComponent);
