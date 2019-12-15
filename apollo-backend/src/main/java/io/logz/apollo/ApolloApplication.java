package io.logz.apollo;

import com.google.inject.Injector;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;
import io.logz.apollo.configuration.ApolloConfiguration;
import io.logz.apollo.configuration.ApolloConfigurationProviderBuilder;
import io.logz.apollo.di.ApolloModule;
import io.logz.apollo.di.ApolloMyBatisModule;
import org.conf4j.core.ConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

import static java.util.Objects.requireNonNull;

public class ApolloApplication {

    private static final Logger logger = LoggerFactory.getLogger(ApolloApplication.class);

    private final ConfigurationProvider<ApolloConfiguration> configurationProvider;
    private LifecycleManager lifecycleManager;
    private Injector injector;

    public ApolloApplication(ConfigurationProvider<ApolloConfiguration> configurationProvider) {
        this.configurationProvider = requireNonNull(configurationProvider);
    }

    public void start() {
        try {
            logger.info("Starting apollo..");

            injector = LifecycleInjector.builder().withModules(
                    new ApolloModule(configurationProvider.get()),
                    new ApolloMyBatisModule(configurationProvider.get().getDatabase())
            ).build().createInjector();

            lifecycleManager = injector.getInstance(LifecycleManager.class);
            lifecycleManager.start();

            logger.info("Apollo started");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        try {
            logger.info("Cleaning up..");
            if (lifecycleManager != null) lifecycleManager.close();
            if (configurationProvider != null) configurationProvider.close();
        } catch (Exception e) {
            logger.warn("Unknown exception thrown while shutting down apollo", e);
            throw new RuntimeException(e);
        }
    }

    public Injector getInjector() {
        return injector;
    }

    public static void main(String[] args) {
        ConfigurationProvider<ApolloConfiguration> configurationProvider = ApolloConfigurationProviderBuilder.build();
        ApolloApplication application = new ApolloApplication(configurationProvider);

        try {
            Runtime.getRuntime().addShutdownHook(new Thread(application::shutdown));

            application.start();
            new CountDownLatch(1).await();
        } catch (Exception e) {
            if (e instanceof InterruptedException) return;

            logger.error(e.getMessage(), e);
            application.shutdown();
            System.exit(1);
        }
    }

}
