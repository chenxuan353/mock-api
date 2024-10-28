package com.chenxuan353.mock.core.exception;

public class MockEngineScriptException extends MockEngineException {
    public MockEngineScriptException() {
    }

    public MockEngineScriptException(String message) {
        super(message);
    }

    public MockEngineScriptException(String message, Throwable cause) {
        super(message, cause);
    }

    public MockEngineScriptException(Throwable cause) {
        super(cause);
    }

    public MockEngineScriptException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
