package com.avioconsulting.mule.testing.invocation.listeners


import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import groovy.util.logging.Log4j2
import org.junit.Test

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
        def result = url.text

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
