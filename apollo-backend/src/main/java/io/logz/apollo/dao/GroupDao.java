package io.logz.apollo.dao;

import io.logz.apollo.models.Group;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GroupDao {
    Group getGroup(int id);
    Group getGroupByName(String name);
    List<Group> getAllGroups();
    List<Group> getGroupsPerServiceAndEnvironment(@Param("serviceId") int serviceId, @Param("environmentId") int environmentId);
    List<Group> getAllRunningScalingOperations();
    int getScalingFactor(int id);
    void addGroup(Group group);
    void updateGroup(Group group);
    void updateGroupScalingStatus(@Param("id") int id, @Param("scalingStatus") Group.ScalingStatus scalingStatus);
    void deleteGroup(int id);
    void updateScalingFactor(Group group);
}
