package io.logz.apollo.services;

import io.logz.apollo.configuration.ApolloConfiguration;
import io.logz.apollo.configuration.CancelDeploymentConfiguration;
import io.logz.apollo.models.Deployment;

import javax.inject.Inject;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

public class CancelDeploymentService {
    private final CancelDeploymentConfiguration cancelDeploymentConfiguration;

    @Inject
    public CancelDeploymentService(ApolloConfiguration apolloConfiguration) {
        cancelDeploymentConfiguration = requireNonNull(apolloConfiguration).getCancelDeployment();
    }

    public boolean isDeploymentHasExpired(Deployment deployment) {
        return getTimeDiffFromNowInMinutes(deployment.getStartedAt()) >= cancelDeploymentConfiguration.getTimeout();
    }

    private long getTimeDiffFromNowInMinutes(Date date) {
        return cancelDeploymentConfiguration.getTimeUnit()
                .convert(Duration.between(date.toInstant(),
                        new Date().toInstant())
                                 .getSeconds(), TimeUnit.SECONDS);
    }

}
