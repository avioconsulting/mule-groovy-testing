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
    void mocks_post() {
        // arrange
        def mockPayload = null
        mockRestHttpCall('the name of our connector') {
            json {
                whenCalledWith(String) { String ourPayload ->
                    mockPayload = ourPayload
                    'new payload'
                }
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
                   mockPayload,
                   is(equalTo('nope'))
        assertThat result,
                   is(equalTo('new payload'))
    }

    @Test
    void mocks_with_http_request_info() {
        // arrange

        // act

        // assert
        fail 'write the test'
    }

    @Test
    void mocks_with_event() {
    }

    @Test
    void mocks_get() {
        // arrange

        // act

        // assert
        fail 'write the test'
    }

}
