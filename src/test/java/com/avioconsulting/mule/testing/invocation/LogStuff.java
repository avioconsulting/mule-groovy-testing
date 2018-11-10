package com.avioconsulting.mule.testing.invocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogStuff {
    private static final Logger logger = LoggerFactory.getLogger(LogStuff.class);

    // TODO: Need to replicate our problem, this won't do it. Maybe we can create a simple extension/connector that will return its classloader and behave more like the DB connector does
    public static String isClassLoadingCorrect() {
        return LogStuff.class.getClassLoader().toString() + "-" + logger.isDebugEnabled();
    }

    public static void logStuff() {
        logger.debug("Hello there");
    }
}
