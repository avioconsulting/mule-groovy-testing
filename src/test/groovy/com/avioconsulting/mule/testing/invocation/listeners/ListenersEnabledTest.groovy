package com.avioconsulting.mule.testing.invocation.listeners

import com.avioconsulting.mule.testing.BaseMuleGroovyTrait
import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import groovy.util.logging.Log4j2
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

@Log4j2
class ListenersEnabledTest extends
        BaseJunitTest implements
        OverrideConfigList,
        PortStuff {
    
    @Override
    List<String> keepListenersOnForTheseFlows() {
        ['theTest']
    }

    @Test
    void can_access_url() {
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
                   is(equalTo('FOOBAR: Server returned HTTP response code: 503 for URL: http://localhost:8088/the-app/api/v1/howdy'))
    }

    @Override
    List<String> getConfigResources() {
        [
                'global-test.xml',
                // TODO: Rename this file
                'listener-disabled-test.xml'
        ]
    }
}
