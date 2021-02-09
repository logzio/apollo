package io.logz.apollo.excpetions;

public class ApolloDeploymentDoesntExistException extends ApolloDeploymentException {
    public ApolloDeploymentDoesntExistException(String message) {
        super(message);
    }

    public ApolloDeploymentDoesntExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApolloDeploymentDoesntExistException(Throwable cause) {
        super(cause);
    }

    public ApolloDeploymentDoesntExistException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ApolloDeploymentDoesntExistException() {
    }
}
