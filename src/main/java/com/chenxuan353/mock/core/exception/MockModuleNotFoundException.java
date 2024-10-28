package com.chenxuan353.mock.core.exception;

public class MockModuleNotFoundException extends RuntimeException {
    public MockModuleNotFoundException() {
    }

    public MockModuleNotFoundException(String message) {
        super(message);
    }
}
