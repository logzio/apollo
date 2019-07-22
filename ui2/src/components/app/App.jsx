import React, {useEffect, useState} from 'react';
import {Router} from "react-router-dom";
import history from '../../utils/history';
import {connect} from "react-redux";
import {appInit, logout} from "../auth/authActions";
import {PrivateRoute, PublicRoute} from "../../utils/routes";
import Signup from "../auth/Signup";
import Login from "../auth/Login";
import {Switch, Redirect} from "react-router-dom";
import {Layout} from "antd";
import Navbar from "./Navbar";
import './App.css';


const AppComponent = ({loggedIn, appInit, logout, isAdmin}) => {

    useEffect(() => {
        appInit();
    }, [appInit]);

    const [collapsed, toggleCollapse] = useState(false);

    return (
        <Router history={history}>
            <Layout className="app">
                {loggedIn &&
                <Layout.Sider trigger={null} collapsible collapsed={collapsed}>
                    <Navbar toggleCollapsed={() => toggleCollapse(!collapsed)} collapsed={collapsed}
                            handleLogout={logout} isAdmin={isAdmin}/>
                </Layout.Sider>
                }
                <Layout>
                    <Layout.Content className="app-content">
                        <Switch>
                            {isAdmin &&
                                <PrivateRoute path="/auth/addUser" title={"Add a new user"} component={Signup}/>}
                            <PublicRoute path="/auth/login" component={Login}/>
                            <Redirect to={"/auth/home"}/>
                        </Switch>
                    </Layout.Content>
                </Layout>
            </Layout>
        </Router>
    );
};


const mapStateToProps = (state) => {
    const {auth} = state;
    return ({
        loggedIn: auth.loggedIn,
        isLoading: auth.isLoading,
        isAdmin: auth.isAdmin
    })
};


const App = connect(
    mapStateToProps,
    {appInit, logout},
)(AppComponent);


export default App;
