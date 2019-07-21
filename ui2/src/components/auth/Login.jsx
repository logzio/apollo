import React from 'react';
import {connect} from "react-redux";
import {login} from "./authActions";
import Spinner from "../../common/Spinner";
import LoginForm from "./LoginForm";
import './Login.css';


const LoginComponent = ({isLoading, error, login}) => {

    const handleSubmit = async (userDetails, resetForm, setSubmitting) => {
        try {
            await login(userDetails);
            resetForm();
        } catch (error) {
            setSubmitting(false);
        }
    };

    if (isLoading) {
        return <Spinner/>;
    }

    return (
        <div className="login">
            <div className="form-error">{error}</div>
            <LoginForm handleSubmit={handleSubmit} />
        </div>
    );
};

const mapStateToProps = (state) => {
    const {auth} = state;
    return ({
        isLoading: auth.isLoading,
        error: auth.error
    })
};


const Login = connect(
    mapStateToProps,
    {login},
)(LoginComponent);


export default Login;
