import React from 'react';
import { Breadcrumb } from 'antd';
import './Header.css';

// const Header = ({toggleCollapsed, collapsed}) => (
//
//     <div className="header">
//         <div className="header-content">
//             <Button type="primary" onClick={()=>toggleCollapsed()}>
//                 <Icon type={collapsed ? 'menu-unfold' : 'menu-fold'} />
//             </Button>
//             <Button type="primary" shape="round" icon="logout" size='large'>Logout</Button>
//         </div>
//     </div>
// );
//
// export default Header;


const Header = ({title, breadcrumb}) => (

    <div className="header">
        <div className="header-content">
        </div>
    </div>
);

export default Header;
