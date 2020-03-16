package io.logz.apollo;

import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.dao.EnvironmentDao;
import io.logz.apollo.exceptions.ApolloClientException;
import io.logz.apollo.helpers.Common;
import io.logz.apollo.helpers.ModelsGenerator;
import io.logz.apollo.helpers.StandaloneApollo;
import io.logz.apollo.kubernetes.KubernetesMonitor;
import io.logz.apollo.models.Environment;
import io.logz.apollo.services.SlaveService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

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

        List<Integer> allEnvironmentIds = environmentDao.getAllEnvironments().stream().map(Environment::getId).collect(Collectors.toList());

        List<Integer> masterScopedEnvironments = standaloneApollo.getInstance(KubernetesMonitor.class).getScopedEnvironments();
        assertThat(getCsvFromList(allEnvironmentIds)).isEqualTo(getCsvFromList(masterScopedEnvironments));

        ApolloApplication slave = standaloneApollo.createAndStartSlave(Arrays.asList(slaveEnvironment1.getId(), slaveEnvironment2.getId()));
        waitUntilSlaveStarted(slave);

        masterScopedEnvironments = standaloneApollo.getInstance(KubernetesMonitor.class).getScopedEnvironments();
        assertThat(masterScopedEnvironments).contains(masterEnvironment1.getId(), masterEnvironment2.getId());
        assertThat(masterScopedEnvironments).doesNotContain(slaveEnvironment1.getId(), slaveEnvironment2.getId());

        List<Integer> slaveScopedEnvironments = slave.getInjector().getInstance(KubernetesMonitor.class).getScopedEnvironments();
        assertThat(slaveScopedEnvironments).containsOnly(slaveEnvironment1.getId(), slaveEnvironment2.getId());
    }

    private void waitUntilSlaveStarted(ApolloApplication apolloApplication) {
        SlaveService slaveService = apolloApplication.getInjector().getInstance(SlaveService.class);
        int retry = 10;
        for(int i = 0; i <= retry; i++) {
            if (slaveService.isStarted()) {
                logger.info("Slave is up!");
                return;
            }

            logger.info("Slave not up yet, waiting a sec. ({}/{})", i, retry);
            Common.waitABit(1);
        }

        throw new  RuntimeException("Reached max retries waiting for slave to be up!");
    }

    private String getCsvFromList(List<Integer> list) {
        return list.stream().map(Object::toString).collect(Collectors.joining(","));
    }
}
