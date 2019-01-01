package io.logz.apollo.dao;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ServicesStackDao {
    List<Integer> getServices(int stackId);
    void addServiceToStack(@Param("serviceId") int serviceId, @Param("stackId") int stackId);
    void removeServiceFromStack(@Param("serviceId") int serviceId, @Param("stackId") int stackId);
    void addServicesToStack(@Param("services") List<Integer> services, @Param("stackId") int stackId);
    void clearServicesStack(int stackId);
}