import { Route, Redirect } from "react-router-dom";
import {connect} from "react-redux";
import Container from "../components/app/Container";
import Login from '../components/auth/Login';
import React from "react";

const PrivateRouteEl = ({loggedIn, path, component: Component, title}) => {
    return loggedIn ? (
        <Route path={path} render={({match, ...props}) => <Container title={title} content={<Component {...props}/>} match={match} />} />
    ) : (
        <Redirect to="/auth/login" />
    );
};

const PublicRouteEl = ({loggedIn}) => {
    return loggedIn ? (
        <Redirect to="/auth/signup" />
    ) : (
        <Route path="/auth/login" component={Login} />
    );
};

const mapStateToProps = (state) => {
    const {auth} = state;
    return({
        loggedIn: auth.loggedIn,
    })
};


export const PrivateRoute = connect(mapStateToProps)(PrivateRouteEl);
export const PublicRoute = connect(mapStateToProps)(PublicRouteEl);

