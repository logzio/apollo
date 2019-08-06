import React, { useEffect, useState } from 'react';
import { Router, Route, Switch, Redirect } from 'react-router-dom';
import { historyBrowser } from '../../utils/history';
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
  const isSession = !!getAuthToken();
  const AppRoute = ({ path, ...props }) => {
    return isSession ? (
      <Route path={path} render={({ match }) => <Container match={match} {...props} />} />
    ) : (
      <Redirect to="/auth/login" />
    );
  };

  return (
    <Router history={historyBrowser}>
      <Layout className="app">
        {isSession && (
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
              {!isSession && <Route path="/auth/login" component={Login} />}
              {isSession ? <Redirect to={`/auth/addUser`} /> : <Redirect to={`/auth/login`} />}
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
