import React, { useEffect, useState } from 'react';
import { Router, Route } from 'react-router-dom';
import history from '../../utils/history';
import { connect } from 'react-redux';
import { appInit, logout } from '../../store/actions/authActions';
import { Signup } from '../auth/Signup';
import { Login } from '../auth/Login';
import { Switch, Redirect } from 'react-router-dom';
import { Layout } from 'antd';
import { Navbar } from './Navbar';
import './App.css';
import { Container } from './Container';
import { NewDeployment } from '../deployment/new/NewDeployment';
import { getAuthToken } from '../../api/api';


const AppComponent = ({ loggedIn, appInit, logout, isAdmin }) => {
  useEffect(() => {
    appInit();
  }, [appInit]);

  const [collapsed, toggleCollapse] = useState(false);

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
              {loggedIn && isAdmin && (
                <Route
                  path="/auth/addUser"
                  render={({ match, ...props }) => (
                    <Container title={'Add a new user'} component={Signup} match={match} {...props} />
                  )}
                />
              )}
              {loggedIn && <Route path="/deployment/new" title={'New deployment'} component={NewDeployment} />}
              {!loggedIn && <Route path="/auth/login" component={Login} />}
              {loggedIn ? <Redirect to={'/auth/addUser'} /> : <Redirect to={'/auth/login'} />}


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
