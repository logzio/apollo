package io.logz.apollo;

import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.exceptions.ApolloClientException;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.ModelsGenerator;
import io.logz.apollo.helpers.StandaloneApollo;
import io.logz.apollo.models.Environment;
import io.logz.apollo.services.SlaveService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class SlaveTest {

    private static final Logger logger = LoggerFactory.getLogger(SlaveTest.class);
    private final StandaloneApollo standaloneApollo;

    public SlaveTest() throws ScriptException, IOException, SQLException {
        this.standaloneApollo = StandaloneApollo.getOrCreateServer();
    }

    @Test
    public void testSlaveOwnership() throws ApolloClientException {
        ApolloTestClient apolloTestClient = Common.signupAndLogin();
        EnvironmentDao environmentDao = standaloneApollo.getInstance(EnvironmentDao.class);

        Environment slaveEnvironment1 = ModelsGenerator.createAndSubmitEnvironment(apolloTestClient);
        Environment slaveEnvironment2 = ModelsGenerator.createAndSubmitEnvironment(apolloTestClient);
        Environment masterEnvironment1 = ModelsGenerator.createAndSubmitEnvironment(apolloTestClient);
        Environment masterEnvironment2 = ModelsGenerator.createAndSubmitEnvironment(apolloTestClient);

        List<Integer> allEnvironmentIds =  apolloTestClient.getAllEnvironments().stream().map(Environment::getId).collect(Collectors.toList());
        Set<Integer> masterScopedEnvironments = standaloneApollo.getInstance(SlaveService.class).getScopedEnvironments();
        assertThat(getCsvFromCollection(allEnvironmentIds)).isEqualTo(getCsvFromCollection(masterScopedEnvironments));

        ApolloApplication slave = standaloneApollo.createAndStartSlave("tahat123", Arrays.asList(slaveEnvironment1.getId(), slaveEnvironment2.getId()), true);
        waitUntilSlaveStarted(slave);

        masterScopedEnvironments = standaloneApollo.getInstance(SlaveService.class).getScopedEnvironments();
        assertThat(masterScopedEnvironments).contains(masterEnvironment1.getId(), masterEnvironment2.getId());
        assertThat(masterScopedEnvironments).doesNotContain(slaveEnvironment1.getId(), slaveEnvironment2.getId());

        Set<Integer> slaveScopedEnvironments = slave.getInjector().getInstance(SlaveService.class).getScopedEnvironments();
        assertThat(slaveScopedEnvironments).containsOnly(slaveEnvironment1.getId(), slaveEnvironment2.getId());
    }

    private void waitUntilSlaveStarted(ApolloApplication apolloApplication) {
        SlaveService slaveService = apolloApplication.getInjector().getInstance(SlaveService.class);

        await("Waiting to slave to start")
                .pollInterval(1, SECONDS)
                .atMost(10, SECONDS)
                .until(slaveService::getIsStarted);

        logger.info("Slave is up!");
    }

    private String getCsvFromCollection(Collection<Integer> list) {
        return list.stream().map(Object::toString).collect(Collectors.joining(","));
    }
}
