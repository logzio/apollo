import React, { useEffect } from 'react';
import {Link} from "react-router-dom";

const NewDeploymentComponent = ({ handleBreadcrumbs, match }) => {
  useEffect(() => {
    handleBreadcrumbs(`${match.url}`, 'env');
  }, []);

  return <div><button><Link to={'group'}>NEXT</Link></button> hi</div>;
};

export default NewDeploymentComponent;
