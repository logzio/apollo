package io.logz.apollo.controllers;

import io.logz.apollo.dao.StackDao;
import io.logz.apollo.models.Stack;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.GET;
import org.rapidoid.security.annotation.LoggedIn;

import javax.inject.Inject;
import javax.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Controller
public class GenericStackController {
    private final StackDao stackDao;

    @Inject
    public GenericStackController(StackDao stackDao) {
        this.stackDao = requireNonNull(stackDao);
    }

    @LoggedIn
    @Transactional
    @GET("/stack")
    public List<Stack> getAllStacks() {
        List<Stack> allStacks = new ArrayList<>();
        allStacks.addAll(stackDao.getAllServicesStacks());
        allStacks.addAll(stackDao.getAllEnvironmentsStacks());
        return allStacks;
    }
}