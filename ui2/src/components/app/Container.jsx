import React, {useState} from 'react';
import {Breadcrumb} from 'antd';
import {Link} from 'react-router-dom';
import './Container.css';


const Container = ({title, component: Component, match, ...props}) => {

    const routes = [
        {path: '/home', title: 'Home'},
        {path: `${match.path}`, title: title}
    ];

    const [breadcrumbs, setBreadcrumb] = useState(routes);  //temp setBreadcrumb wasn't used

    return (
        <div className="container">
            <div className="container-title large-title">
                {title}
            </div>
            <div className="container-breadcrumbs">
                <Breadcrumb>
                    {breadcrumbs.map((breadcrumb, index) => (
                        <Breadcrumb.Item key={index}  className="container-breadcrumb">
                            <Link to={breadcrumb.path}>
                                {breadcrumb.title}
                            </Link>
                        </Breadcrumb.Item>
                    ))}
                </Breadcrumb>
            </div>
            <div className="container-content">
                <Component setBreadcrumb={setBreadcrumb} {...props}/>
            </div>
        </div>
    );
};

export default Container;
