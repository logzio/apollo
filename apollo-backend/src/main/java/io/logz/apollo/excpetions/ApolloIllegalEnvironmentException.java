package io.logz.apollo.excpetions;

public class ApolloIllegalEnvironmentException extends ApolloDeploymentException {

    public ApolloIllegalEnvironmentException(String message) {
        super(message);
    }

    public ApolloIllegalEnvironmentException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApolloIllegalEnvironmentException(Throwable cause) {
        super(cause);
    }

    public ApolloIllegalEnvironmentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ApolloIllegalEnvironmentException() {
    }
}
