package com.avioconsulting.mule.testing.invocation.listeners

import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import groovy.util.logging.Log4j2
import org.junit.Ignore
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail

@Log4j2
class ListenersDisabledTest extends
        BaseJunitTest implements
        OverrideConfigList,
        PortStuff {
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
                   is(equalTo("Server returned HTTP response code: 503 for URL: http://localhost:${unusedPort}/the-app/api/v1/howdy".toString()))
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
                'listener-enabledisable-test.xml'
        ]
    }
}
