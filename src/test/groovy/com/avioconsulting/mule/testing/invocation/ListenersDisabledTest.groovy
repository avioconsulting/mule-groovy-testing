package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.BaseApiKitTest
import com.avioconsulting.mule.testing.OverrideConfigList
import groovy.util.logging.Log4j2
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

@Log4j2
class ListenersDisabledTest extends BaseApiKitTest implements OverrideConfigList {
    @Test
    void does_not_listen() {
        // arrange
        def port = getChosenHttpPort()
        def url = "http://localhost:${port}/the-app/api/v1/resources?foo=123&bar=456".toURL()

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

    String getApiNameUnderTest() {
        'the-app'
    }

    String getApiVersionUnderTest() {
        'v1'
    }

    List<String> getConfigResourcesList() {
        [
                'global-test.xml',
                "${fullApiName}.xml".toString()
        ]
    }
}
