package com.avioconsulting.mule.testing

class TestingFrameworkException extends Exception {
    TestingFrameworkException(String message) {
        super(message)
    }

    TestingFrameworkException(String message,
                              Throwable cause) {
        super(message,
              cause)
    }
}
