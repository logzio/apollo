import {Menu, Icon} from 'antd';
import Logo from '../../assets/images/apollo-logo.svg';
import Symbol from '../../assets/images/apollo-symbol.svg';
import React from 'react';
import 'antd/dist/antd.css';
import './Navbar.css';

const Navbar = ({toggleCollapsed, collapsed}) => {

    const navItems = [
        {title: 'New Deployment', iconType: 'edit'},
        {title: 'Ongoing Deployment', iconType: 'unordered-list'},
        {title: 'Deployment History', iconType: 'history'},
    ];

    return(
        <Menu
            className="navbar-menu"
            defaultSelectedKeys={['0']}
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
            {navItems.map((navItem, index)=>(
                <Menu.Item key={index}>
                <Icon type={navItem.iconType} />
                <span>{navItem.title}</span>
            </Menu.Item>
            ))}
            <Menu.Item
                className="menu-footer"
                id="menu-footer-test"
            >
                <Icon type="logout" />
                <span>Logout</span>
            </Menu.Item>
            <Menu.Item

                onClick={()=>toggleCollapsed()}
            >
                <Icon type={collapsed ? 'menu-unfold' : 'menu-fold'} />
                <span>Collapse</span>
            </Menu.Item>
        </Menu>
    );
};

export default Navbar;
