import React, {useEffect} from 'react';
import SignupForm from './SignupForm';
import {notification} from 'antd';
import {connect} from "react-redux";
import {signup, getDeploymentRoles, login} from "./authActions";
import Spinner from "../../common/Spinner";
import './Signup.css';


const SignupComponent = ({signup, getDeploymentRoles, isLoading, depRoles, error, login}) => {

    const handleSubmit = async (userDetails, resetForm, setSubmitting) => {
        try {
            await signup(userDetails);
            resetForm();
            notification.open({
                message: `${userDetails.firstName} ${userDetails.lastName} was added`
            });
        } catch (error) {
            setSubmitting(false);
        }
    };

    useEffect(() => {
        const temp = () => {
             // login();
             getDeploymentRoles();
        };
        temp();
    }, [login, getDeploymentRoles]);

    if (isLoading) {
        return <Spinner/>;
    }

    return (
        <div className="signup">
            {error && <div className="form-error">{error}</div>}
            <SignupForm handleSubmit={handleSubmit} options={depRoles}/>
        </div>
    );
};

const mapStateToProps = (state) => {
    const {auth} = state;
    return ({
        isLoading: auth.isLoading,
        depRoles: auth.depRoles,
        error: auth.error
    })
};


const Signup = connect(
    mapStateToProps,
    {signup, getDeploymentRoles, login},
)(SignupComponent);


export default Signup;
