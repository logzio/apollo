package io.logz.apollo.dao;

import io.logz.apollo.models.Slave;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface SlaveDao {

    List<Slave> getAllSlaves();
    void keepalive(String slaveId);
    void addSlave(Slave slave);
    void removeAllSlavesById(String slaveId);
    void cleanupDeadSlaves(@Param("oldestValidKeepAlive") int oldestValidKeepalive);
}
