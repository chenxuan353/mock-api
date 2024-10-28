package com.chenxuan353.mock.core.exception;

public class MockEngineException extends Exception {
    public MockEngineException() {
    }

    public MockEngineException(String message) {
        super(message);
    }

    public MockEngineException(String message, Throwable cause) {
        super(message, cause);
    }

    public MockEngineException(Throwable cause) {
        super(cause);
    }

    public MockEngineException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
