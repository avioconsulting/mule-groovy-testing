package com.avioconsulting.mule.testing.invocation.listeners


import com.avioconsulting.mule.testing.ConfigTrait
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import groovy.util.logging.Log4j2
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

@Log4j2
class ListenersEnabledLazyInitTest extends
        BaseJunitTest implements
        ConfigTrait,
        PortStuff {

    @Override
    List<String> keepListenersOnForTheseFlows() {
        ['theTest']
    }

    boolean isUseLazyInit() {
        // see isUseLazyConnections, lazyInit is not required to address that issue
        // since our runner does a decent job of loading everything before the test starts, in order to
        // keep the actual test method output less noisy, making this false by default
        true
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
