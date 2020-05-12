package io.logz.apollo.common;

public class EnvironmentVariableGetter {

    public static String getEnvVarOrProperty(String name) {
        String envVar = System.getenv(name);
        if (envVar != null) {
            return envVar;
        } else {
            return System.getProperty(name);
        }
    }
}
