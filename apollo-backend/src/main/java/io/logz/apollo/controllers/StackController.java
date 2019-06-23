package io.logz.apollo.controllers;

import io.logz.apollo.dao.StackDao;
import io.logz.apollo.models.Stack;
import io.logz.apollo.models.StackType;
import org.rapidoid.security.annotation.LoggedIn;

import javax.inject.Inject;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class StackController {

    private final StackDao stackDao;

    @Inject
    public StackController(StackDao stackDao) {
        this.stackDao = requireNonNull(stackDao);
    }

    @LoggedIn
    public Stack getStack(int stackId) {
        switch(stackDao.getStackType(stackId)) {
            case ENVIRONMENTS:
                return stackDao.getEnvironmentsStack(stackId);
            case SERVICES:
                return stackDao.getServicesStack(stackId);
            default:
                 throw new EnumConstantNotPresentException(StackType.class, "Unknown Enum type - " + stackDao.getStackType(stackId));
        }
    }

    @LoggedIn
    public StackType getStackType(int stackId) {
        return stackDao.getStackType(stackId);
    }
}