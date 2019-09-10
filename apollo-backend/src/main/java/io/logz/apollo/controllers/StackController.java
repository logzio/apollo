package io.logz.apollo.controllers;

import io.logz.apollo.dao.StackDao;
import io.logz.apollo.models.Stack;
import io.logz.apollo.models.StackType;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.GET;
import org.rapidoid.security.annotation.LoggedIn;

import javax.inject.Inject;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Controller
public class StackController {

    private final StackDao stackDao;

    @Inject
    public StackController(StackDao stackDao) {
        this.stackDao = requireNonNull(stackDao);
    }

    @LoggedIn
    @GET("/stack/{id}")
    public Stack getStack(int id) {
        switch(stackDao.getStackType(id)) {
            case ENVIRONMENTS:
                return stackDao.getEnvironmentsStack(id);
            case SERVICES:
                return stackDao.getServicesStack(id);
            default:
                 throw new EnumConstantNotPresentException(StackType.class, "Unknown Enum type - " + stackDao.getStackType(id));
        }
    }

    @LoggedIn
    @GET("/stack/type/{id}")
    public StackType getStackType(int id) {
        return stackDao.getStackType(id);
    }
}