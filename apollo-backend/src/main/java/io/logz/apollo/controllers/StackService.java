package io.logz.apollo.controllers;

import io.logz.apollo.dao.EnvironmentsStackDao;
import io.logz.apollo.dao.ServicesStackDao;
import io.logz.apollo.dao.StackDao;
import io.logz.apollo.models.EnvironmentsStack;
import io.logz.apollo.models.ServicesStack;
import io.logz.apollo.models.Stack;
import io.logz.apollo.models.StackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static java.util.Objects.requireNonNull;

public class StackService {

    private final StackDao stackDao;
    private final EnvironmentsStackDao environmentsStackDao;
    private final ServicesStackDao servicesStackDao;
    private static final Logger logger = LoggerFactory.getLogger(StackService.class);

    @Inject
    public StackService(StackDao stackDao, EnvironmentsStackDao environmentsStackDao, ServicesStackDao servicesStackDao) {
        this.stackDao = requireNonNull(stackDao);
        this.environmentsStackDao = requireNonNull(environmentsStackDao);
        this.servicesStackDao = requireNonNull(servicesStackDao);
    }

    public Stack getStack(int id) {
        switch(stackDao.getStackType(id)) {
            case ENVIRONMENTS:
                EnvironmentsStack environmentsStack = stackDao.getEnvironmentsStack(id);
                environmentsStack.setEnvironments(environmentsStackDao.getEnvironments(id));
                return environmentsStack;
            case SERVICES:
                ServicesStack servicesStack = stackDao.getServicesStack(id);
                servicesStack.setServices(servicesStackDao.getServices(id));
                return servicesStack;
            default:
                logger.error("Trying to create unknown stack type");
                throw new RuntimeException("Unknown stack type - " + stackDao.getStackType(id));
        }
    }

    public StackType getStackType(int id) {
        return stackDao.getStackType(id);
    }

    public EnvironmentsStack getEnvironmentsStack(int id) {
        return (EnvironmentsStack) getStack(id);
    }

    public ServicesStack getServicesStack(int id) {
        return (ServicesStack) getStack(id);
    }
}