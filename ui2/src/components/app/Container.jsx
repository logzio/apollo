import React from 'react';
import { PageHeader, Breadcrumb } from 'antd';
import './Container.css';


//TODO: breadcrumbs
const Container = ({title, content, match}) => {

        const routes = [
                {
                        path: ``,
                        breadcrumbName: 'First-level Menu',
                },
                {
                        path: 'first',
                        breadcrumbName: 'Second-level Menu',
                },
                {
                        path: 'second',
                        breadcrumbName: 'Third-level Menu',
                },
        ];


        const setBreadcrumbs = () => {

        };

        return(
                <div className="container">
                        <div className="container-title">
                                {title}
                        </div>
                        <PageHeader className="container-breadcrumbs" breadcrumb={{routes}}/>
                        <div className="container-content">
                                {content}
                        </div>
                </div>
        );
};

export default Container;
