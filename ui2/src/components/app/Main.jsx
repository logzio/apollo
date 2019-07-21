import React, {useState} from 'react';
import Navbar from './Navbar';
import {Layout} from 'antd';
import Signup from '../auth/Signup';
import Login from '../auth/Login';
import {PrivateRoute, PublicRoute} from '../../utils/routes';
import {Switch, Redirect} from "react-router-dom";
import './Main.css';

const Main = () => {

    const [collapsed, toggleCollapse] = useState(false);

    return (
        <Layout className="main">
            <Layout.Sider trigger={null} collapsible collapsed={collapsed}>
                <Navbar toggleCollapsed={() => toggleCollapse(!collapsed)} collapsed={collapsed}/>
            </Layout.Sider>
            <Layout>
                <Layout.Content className="main-content">
                    <Switch>
                        <PrivateRoute path="/auth/signup" title={"Add a new user"} component={Signup}/>
                        <PublicRoute path="/auth/login" component={Login}/>
                        <Redirect to={"/auth/signup"}/>
                    </Switch>
                </Layout.Content>
            </Layout>
        </Layout>
    );

};

export default Main;
