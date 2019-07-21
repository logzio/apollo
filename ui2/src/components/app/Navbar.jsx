import {Menu, Icon} from 'antd';
import Logo from '../../assets/images/apollo-logo.svg';
import Symbol from '../../assets/images/apollo-symbol.svg';
import React from 'react';
import {Link} from 'react-router-dom';
import 'antd/dist/antd.css';
import './Navbar.css';

const Navbar = ({toggleCollapsed, collapsed}) => {

    const navItems = [
        {title: 'New Deployment', iconType: 'edit', path: '/deployment/new'},
        {title: 'Ongoing Deployment', iconType: 'unordered-list', path: '/deployment/ongoing'},
        {title: 'Deployment History', iconType: 'history', path: '/deployment/history'},
        {title: 'Service Status', iconType: 'eye', path: '/service/status'},
        {title: 'Configure Service', iconType: 'setting', path: '/configure/service'},
        {title: 'Configure Blocker', iconType: 'stop', path: '/configure/block'},
        {title: 'Configure Groups', iconType: 'cluster', path: '/configure/group'},
        {title: 'Add User', iconType: 'user-add', path: '/auth/signup'}
    ];

    return (
        <Menu
            className="navbar-menu"
            defaultSelectedKeys={['0']}
            mode="inline"
            theme="dark"
        >
            <div className="menu-header">
                {/*<img className="logo" src={Symbol} alt="Apollo logo"/>*/}
                {/*{!collapsed && <div className="menu-title">Apollo</div>}*/}
                {collapsed ?
                    <img className="symbol" src={Symbol} alt="Apollo logo"/>
                    :
                    <img className="logo" src={Logo} alt="Apollo logo"/>
                }
            </div>
            {navItems.map((navItem, index) => (
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
