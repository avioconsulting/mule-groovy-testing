package com.avioconsulting.mule.testing.invocation.listeners

import com.avioconsulting.mule.testing.ConfigTrait
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import groovy.util.logging.Log4j2
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

@Log4j2
class ListenersDisabledTest extends
        BaseJunitTest implements
        ConfigTrait,
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
                'listener-enabledisable-test.xml'
        ]
    }
}
