package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.ConfigTrait
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors.HttpRequesterInfo
import groovy.util.logging.Log4j2
import org.junit.Ignore
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

@Log4j2
class ApiMockTest extends
        BaseJunitTest implements
        ConfigTrait {
    List<String> getConfigResources() {
        ['api-mock.xml']
    }

    // TODO: Not sure about several things here. For one, we're not supplying inputParam
    // when we call the connector. What is our mock supposed to be doing then?
    // it's almost like we need a dedicated "mock connector" function that makes
    // params available
    // what is also going on is prior to 4.3.0, the workaround the interceptor code
    // in this framework does allows us to mock HTTP requesters inside custom connectors
    // but using the name of the customer connector invocation, not the HTTP requester
    // they've done something in 4.3.0 that "fixes" the need for that.
    // we'll probably have to:
    // 1) decided whether to support < 4.3.0 with this framework going forward.
    // 2) If not, there are other pre 4.3 and 4.2 workarounds in code that can be removed
    // 3) regardless, we should probably make mockHttpRequesterInsideConnector a more deliberate
    //    mocking method and deliberate code finding the http requester stuff in the
    //    module's child processor list. this could get hairy when building ConnectorInfo
    //    objects because we rely heavily on the parameter list, which will not be
    //    the connector. alternatively we may have to invert the current paradigm
    //    and mock the http requester IF the parent module connector invocation matches a name
    // and 4) get a dedicated moc connector method going as discussed above
    @Test
    void mock_entire_connector() {
        // arrange
        def mockPayload = null
        mockGeneric('the name of our connector') {
            json {
                whenCalledWith { Map ourPayload ->
                    mockPayload = ourPayload
                    [
                            foo: 'the new payload'
                    ]
                }
            }
        }

        // act
        def result = runFlow('fooFlow') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assertThat 'Parameter key is based on the value inside the module XML, not the call to the module',
                   mockPayload,
                   is(equalTo([foo: 123]))
        assertThat result,
                   is(equalTo('new payload'))
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
    void mocksPostForeach() {
        // arrange
        def mockPayloads = []
        mockRestHttpCall('the name of our connector') {
            json {
                whenCalledWith(String) { String ourPayload ->
                    mockPayloads << ourPayload
                    'new payload'
                }
            }
        }

        // act
        runFlow('fooFlowForEach') {
            java {
                inputPayload(['hello', 'there'])
            }
        }

        // assert
        assertThat mockPayloads,
                   is(equalTo(
                           [
                                   'hello',
                                   'there'
                           ]
                   ))
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
        HttpRequesterInfo actualHttpRequestInfo = null
        mockRestHttpCall('the name of our connector') {
            json {
                whenCalledWith(String) { HttpRequesterInfo httpInfo ->
                    actualHttpRequestInfo = httpInfo
                    'new payload'
                }
            }
        }

        // act
        def result = runFlow('fooGetFlow') {
            java {
                inputPayload('nope')
            }
        }

        // assert
        assertThat result,
                   is(equalTo('new payload'))
        assertThat actualHttpRequestInfo.method,
                   is(equalTo('GET'))
        assertThat actualHttpRequestInfo.queryParams,
                   is(equalTo([
                           created_by: 'nope'
                   ]))
    }

    @Test
    void mocks_get_try_logger_first() {
        // arrange
        HttpRequesterInfo actualHttpRequestInfo = null
        mockRestHttpCall('the name of our connector') {
            json {
                whenCalledWith(String) { HttpRequesterInfo httpInfo ->
                    actualHttpRequestInfo = httpInfo
                    'new payload'
                }
            }
        }

        // act
        def result = runFlow('fooGetFlowTryLoggerFirst') {
            java {
                inputPayload('nope')
            }
        }

        // assert
        assertThat result,
                   is(equalTo('new payload'))
        assertThat actualHttpRequestInfo.method,
                   is(equalTo('GET'))
        assertThat actualHttpRequestInfo.queryParams,
                   is(equalTo([
                           created_by: 'nope'
                   ]))
    }

    @Test
    void mocks_get_try_connector_first() {
        // arrange
        HttpRequesterInfo actualHttpRequestInfo = null
        mockRestHttpCall('the name of our connector') {
            json {
                whenCalledWith(String) { HttpRequesterInfo httpInfo ->
                    actualHttpRequestInfo = httpInfo
                    'new payload'
                }
            }
        }

        // act
        def result = runFlow('fooGetFlowTryConnectorFirst') {
            java {
                inputPayload('nope')
            }
        }

        // assert
        assertThat result,
                   is(equalTo('new payload'))
        assertThat actualHttpRequestInfo.method,
                   is(equalTo('GET'))
        assertThat actualHttpRequestInfo.queryParams,
                   is(equalTo([
                           created_by: 'nope'
                   ]))
    }

    @Test
    void mocks_get_try_logger_after_connector() {
        // arrange
        HttpRequesterInfo actualHttpRequestInfo = null
        mockRestHttpCall('the name of our connector') {
            json {
                whenCalledWith(String) { HttpRequesterInfo httpInfo ->
                    actualHttpRequestInfo = httpInfo
                    'new payload'
                }
            }
        }

        // act
        def result = runFlow('fooGetFlowTryLoggerAfterConnector') {
            java {
                inputPayload('nope')
            }
        }

        // assert
        assertThat result,
                   is(equalTo('new payload'))
        assertThat actualHttpRequestInfo.method,
                   is(equalTo('GET'))
        assertThat actualHttpRequestInfo.queryParams,
                   is(equalTo([
                           created_by: 'nope'
                   ]))
    }

    @Test
    void mocks_get_try_error_handler() {
        // arrange
        HttpRequesterInfo actualHttpRequestInfo = null
        mockRestHttpCall('the name of our connector') {
            json {
                whenCalledWith(String) { HttpRequesterInfo httpInfo ->
                    actualHttpRequestInfo = httpInfo
                    'new payload'
                }
            }
        }

        // act
        def result = runFlow('fooGetFlowTryErrorHandler') {
            java {
                inputPayload('nope')
            }
        }

        // assert
        assertThat result,
                   is(equalTo('new payload'))
        assertThat actualHttpRequestInfo.method,
                   is(equalTo('GET'))
        assertThat actualHttpRequestInfo.queryParams,
                   is(equalTo([
                           created_by: 'nope'
                   ]))
    }

    @Test
    void mock_get_error() {
        // arrange
        mockRestHttpCall('the name of our connector') {
            json {
                whenCalledWith(String) { HttpRequesterInfo httpInfo ->
                    setHttpStatusCode(404)
                    'new payload'
                }
            }
        }

        // act
        def exception = shouldFail {
            runFlow('fooGetFlow') {
                java {
                    inputPayload('nope')
                }
            }
        }

        // assert
        assertThat exception.message,
                   is(equalTo('org.mule.runtime.core.internal.exception.MessagingException: HTTP GET on resource \'/stuff\' failed: not found (404).; ErrorType: MODULE-HELLO:NOT_FOUND'))
    }

    @Test
    void elementWithQuotedStuffInsideTry() {
        // arrange
        def actualResult = null

        // act
        runFlow('elementWithQuotedStuffInsideTry') {
            json {
                inputPayload([foo: 123])
            }
            withOutputEvent { EventWrapper event ->
                actualResult = event.getVariable('someVariable')
            }
        }

        // assert
        assertThat actualResult.value,
                   is(equalTo('"quoted stuff"'))
    }
}
