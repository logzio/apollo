package io.logz.apollo.dao;

import io.logz.apollo.models.Service;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ServicesStackDao {

    List<Service> getServices(int id);
    String getStackName(int id);
    int getStackIdByName(String name);
    boolean isStackEnabled(int id);
    void addServicesStack(@Param("name") String name, @Param("isEnabled") boolean isEnabled);
    void addServiceToStack(@Param("serviceId") int serviceId, @Param("stackId") int stackId);
}