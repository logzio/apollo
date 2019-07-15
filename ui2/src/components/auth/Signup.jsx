import React from 'react';
import SignupForm from './SignupForm';
import './Signup.css';
import {connect} from "react-redux";
import {signup} from "./authActions";

const Signup = ({signup, isLoading}) => {
    const handleSubmit = (userDetails) => {
        signup(userDetails);
    };

    return (
        <div className="signup">
            <SignupForm handleSubmit={handleSubmit}/>
        </div>
    );
};

const mapStateToProps = (state) => {
    const {auth} = state;
    return({
        isLoading: auth.isLoading
    })
};


export const SignupComponent = connect(
    mapStateToProps,
    {signup},
)(Signup);


export default SignupComponent;
