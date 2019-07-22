import {Menu, Icon} from 'antd';
import Logo from '../../assets/images/apollo-logo.svg';
import Symbol from '../../assets/images/apollo-symbol.svg';
import React from 'react';
import {Link} from 'react-router-dom';
import './Navbar.css';

const Navbar = ({toggleCollapsed, collapsed, handleLogout, isAdmin}) => {

    const navItems = [
        {title: 'New Deployment', iconType: 'edit', path: '/deployment/new', isAdmin: false},
        {title: 'Ongoing Deployment', iconType: 'unordered-list', path: '/deployment/ongoing', isAdmin: false},
        {title: 'Deployment History', iconType: 'history', path: '/deployment/history', isAdmin: false},
        {title: 'Service Status', iconType: 'eye', path: '/service/status', isAdmin: false},
        {title: 'Configure Service', iconType: 'setting', path: '/configure/service', isAdmin: false},
        {title: 'Configure Blocker', iconType: 'stop', path: '/configure/block', isAdmin: false},
        {title: 'Configure Groups', iconType: 'cluster', path: '/configure/group', isAdmin: false},
        {title: 'Add User', iconType: 'user-add', path: '/auth/addUser', isAdmin: true}
    ];

    return (
        <Menu
            className="navbar-menu"
            defaultSelectedKeys={['7']} //temp until we have a home screen
            mode="inline"
            theme="dark"
        >
            <div className="menu-header">
                {collapsed ?
                    <img className="symbol" src={Symbol} alt="Apollo logo"/>
                    :
                    <img className="logo" src={Logo} alt="Apollo logo"/>
                }
            </div>
            {navItems.map((navItem, index) => (
                (!navItem.isAdmin || navItem.isAdmin && navItem.isAdmin === isAdmin) &&
                <Menu.Item key={index}>
                    <Link to={navItem.path}>
                        <Icon type={navItem.iconType}/>
                        <span>{navItem.title}</span>
                    </Link>
                </Menu.Item>

            ))}
            <Menu.Item
                className="menu-footer"
                id="menu-footer-test"
                onClick={() => handleLogout()}
            >
                <Icon type="logout"/>
                <span>Logout</span>
            </Menu.Item>
            <Menu.Item
                onClick={() => toggleCollapsed()}
            >
                <Icon type={collapsed ? 'menu-unfold' : 'menu-fold'}/>
                <span>Collapse</span>
            </Menu.Item>
        </Menu>
    );
};

export default Navbar;
