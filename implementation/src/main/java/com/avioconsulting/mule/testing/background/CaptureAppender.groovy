package com.avioconsulting.mule.testing.background

import org.apache.logging.log4j.core.Appender
import org.apache.logging.log4j.core.ErrorHandler
import org.apache.logging.log4j.core.Layout
import org.apache.logging.log4j.core.LogEvent

class CaptureAppender implements Appender {
    public static final String TEST_APPENDER = 'test_appender'
    private final List<Map> capturedEvents = []
    private ErrorHandler errorHandler

    List<Map> getAllLogEvents() {
        synchronized (capturedEvents) {
            def copy = new ArrayList<Map>(capturedEvents)
            capturedEvents.clear()
            return copy
        }
    }

    @Override
    void append(LogEvent logEvent) {
        def eventMap = [
                level  : logEvent.level.name(),
                message: logEvent.message.formattedMessage,
                logger : logEvent.loggerName
        ]
        synchronized (capturedEvents) {
            capturedEvents << eventMap
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
        State.STARTED
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
