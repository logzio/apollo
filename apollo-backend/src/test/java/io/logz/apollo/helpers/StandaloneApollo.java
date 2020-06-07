package io.logz.apollo.helpers;

import io.logz.apollo.ApolloApplication;
import io.logz.apollo.clients.ApolloTestAdminClient;
import io.logz.apollo.clients.ApolloTestClient;
import io.logz.apollo.configuration.ApiConfiguration;
import io.logz.apollo.configuration.ApolloConfiguration;
import io.logz.apollo.configuration.DeploymentConfiguration;
import io.logz.apollo.configuration.DatabaseConfiguration;
import io.logz.apollo.configuration.KubernetesConfiguration;
import io.logz.apollo.configuration.ScmConfiguration;
import io.logz.apollo.configuration.SlaveConfiguration;
import io.logz.apollo.configuration.WebsocketConfiguration;
import io.logz.apollo.kubernetes.KubernetesMonitor;
import io.logz.apollo.kubernetes.KubernetesHealth;
import io.logz.apollo.scm.GithubConnector;
import org.apache.commons.lang3.StringUtils;
import org.conf4j.core.ConfigurationProvider;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StandaloneApollo {

    private static StandaloneApollo instance;
    private static String hostname = "localhost";
    private static String protocol = "http";
    private static final String DATA_SOURCE_CLASS_NAME = "org.mariadb.jdbc.MariaDbDataSource";

    private final ApolloApplication apolloApplication;
    private final KubernetesMonitor kubernetesMonitor;
    private final KubernetesHealth kubernetesHealth;
    private final GithubConnector githubConnector;
    private final DatabaseConfiguration databaseConfiguration;

    private ApolloConfiguration apolloConfiguration;

    private StandaloneApollo() throws ScriptException, SQLException, IOException {
        System.setProperty(KubernetesMonitor.LOCAL_RUN_PROPERTY, "true");
        System.setProperty(KubernetesHealth.LOCAL_RUN_PROPERTY, "true");

        // Start DB and match configuration
        ApolloMySQL apolloMySQL = new ApolloMySQL();
        databaseConfiguration = new DatabaseConfiguration(
                apolloMySQL.getMappedPort(),
                apolloMySQL.getContainerIpAddress(),
                apolloMySQL.getUsername(),
                apolloMySQL.getPassword(),
                apolloMySQL.getSchema(),
                DATA_SOURCE_CLASS_NAME
        );

        apolloConfiguration = createApolloConfiguration(null, false, "", false);

        // Start apollo
        apolloApplication = new ApolloApplication(createConfigurationProvider(apolloConfiguration));
        apolloApplication.start();

        // Get Kubernetes monitor and health, they are stopped by default in tests because usually will want to inject mock first
        kubernetesMonitor = apolloApplication.getInjector().getInstance(KubernetesMonitor.class);
        kubernetesHealth = apolloApplication.getInjector().getInstance(KubernetesHealth.class);
        Runtime.getRuntime().addShutdownHook(new Thread(apolloApplication::shutdown));

        githubConnector = new GithubConnector(apolloConfiguration);
    }

    @NotNull
    private ApolloConfiguration createApolloConfiguration(String slaveId, boolean isSlave, String slaveCsvEnvironments, boolean disableApiServer) {
        return new ApolloConfiguration(
                new ApiConfiguration(Common.getAvailablePort(), "0.0.0.0", "secret", disableApiServer),
                databaseConfiguration,
                new KubernetesConfiguration(1, 1),
                new ScmConfiguration(StringUtils.EMPTY, StringUtils.EMPTY),
                new WebsocketConfiguration(Common.getAvailablePort(), 5),
                new SlaveConfiguration(slaveId,1, isSlave, slaveCsvEnvironments),
                new DeploymentConfiguration(1)
        );
    }

    public static StandaloneApollo getOrCreateServer() throws ScriptException, IOException, SQLException {
        if (instance == null) {
            instance = new StandaloneApollo();
        }

        return instance;
    }

    public ApolloApplication createAndStartSlave(String slaveId, List<Integer> environmentIds, boolean disableApiServer) {
        if (instance == null) {
            throw new RuntimeException("Can't create slave without master first");
        }

        ApolloConfiguration apolloConfiguration = createApolloConfiguration(slaveId, true,
                environmentIds.stream().map(Object::toString).collect(Collectors.joining(",")), disableApiServer);

        ApolloApplication apolloApplication = new ApolloApplication(createConfigurationProvider(apolloConfiguration));
        apolloApplication.start();

        return apolloApplication;
    }

    public GithubConnector getGithubConnector() {
        return githubConnector;
    }

    public void startKubernetesMonitor() {
        System.setProperty(KubernetesMonitor.LOCAL_RUN_PROPERTY, "false");
        kubernetesMonitor.start();
    }

    public void stopKubernetesMonitor() {
        System.setProperty(KubernetesMonitor.LOCAL_RUN_PROPERTY, "true");
        kubernetesMonitor.stop();
    }

    public void startKubernetesHealth() {
        System.setProperty(KubernetesHealth.LOCAL_RUN_PROPERTY, "false");
        kubernetesHealth.start();
    }

    public void stopKubernetesHealth() {
        kubernetesHealth.stop();
        System.setProperty(KubernetesHealth.LOCAL_RUN_PROPERTY, "true");
    }

    public ApolloTestClient createTestClient() {
        return new ApolloTestClient(ModelsGenerator.createRegularUser(), hostname, apolloConfiguration.getApi().getPort(), protocol);
    }

    public <T> T getInstance(Class<T> clazz) {
        return apolloApplication.getInjector().getInstance(clazz);
    }

    public ApolloTestAdminClient createTestAdminClient() {
        return new ApolloTestAdminClient(hostname, apolloConfiguration.getApi().getPort(), protocol);
    }

    private <T> ConfigurationProvider<T> createConfigurationProvider(T configuration) {
        return new ConfigurationProvider<T>() {
            @Override
            public T get() {
                return configuration;
            }

            @Override
            public <C> ConfigurationProvider<C> createConfigurationProvider(Function<T, C> extractor) {
                return null;
            }

            @Override
            public void registerChangeListener(BiConsumer<T, T> listener) {}
        };
    }

}
