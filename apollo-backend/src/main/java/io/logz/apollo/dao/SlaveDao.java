package io.logz.apollo.dao;

import io.logz.apollo.models.Slave;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface SlaveDao {

    List<Slave> getAllSlaves();
    void keepalive(@Param("slaveId") String slaveId, @Param("lastKeepalive") Date lastKeepalive);
    void addSlave(Slave slave);
    void removeAllSlavesById(String slaveId);
    void removeEnvironmentFromSlave(@Param("slaveId") String slaveId, @Param("environmentId") int environmentId);
}
