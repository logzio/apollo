package io.logz.apollo.dao;

import io.logz.apollo.models.EnvironmentsStack;
import io.logz.apollo.models.ServicesStack;
import io.logz.apollo.models.Stack;
import io.logz.apollo.models.StackType;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface StackDao {
    Stack getStack(int id);
    EnvironmentsStack getEnvironmentsStack(int id);
    ServicesStack getServicesStack(int id);
    List<Stack> getAllStacks();
    List<Stack> getAllStackFromType(@Param("stackType") StackType stackType);
    List<EnvironmentsStack> getAllEnvironmentsStacks();
    List<ServicesStack> getAllServicesStacks();
    void addStack(Stack stack);
    void updateStack(Stack stack);
    void deleteStack(int id);
}
