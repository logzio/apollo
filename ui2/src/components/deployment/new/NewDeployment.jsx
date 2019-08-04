import React from 'react';
import { connect } from 'react-redux';
import { Switch, Redirect, Route } from 'react-router-dom';
import {
  getServices,
  getServicesStack,
  selectServices,
  getEnvironment,
  getEnvironmentsStack,
} from '../deploymentActions';
import SelectService from './SelectService';
import SelectEnv from './SelectEnv';
import SelectGrourp from './SelectGroup'; //temp placeholder

const NewDeploymentComponent = ({
  handleBreadcrumbs,
  getServices,
  services,
  getServicesStack,
  match,
  servicesStacks,
  selectServices,
  getEnvironment,
  getEnvironmentsStack,
  environment,
  environmentsStacks,
}) => {
  return (
    <Switch>
      <Route
        path={`${match.url}/service${window.location.search}`}
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
        render={({ match }) => (
          <SelectEnv
            handleBreadcrumbs={handleBreadcrumbs}
            match={match}
            getEnvironment={getEnvironment}
            getEnvironmentsStack={getEnvironmentsStack}
            environment={environment}
            environmentsStacks={environmentsStacks}
          />
        )}
      />
      <Route
        path={`${match.url}/group`}
        render={({ match }) => <SelectGrourp handleBreadcrumbs={handleBreadcrumbs} match={match} />}
      />
      {environment ? (
        <Redirect to={`${match.url}/environment${window.location.search}`} />
      ) : (
        <Redirect to={`${match.url}/service`} />
      )}
    </Switch>
  );
};

const mapStateToProps = state => {
  const {
    deploy: { services, isLoading, servicesStacks, selectedServices, environment, environmentsStacks },
  } = state;
  return {
    services,
    isLoading,
    servicesStacks,
    selectedServices,
    environment,
    environmentsStacks,
  };
};

const NewDeployment = connect(
  mapStateToProps,
  { getServices, getServicesStack, selectServices, getEnvironment, getEnvironmentsStack },
)(NewDeploymentComponent);

export default NewDeployment;
