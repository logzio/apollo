import React, { useEffect, useState } from 'react';
import { Router, Route } from 'react-router-dom';
import { history } from '../../utils/history';
import { connect } from 'react-redux';
import { appInit, logout } from '../../store/actions/authActions';
import { Signup } from '../auth/Signup';
import { Login } from '../auth/Login';
import { Switch, Redirect } from 'react-router-dom';
import { Layout } from 'antd';
import { Navbar } from './Navbar';
import { getAuthToken } from '../../api/api';
import { PrivateRoute, PublicRoute } from '../../utils/routes';
import { NewDeployment } from '../deployment/new/NewDeployment';
import './App.css';

const AppComponent = ({ appInit, logout, isAdmin }) => {
  useEffect(() => {
    appInit();
  }, [appInit]);

  const [collapsed, toggleCollapse] = useState(false);
  const loggedIn = !!getAuthToken();

  return (
    <Router history={history}>
      <Layout className="app">
        {loggedIn && (
          <Layout.Sider trigger={null} collapsible collapsed={collapsed}>
            <Navbar
              toggleCollapsed={() => toggleCollapse(!collapsed)}
              collapsed={collapsed}
              handleLogout={logout}
              isAdmin={isAdmin}
            />
          </Layout.Sider>
        )}
        <Layout>
          <Layout.Content className="app-content">
            <Switch>
              {isAdmin && <PrivateRoute path="/auth/addUser" title={'Add a new user'} component={Signup} />}
              <PrivateRoute path="/deployment/new" title={'New deployment'} component={NewDeployment} />
              <PublicRoute path="/auth/login" component={Login} />
              <Redirect to={`/auth/addUser`} />
            </Switch>
          </Layout.Content>
        </Layout>
      </Layout>
    </Router>
  );
};

const mapStateToProps = ({ auth: { loggedIn, isAdmin } }) => ({ loggedIn, isAdmin });

export const App = connect(
  mapStateToProps,
  { appInit, logout },
)(AppComponent);
