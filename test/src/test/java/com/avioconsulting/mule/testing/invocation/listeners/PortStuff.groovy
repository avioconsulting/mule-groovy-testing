package com.avioconsulting.mule.testing.invocation.listeners

import com.avioconsulting.mule.testing.OpenPortLocator

trait PortStuff implements OpenPortLocator {
    private static final String TEST_PORT_PROPERTY = 'avio.test.http.port'
    static int unusedPort = -1

    Map getStartUpProperties() {
        def properties = super.getStartUpProperties()
        // have to have the listener running to use apikit
        // http listener gets go
        // ing before the properties object this method creates has had its values take effect
        if (unusedPort == -1) {
            unusedPort = getHttpPort()
            log.info 'Setting HTTP listener port to {}',
                     unusedPort
        }
        properties.put(TEST_PORT_PROPERTY,
                       unusedPort as String)
        properties
    }
}
