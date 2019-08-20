import React, { useEffect, useState } from 'react';
import { Router, Switch, Redirect } from 'react-router-dom';
import { historyBrowser } from '../../utils/history';
import { connect } from 'react-redux';
import { appInit, logout } from '../../store/actions/authActions';
import { Signup } from '../auth/Signup';
import { Login } from '../auth/Login';
import { Layout } from 'antd';
import { Navbar } from './Navbar';
import { NewDeployment } from '../deployment/new/NewDeployment';
import { OngoingDeployment } from '../deployment/ongoing/OngoingDeployment';
import { PrivateRoute, PublicRoute } from '../../utils/routes';
import './App.css';

const AppComponent = ({ appInit, logout, isAdmin, loggedIn }) => {
  const [collapsed, toggleCollapse] = useState(false);

  useEffect(() => {
    appInit();
  }, []);

<<<<<<< HEAD
  // const isSession = !!getAuthToken();
  // const AppRoute = ({ path, ...props }) => {
  //   return isSession ? (
  //     <Route path={path} render={({ match }) => <Container match={match} {...props} />} />
  //   ) : (
  //     <Redirect to="/auth/login" />
  //   );
  // };

=======
>>>>>>> 21fb84b4660cf6c09a558a25820ea90d74c9772e
  return (
    <Router history={historyBrowser}>
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
        <Layout.Content className="app-content">
          <Switch>
            {isAdmin && <PrivateRoute path="/auth/addUser" title={'Add a new user'} component={Signup} />}
            <PrivateRoute path="/deployment/new" title={'New deployment'} component={NewDeployment} />
            <PrivateRoute path="/deployment/ongoing" title={'Ongoing deployment'} component={OngoingDeployment} />
            <PublicRoute path="/auth/login" component={Login} />
            <Redirect to={`/auth/addUser`} />
          </Switch>
        </Layout.Content>
      </Layout>
    </Router>
  );
};

const mapStateToProps = ({ auth: { isAdmin, loggedIn } }) => ({ isAdmin, loggedIn });

export const App = connect(
  mapStateToProps,
  { appInit, logout },
)(AppComponent);
