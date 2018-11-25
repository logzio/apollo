package io.logz.apollo.dao;

import io.logz.apollo.models.Service;

import java.util.List;

/**
 * Created by roiravhon on 12/20/16.
 */
public interface ServiceDao {

    Service getService(int id);
    List<Service> getAllServices();
    void addService(Service service);
    void updateService(Service service);
}
