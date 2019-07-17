import React, { useEffect } from 'react';
import SignupForm from './SignupForm';
import { notification } from 'antd';
import {connect} from "react-redux";
import {signup, getDeploymentRoles} from "./authActions";
import Spinner from "../../common/Spinner";
import './Signup.css';


const SignupComponent = ({signup, getDeploymentRoles, isLoading, depRoles, error}) => {

    const handleSubmit = async(userDetails, resetForm, setSubmitting) => {
        try {
            await signup(userDetails);
            resetForm();
            notification.open({
                message: `${userDetails.firstName} ${userDetails.lastName} was added`
        });
        }catch(error) {
            setSubmitting(false);
        }
    };

    useEffect(() => {
        getDeploymentRoles();
    }, [error]);

    if(isLoading){
        return <Spinner/>;
    }

    return (
        <div className="signup">
            <div className="form-error">{error}</div>
            <SignupForm handleSubmit={handleSubmit} options={depRoles}/>
        </div>
    );
};

const mapStateToProps = (state) => {
    const {auth} = state;
    return({
        isLoading: auth.isLoading,
        depRoles: auth.depRoles,
        error: auth.error
    })
};


const Signup = connect(
    mapStateToProps,
    {signup, getDeploymentRoles},
)(SignupComponent);


export default Signup;
