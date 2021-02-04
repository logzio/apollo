package io.logz.apollo.helpers;

import com.google.common.base.Stopwatch;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.output.WaitingConsumer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;

public class ApolloConsul {
    private static final Logger logger = LoggerFactory.getLogger(ApolloConsul.class);

    private GenericContainer standaloneConsul;
    private int consulPort;
    private KeyValueClient client;

    public ApolloConsul start() {
        logger.info("Starting standalone consul");
        Stopwatch stopwatch = Stopwatch.createStarted();


        await().atMost(60, TimeUnit.SECONDS).pollInterval(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    try {
                        standaloneConsul = new GenericContainer("consul:v0.6.4")
                                .withCommand("agent -dev -client 0.0.0.0")
                                .withLogConsumer(new Slf4jLogConsumer(logger).withPrefix("consul"))
                                .withExposedPorts(8500);
                    } catch (AssertionError e) {
                        logger.info("Failed to raise standalone consul, retrying. Error: ", e);
                        throw e;
                    }
                });

        standaloneConsul.start();
        consulPort = standaloneConsul.getMappedPort(8500);


        WaitingConsumer waitingConsumer = new WaitingConsumer();
        standaloneConsul.followOutput(waitingConsumer, OutputFrame.OutputType.STDOUT, OutputFrame.OutputType.STDERR);

        try {
            waitingConsumer.waitUntil(f -> f.getUtf8String() != null && f.getUtf8String().contains("agent: Synced service 'consul'"), 60, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            fail("Waited too long for Standalone Consul to start");
        }

        client = getClient();

        logger.info("Standalone consul started successfully. up time: {} seconds, consul port: {}", stopwatch.elapsed(TimeUnit.SECONDS), consulPort);
        return this;
    }

    public void stop() {
        if (standaloneConsul != null) standaloneConsul.stop();
    }

    public int getHttpPort() {
        return consulPort;
    }

    public String getConnectString() {
        return String.format("localhost:%d", getHttpPort());
    }

    public String getURL() { return "http://" + getConnectString(); }

    public KeyValueClient getClient() {
        Consul consul = Consul.builder().withUrl(getURL()).build();

        return consul.keyValueClient();
    }

    public void putValue(String consulKeyPath, String key, String value) {
        client.putValue(consulKeyPath, createKeyWithValue(key, value));
    }

    private String createKeyWithValue(String key, String value) {
        requireNonNull(key);
        requireNonNull(value);
        return key + " = " + value + "\n";
    }
}

