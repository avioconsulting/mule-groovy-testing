package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors.HttpRequesterInfo
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
        def mockPayload = null
        HttpRequesterInfo actualHttpRequestInfo = null
        mockRestHttpCall('the name of our connector') {
            json {
                whenCalledWith(String) { String ourPayload,
                                         HttpRequesterInfo httpInfo ->
                    mockPayload = ourPayload
                    actualHttpRequestInfo = httpInfo
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
        assertThat actualHttpRequestInfo.method,
                   is(equalTo('POST'))
    }

    @Test
    void mocks_with_event() {
        def mockPayload = null
        EventWrapper actualEvent = null
        mockRestHttpCall('the name of our connector') {
            json {
                whenCalledWith(String) { String ourPayload,
                                         EventWrapper event ->
                    mockPayload = ourPayload
                    actualEvent = event
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
        assertThat 'Parameters to module show up as flowVars inside the module',
                   actualEvent.getVariable('inputParam').value,
                   is(equalTo('nope'))
    }

    @Test
    void mocks_get() {
        // arrange

        // act

        // assert
        fail 'write the test'
    }

}
