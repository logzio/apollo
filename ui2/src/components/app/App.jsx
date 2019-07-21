import React, {useEffect, useState} from 'react';
import {BrowserRouter as Router} from "react-router-dom";
import history from '../../utils/history';
import {connect} from "react-redux";
import {appInit} from "../auth/authActions";
import {PrivateRoute, PublicRoute} from "../../utils/routes";
import Signup from "../auth/Signup";
import Login from "../auth/Login";
import {Switch, Redirect} from "react-router-dom";
import {Layout} from "antd";
import Navbar from "./Navbar";
import 'antd/dist/antd.css';
import './App.css';


const AppComponent = ({loggedIn, appInit}) => {

    useEffect(() => {
        appInit();
    }, [loggedIn]);

    const [collapsed, toggleCollapse] = useState(false);

    return (
        <Router history={history}>
            <Layout className="app">
                {loggedIn &&
                <Layout.Sider trigger={null} collapsible collapsed={collapsed}>
                    <Navbar toggleCollapsed={() => toggleCollapse(!collapsed)} collapsed={collapsed}/>
                </Layout.Sider>
                }
                <Layout>
                    <Layout.Content className="app-content">
                        <Switch>
                            <PrivateRoute path="/auth/signup" title={"Add a new user"} component={Signup}/>
                            <PublicRoute path="/auth/login" component={Login}/>
                            <Redirect to={"/auth/signup"}/>
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
        loggedIn: auth.loggedIn
    })
};


const App = connect(
    mapStateToProps,
    {appInit},
)(AppComponent);


export default App;
