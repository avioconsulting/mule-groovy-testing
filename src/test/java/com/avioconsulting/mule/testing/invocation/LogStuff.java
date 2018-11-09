package com.avioconsulting.mule.testing.invocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogStuff {
    private static final Logger logger = LoggerFactory.getLogger(LogStuff.class);

    public static boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public static void logStuff() {
        logger.debug("Hello there");
    }
}
