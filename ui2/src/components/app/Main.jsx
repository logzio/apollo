import React, { useState } from 'react';
import Navbar from './Navbar';
import {Layout} from 'antd';
import { Route, Redirect } from "react-router-dom";
import Container from "../../common/Container";
import Signup from '../auth/Signup';
import './Main.css';


const routes = [
    {
        path: 'index',
        breadcrumbName: 'First-level Menu',
    },
    {
        path: 'first',
        breadcrumbName: 'Second-level Menu',
    },
    {
        path: 'second',
        breadcrumbName: 'Third-level Menu',
    },
];

const Main = () => {

    const [collapsed, toggleCollapse] = useState(false);

    return(
        <Layout className="main">
            <Layout.Sider trigger={null} collapsible collapsed={collapsed}>
                <Navbar toggleCollapsed={()=>toggleCollapse(!collapsed)} collapsed={collapsed}/>
            </Layout.Sider>
            <Layout>
                <Layout.Content className="main-content">
                    <Route path="/auth/signup" render={()=> <Container title={"Add a new user"} content={<Signup/>} routes={routes}/>} />
                    <Redirect to="/auth/signup" />
                </Layout.Content>
            </Layout>
        </Layout>
    );

};

export default Main;
