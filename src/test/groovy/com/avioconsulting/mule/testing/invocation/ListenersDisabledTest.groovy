package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import groovy.util.logging.Log4j2
import org.junit.Ignore
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail

@Log4j2
class ListenersDisabledTest extends BaseJunitTest implements OverrideConfigList {
    private static final String TEST_PORT_PROPERTY = 'avio.test.http.port'
    static int unusedPort = -1

    @Override
    Properties getStartUpProperties() {
        def properties = super.getStartUpProperties()
        // have to have the listener running to use apikit
        // http listener gets go
        // ing before the properties object this method creates has had its values take effect
        if (unusedPort == -1) {
            unusedPort = findUnusedPort()
            log.info 'Setting HTTP listener port to {}',
                     unusedPort
        }
        properties.put(TEST_PORT_PROPERTY,
                       unusedPort as String)
        properties
    }

    static int findUnusedPort() {
        (8088..8199).find { candidate ->
            try {
                def socket = new ServerSocket(candidate,
                                              1,
                                              InetAddress.loopbackAddress)
                socket.close()
                true
            }
            catch (IOException ignored) {
                false
            }
        }
    }

    @Test
    void cant_access_url() {
        // arrange
        def url = "http://localhost:${unusedPort}/the-app/api/v1/howdy".toURL()

        // act
        log.info 'Attempting to access {}',
                 url
        def exception = shouldFail {
            log.info "Got this back from the URL but should not have '{}'",
                     url.text
        }

        // assert
        assertThat exception.message,
                   is(equalTo('Server returned HTTP response code: 503 for URL: http://localhost:8088/the-app/api/v1/howdy'))
    }

    // TODO: Look into disabling actual HTTP listener/global elements later on
    @Test
    @Ignore('MUnit does not disable the actual listener config/global element, just the flow listener. Will come back to this')
    void port_not_used() {
        // arrange
        log.info 'Will attempt to bind to socket {}',
                 unusedPort

        try {
            def server = new ServerSocket(unusedPort,
                                          1,
                                          InetAddress.loopbackAddress)
            server.close()
        }
        catch (BindException e) {
            fail('The loopback address/port should have been available for binding but it was not')
        }
    }

    @Test
    void works_via_flow_ref() {
        // arrange

        // act
        def result = runFlow('theTest') {
            java {
                inputPayload(null)
            }
        } as String

        // assert
        assertThat result,
                   is(equalTo('our payload'))
    }


    @Override
    List<String> getConfigResources() {
        [
                'global-test.xml',
                'listener-disabled-test.xml'
        ]
    }
}
