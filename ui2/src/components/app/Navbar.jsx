import { Menu } from 'antd';
import { AppIcon } from '../../common/Icon';
import Logo from '../../assets/images/apollo-logo.svg';
import Symbol from '../../assets/images/apollo-symbol.svg';
import React from 'react';
import { Link } from 'react-router-dom';
import './Navbar.css';

export const Navbar = ({ toggleCollapsed, collapsed, handleLogout, isAdmin, currentNavBarTab }) => {
  const navItems = [
    { title: 'New Deployment', iconType: 'edit', path: '/deployment/new', isAdmin: false },
    { title: 'Ongoing Deployment', iconType: 'unordered-list', path: '/deployment/ongoing', isAdmin: false },
    { title: 'Deployment History', iconType: 'history', path: '/deployment/history', isAdmin: false },
    { title: 'Service Status', iconType: 'eye', path: '/service/status', isAdmin: false },
    { title: 'Configure Service', iconType: 'setting', path: '/configure/service', isAdmin: false },
    { title: 'Configure Blocker', iconType: 'stop', path: '/configure/block', isAdmin: false },
    { title: 'Configure Groups', iconType: 'cluster', path: '/configure/group', isAdmin: false },
    { title: 'Add User', iconType: 'user-add', path: '/auth/addUser', isAdmin: true },
  ];

  return (
    <Menu className="navbar-menu" mode="inline" theme="dark">
      <div className="menu-header">
        {collapsed ? (
          <img className="symbol" src={Symbol} alt="Apollo logo" />
        ) : (
          <img className="logo" src={Logo} alt="Apollo logo" />
        )}
      </div>
      {navItems.map(
        ({ isAdmin: isAdminNav, path, iconType, title }, index) =>
          (!isAdminNav || isAdminNav === isAdmin) && (
            <Menu.Item key={index}>
              <Link to={path}>
                <AppIcon type={iconType} />
                <span>{title}</span>
              </Link>
            </Menu.Item>
          ),
      )}
      <Menu.Item className="menu-footer" id="menu-footer-test" onClick={() => handleLogout()}>
        <AppIcon type="logout" />
        <span>Logout</span>
      </Menu.Item>
      <Menu.Item onClick={() => toggleCollapsed()}>
        <AppIcon type={collapsed ? 'menu-unfold' : 'menu-fold'} />
        <span>Collapse</span>
      </Menu.Item>
    </Menu>
  );
};
