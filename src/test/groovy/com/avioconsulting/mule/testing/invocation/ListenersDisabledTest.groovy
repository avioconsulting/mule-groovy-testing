package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.BaseJunitTest
import com.avioconsulting.mule.testing.OverrideConfigList
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

    @Override
    Properties getStartUpProperties() {
        def properties = super.getStartUpProperties()
        // have to have the listener running to use apikit
        // http listener gets go
        // ing before the properties object this method creates has had its values take effect
        def port = unusedPort
        log.info 'Setting HTTP listener port to {}',
                 port
        System.setProperty(TEST_PORT_PROPERTY,
                           port as String)
        properties
    }

    static int getUnusedPort() {
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

    static int getChosenHttpPort() {
        // avoid duplicate ports
        Integer.parseInt(System.getProperty(TEST_PORT_PROPERTY))
    }

    @Test
    void cant_access_url() {
        // arrange
        def port = getChosenHttpPort()
        def url = "http://localhost:${port}/the-app/api/v1/howdy".toURL()

        // act
        log.info 'Attempting to access {}',
                 url
        def exception = shouldFail {
            log.info "Got this back from the URL but should not have '{}'",
                     url.text
        }

        // assert
        assertThat exception,
                   is(instanceOf(FileNotFoundException))
    }

    // TODO: Look into disabling actual HTTP listener/global elements later on
    @Test
    @Ignore('MUnit does not disable the actual listener config/global element, just the flow listener. Will come back to this')
    void port_not_used() {
        // arrange
        def port = getChosenHttpPort()
        log.info 'Will attempt to bind to socket {}',
                 port

        try {
            def server = new ServerSocket(port,
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
    List<String> getConfigResourcesList() {
        [
                'global-test.xml',
                'listener-disabled-test.xml'
        ]
    }
}
