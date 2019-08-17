import React, { useEffect, useState } from 'react';
import { Router, Route, Switch, Redirect } from 'react-router-dom';
import { history } from '../../utils/history';
import { connect } from 'react-redux';
import { appInit, logout } from '../../store/actions/authActions';
import { Signup } from '../auth/Signup';
import { Login } from '../auth/Login';
import { Layout } from 'antd';
import { Navbar } from './Navbar';
import { getAuthToken } from '../../api/api';
import { NewDeployment } from '../deployment/new/NewDeployment';
import './App.css';
import { Container } from './Container';

const AppComponent = ({ appInit, logout, isAdmin, loggedIn }) => {
  useEffect(() => {
    appInit();
  }, [appInit, loggedIn]);

  const [collapsed, toggleCollapse] = useState(false);
  const isAuthenticate = !!getAuthToken();
  const AppRoute = ({ path, ...props }) => {
    return isAuthenticate ? (
      <Route path={path} render={({ match }) => <Container match={match} {...props} />} />
    ) : (
      <Redirect to="/auth/login" />
    );
  };

  return (
    <Router history={history}>
      <Layout className="app">
        {isAuthenticate && (
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
              {isAdmin && <AppRoute path="/auth/addUser" title={'Add a new user'} component={Signup} />}
              <AppRoute path="/deployment/new" title={'New deployment'} component={NewDeployment} />
              {!isAuthenticate && <Route path="/auth/login" component={Login} />}
              {isAuthenticate ? <Redirect to={`/auth/addUser`} /> : <Redirect to={`/auth/login`} />}
            </Switch>
          </Layout.Content>
        </Layout>
      </Layout>
    </Router>
  );
};

const mapStateToProps = ({ auth: { isAdmin, loggedIn } }) => ({ isAdmin, loggedIn });

export const App = connect(
  mapStateToProps,
  { appInit, logout },
)(AppComponent);
