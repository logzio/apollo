import React from 'react';
import Navbar from './Navbar';
import Header from "./Header";
import Signup from '../auth/Signup';
import {Row, Col } from 'antd';
import { BrowserRouter as Router, Route, Switch, Redirect } from "react-router-dom";


export default class Main extends React.Component {

    state = {
        collapsed: false,
    };

    toggleCollapsed = () => {
        this.setState({
            collapsed: !this.state.collapsed
        });
    };

    render(){
        const {collapsed} = this.state;

        return (
            <Router>
                <Switch>
                    <Row>
                        <Col span={collapsed ? 1 : 4}>
                            <Navbar collapsed={collapsed}/>
                        </Col>
                        <Col span={20}>
                            <Header toggleCollapsed={this.toggleCollapsed} collapsed={collapsed}/>
                            <Route path="/auth/signup" component={Signup} />
                            <Redirect to="/auth/signup" />
                        </Col>
                    </Row>
                </Switch>
            </Router>
        );
    };
}
