package com.avioconsulting.mule.testing.background

import org.apache.logging.log4j.core.Appender
import org.apache.logging.log4j.core.ErrorHandler
import org.apache.logging.log4j.core.Layout
import org.apache.logging.log4j.core.LogEvent

class CaptureAppender implements Appender {
    public static final String TEST_APPENDER = 'test_appender'
    private final List<LogEvent> capturedEvents = []
    private ErrorHandler errorHandler

    List<LogEvent> getAllLogEvents() {
        synchronized (capturedEvents) {
            def copy = new ArrayList<LogEvent>(capturedEvents)
            capturedEvents.clear()
            return copy
        }
    }

    @Override
    void append(LogEvent logEvent) {
        synchronized (capturedEvents) {
            capturedEvents << logEvent
        }
    }

    @Override
    String getName() {
        TEST_APPENDER
    }

    @Override
    Layout<? extends Serializable> getLayout() {
        return null
    }

    @Override
    boolean ignoreExceptions() {
        return false
    }

    @Override
    ErrorHandler getHandler() {
        return errorHandler
    }

    @Override
    void setHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler
    }

    @Override
    State getState() {
        return null
    }

    @Override
    void initialize() {

    }

    @Override
    void start() {

    }

    @Override
    void stop() {

    }

    @Override
    boolean isStarted() {
        return true
    }

    @Override
    boolean isStopped() {
        return false
    }
}
