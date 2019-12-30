package io.logz.apollo.di;

import com.google.inject.AbstractModule;
import io.logz.apollo.ApolloApplication;
import io.logz.apollo.blockers.BlockerService;
import io.logz.apollo.configuration.ApolloConfiguration;
import io.logz.apollo.kubernetes.KubernetesMonitor;
import io.logz.apollo.notifications.ApolloNotifications;
import io.logz.apollo.rest.RestServer;
import io.logz.apollo.websockets.WebSocketServer;
import io.logz.apollo.websockets.exec.AuthenticationService;
import org.rapidoid.annotation.Controller;
import org.reflections.Reflections;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public class ApolloModule extends AbstractModule {

    private final ApolloConfiguration configuration;

    public ApolloModule(ApolloConfiguration configuration) {
        this.configuration = requireNonNull(configuration);
    }

    @Override
    protected void configure() {
        bind(ApolloConfiguration.class).toInstance(configuration);
        bind(KubernetesMonitor.class).asEagerSingleton();
        bind(WebSocketServer.class).asEagerSingleton();
        bind(RestServer.class).asEagerSingleton();
        bind(BlockerService.class).asEagerSingleton();
        bind(AuthenticationService.class).asEagerSingleton();
        bind(ApolloNotifications.class).asEagerSingleton();

        bindControllers();
    }

    private void bindControllers() {
        Reflections reflections = new Reflections(ApolloApplication.class.getPackage().getName());
        Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(Controller.class);
        controllers.forEach(this::bindAsEagerSingleton);
    }

    private void bindAsEagerSingleton(Class<?> clazz) {
        bind(clazz).asEagerSingleton();
    }

}
