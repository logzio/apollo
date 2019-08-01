import React from 'react';
import { connect } from 'react-redux';
import { Switch, Redirect, Route } from 'react-router-dom';
import { getServices, getServicesStack, selectServices } from '../deploymentActions';
import SelectService from './SelectService';
import SelectEnv from './SelectEnv'; //temp placeholder
import SelectGrourp from './SelectGroup'; //temp placeholder

const NewDeploymentComponent = ({
  handleBreadcrumbs,
  getServices,
  services,
  getServicesStack,
  match,
  servicesStacks,
  selectServices,
}) => {
  return (
    <Switch>
      <Route
        path={`${match.url}/service`}
        render={({ match }) => (
          <SelectService
            getServices={getServices}
            services={services}
            getServicesStack={getServicesStack}
            servicesStacks={servicesStacks}
            handleBreadcrumbs={handleBreadcrumbs}
            match={match}
            selectServices={selectServices}
          />
        )}
      />
      <Route
        path={`${match.url}/environment`}
        render={({ match }) => <SelectEnv handleBreadcrumbs={handleBreadcrumbs} match={match} />}
      />
      <Route
        path={`${match.url}/group`}
        render={({ match }) => <SelectGrourp handleBreadcrumbs={handleBreadcrumbs} match={match} />}
      />
      <Redirect to={`${match.url}/service`} />
    </Switch>
  );
};

const mapStateToProps = state => {
  const { deploy } = state;
  return {
    services: deploy.services,
    isLoading: deploy.isLoading,
    servicesStacks: deploy.servicesStacks,
    selectedServices: deploy.selectedServices,
  };
};

const NewDeployment = connect(
  mapStateToProps,
  { getServices, getServicesStack, selectServices },
)(NewDeploymentComponent);

export default NewDeployment;
