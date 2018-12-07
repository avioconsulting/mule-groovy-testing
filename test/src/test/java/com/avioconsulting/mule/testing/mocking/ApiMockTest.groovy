package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import groovy.util.logging.Log4j2
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

@Log4j2
class ApiMockTest extends
        BaseJunitTest implements
        OverrideConfigList {
    List<String> getConfigResources() {
        ['api-mock.xml']
    }

    @Test
    void mocksProperly() {
        // arrange
        def payload = null
        def params = [:]
        mockApiCall('the name of our connector') {
            whenCalledWith { mockPayload, mockParams ->
                payload = mockPayload
                params = mockParams
            }
        }

        // act
        def result = runFlow('fooFlow') {
            java {
                inputPayload('nope')
            }
        }

        // assert
        assertThat payload,
                   is(equalTo('nope'))
        assertThat params,
                   is(equalTo([
                           inputParam: 'howdy'
                   ]))
        assertThat result,
                   is(equalTo('howdy'))
    }
}
