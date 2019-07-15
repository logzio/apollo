import React from 'react';
import { PageHeader } from 'antd';
import './Container.css';



const Container = ({title, content, routes}) => (

    <div className="container">
        <div className="container-title">
            {title}
        </div>
        <PageHeader className="container-breadcrumbs" breadcrumb={{ routes }} />
        <div className="container-content">
            {content}
        </div>
    </div>
);

export default Container;
