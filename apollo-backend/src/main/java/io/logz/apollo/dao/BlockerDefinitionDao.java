package io.logz.apollo.dao;

import io.logz.apollo.models.BlockerDefinition;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by roiravhon on 6/4/17.
 */
public interface BlockerDefinitionDao {
    BlockerDefinition getBlockerDefinition(int id);
    List<BlockerDefinition> getAllBlockerDefinitions();
    List<Integer> getUnconditionalBlockers();
    List<Integer> getUnconditionalBlockersByEnvironmentTypeAndRegion(@Param("regions") List<String> regions, @Param("environmentTypes") List<String> environmentTypes);
    List<Integer> getUnconditionalBlockersByRegion(@Param("regions") List<String> regions);
    List<Integer> getUnconditionalBlockersByEnvironmentType(@Param("environmentTypes") List<String> environmentTypes);
    List<String> getBlockersJsonConfiguration(String blockerTypeName);
    void addBlockerDefinition(BlockerDefinition blockerDefinition);
    void updateBlockerDefinition(BlockerDefinition blockerDefinition);
    void deleteBlockerDefinition(int id);
    List<Integer> getOverrideBlockersIdsByUser(String userEmail);
    void addUserToBlockerOverride(@Param("userEmail") String userEmail, @Param("blockerId") int blockerId);
    void deleteUserToBlockerOverride(@Param("userEmail") String userEmail, @Param("blockerId") int blockerId);
}
