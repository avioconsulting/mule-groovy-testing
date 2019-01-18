package com.avioconsulting.mule.testing.connectortest.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Executor {
    private static final Logger logger = LoggerFactory.getLogger(Executor.class);

    public boolean doExecute() {
        return logger.isDebugEnabled();
    }
}
