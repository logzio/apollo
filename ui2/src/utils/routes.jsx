import React from "react";
import {Route, Redirect} from "react-router-dom";
import {connect} from "react-redux";
import Container from "../components/app/Container";
import Login from '../components/auth/Login';

const PrivateRouteEl = ({loggedIn, path, component, title}) => {
    return loggedIn ? (
        <Route path={path} render={({match, ...props}) => <Container title={title} component={component} match={match}/>}/>
    ) : (
        <Redirect to="/auth/login"/>
    );
};

const PublicRouteEl = ({loggedIn}) => {
    return loggedIn ? (
        <Redirect to="/auth/addUser"/>  //temp, will be directed to home
    ) : (
        <Route path="/auth/login" component={Login}/>
    );
};

const mapStateToProps = (state) => {
    const {auth} = state;
    return ({
        loggedIn: auth.loggedIn,
        isAdmin: auth.isAdmin
    })
};


export const PrivateRoute = connect(mapStateToProps)(PrivateRouteEl);
export const PublicRoute = connect(mapStateToProps)(PublicRouteEl);
