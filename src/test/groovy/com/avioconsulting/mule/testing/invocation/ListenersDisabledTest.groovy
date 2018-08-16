package com.avioconsulting.mule.testing.invocation


import com.avioconsulting.mule.testing.BaseJunitTest
import com.avioconsulting.mule.testing.OverrideConfigList
import groovy.util.logging.Log4j2
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

@Log4j2
class ListenersDisabledTest extends BaseJunitTest implements OverrideConfigList {
    private static final String TEST_PORT_PROPERTY = 'avio.test.http.port'

    @Override
    Properties getStartUpProperties() {
        def properties = super.getStartUpProperties()
        // have to have the listener running to use apikit
        // http listener gets go
        // ing before the properties object this method creates has had its values take effect
        System.setProperty(TEST_PORT_PROPERTY,
                           httpPort as String)
        properties
    }

    static int getHttpPort() {
        (8088..8199).find { candidate ->
            try {
                def socket = new ServerSocket(candidate)
                socket.close()
                true
            }
            catch (IOException ignored) {
                false
            }
        }
    }

    static int getChosenHttpPort() {
        // avoid duplicate ports
        Integer.parseInt(System.getProperty(TEST_PORT_PROPERTY))
    }

    @Test
    void does_not_listen() {
        // arrange
        def port = getChosenHttpPort()
        def url = "http://localhost:${port}/the-app/api/v1/howdy".toURL()

        // act
        log.info 'Attempting to access {}',
                 url
        def exception = shouldFail {
            url.text
        }

        // assert
        assertThat exception,
                   is(instanceOf(String))
    }

    @Override
    List<String> getConfigResourcesList() {
        [
                'global-test.xml',
                'listener-disabled-test.xml'
        ]
    }
}
