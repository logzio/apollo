import {Menu, Icon} from 'antd';
import Logo from '../../assets/images/apollo-logo.svg';
import Symbol from '../../assets/images/apollo-symbol.svg';
import React from 'react';
import 'antd/dist/antd.css';
import './Navbar.css';

const Navbar = ({collapsed}) => (

    <div className="navbar">
        <Menu
            className="navbar-menu"
            defaultSelectedKeys={['1']}
            mode="inline"
            theme="light"
            inlineCollapsed={collapsed}
        >
            <div>
                {collapsed ?
                    <img src={Symbol}/>
                :
                    <img src={Logo}/>
                }
            </div>
            <Menu.Item key="1">
                <Icon type="pie-chart" />
                <span>Option 1</span>
            </Menu.Item>
            <Menu.Item key="2">
                <Icon type="desktop" />
                <span>Option 2</span>
            </Menu.Item>
            <Menu.Item key="3">
                <Icon type="inbox" />
                <span>Option 3</span>
            </Menu.Item>
        </Menu>
    </div>
);

export default Navbar;
