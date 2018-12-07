package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
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
        def params = [:]
        mockApiCall('the name of our connector') {
            whenCalledWith { Map parameters ->
                params = parameters
                'new payload'
            }
        }

        // act
        def result = runFlow('fooFlow') {
            java {
                inputPayload('nope')
            }
        }

        // assert
        assertThat 'Parameter key is based on the value inside the module XML, not the call to the module',
                   params,
                   is(equalTo([
                           value: 'howdy'
                   ]))
        assertThat result,
                   is(equalTo('new payload'))
    }

    @Test
    void mocksProperly_with_event() {
        // arrange
        def params = [:]
        mockApiCall('the name of our connector') {
            whenCalledWith { Map parameters,
                             EventWrapper event ->
                params = parameters
                'new payload'
            }
        }

        // act
        def result = runFlow('fooFlow') {
            java {
                inputPayload('nope')
            }
        }

        // assert
        assertThat 'Parameter key is based on the value inside the module XML, not the call to the module',
                   params,
                   is(equalTo([
                           value: 'howdy'
                   ]))
        assertThat result,
                   is(equalTo('new payload'))
        fail 'write the test'
    }
}
