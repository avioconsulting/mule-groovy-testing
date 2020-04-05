package com.avioconsulting.mule.testing.background;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CaptureAppender implements Appender {
    public List<Map<String, String>> getAllLogEvents() {
        synchronized (capturedEvents) {
            ArrayList<Map<String, String>> copy = new ArrayList<>(capturedEvents);
            capturedEvents.clear();
            return copy;
        }
    }

    @Override
    public void append(LogEvent logEvent) {
        // we can't easily serialize this to JSON so just get the parts we care about
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(3);
        map.put("level", logEvent.getLevel().name());
        map.put("message", logEvent.getMessage().getFormattedMessage());
        map.put("logger", logEvent.getLoggerName());
        synchronized (capturedEvents) {
            capturedEvents.add(map);
        }
    }

    @Override
    public String getName() {
        return TEST_APPENDER;
    }

    @Override
    public Layout<? extends Serializable> getLayout() {
        return null;
    }

    @Override
    public boolean ignoreExceptions() {
        return false;
    }

    @Override
    public ErrorHandler getHandler() {
        return errorHandler;
    }

    @Override
    public void setHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public State getState() {
        return State.STARTED;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return true;
    }

    @Override
    public boolean isStopped() {
        return false;
    }

    public static final String TEST_APPENDER = "test_appender";
    private final List<Map<String, String>> capturedEvents = new ArrayList<>();
    private ErrorHandler errorHandler;
}
