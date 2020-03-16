package io.logz.apollo.dao;

import io.logz.apollo.models.Slave;

import java.util.List;

public interface SlaveDao {

    List<Slave> getAllSlaves();
    void keepalive(String slaveId);
    void addSlave(Slave slave);
    void removeAllSlavesById(String slaveId);
}
