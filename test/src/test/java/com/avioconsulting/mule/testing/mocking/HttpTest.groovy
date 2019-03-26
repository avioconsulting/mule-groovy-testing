package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.ConfigTrait
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.InvokeExceptionWrapper
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.ReturnWrapper
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.StreamUtils
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.connectors.HttpRequesterInfo
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j2
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

@Log4j2
class HttpTest extends
        BaseJunitTest implements
        ConfigTrait,
        StreamUtils {
    List<String> getConfigResources() {
        ['http_test.xml']
    }

    @Test
    void mocksProperly() {
        // arrange
        def stuff = null
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { Map incoming ->
                    stuff = incoming
                    [reply: 456]
                }
            }
        }

        // act
        def result = runFlow('restRequest') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assertThat stuff,
                   is(equalTo([key: 123]))
        assertThat result,
                   is(equalTo([reply_key: 457]))
    }

    @Test
    void mimeType_not_set() {
        // arrange
        def stuff = null
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { HttpRequesterInfo connectorInfo ->
                    stuff = connectorInfo.mimeType
                    [reply: 456]
                }
            }
        }

        // act
        runFlow('restRequest') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assertThat stuff,
                   is(nullValue())
    }

    @Test
    void mimeType_set() {
        // arrange
        def stuff = null
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { HttpRequesterInfo connectorInfo ->
                    stuff = connectorInfo.mimeType
                    [reply: 456]
                }
            }
        }

        // act
        runFlow('restRequestMimeType') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assertThat stuff,
                   is(equalTo('application/xml'))
    }

    @Test
    void mocksProperly_from_flowVar() {
        // arrange
        def stuff = null
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { Map incoming ->
                    stuff = incoming
                    [reply: 456]
                }
            }
        }

        // act
        def result = runFlow('restRequestFromFlowVar') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assertThat stuff,
                   is(equalTo([key: 123]))
        assertThat result,
                   is(equalTo([reply_key: 457]))
    }

    @Test
    void mocksProperly_from_flowVar_raw() {
        // arrange
        def stuff = null
        mockRestHttpCall('SomeSystem Call') {
            raw {
                whenCalledWith { incoming ->
                    stuff = incoming
                    // we're in raw mode so use a string
                    new ReturnWrapper(JsonOutput.toJson([reply: 456]),
                                      'application/json')
                }
            }
        }

        // act
        def result = runFlow('restRequestFromFlowVar') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        // we're in raw mode so our payload will be a JSON string
        def asMap = new JsonSlurper().parseText(stuff)
        assertThat asMap,
                   is(equalTo([key: 123]))
        assertThat result,
                   is(equalTo([reply_key: 457]))
    }

    @Test
    void mocksProperly_from_to_flowVar() {
        // arrange
        def stuff = null
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { Map incoming ->
                    stuff = incoming
                    [reply: 456]
                }
            }
        }

        // act
        def result = runFlow('restRequestFromToFlowVar') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assertThat stuff,
                   is(equalTo([key: 123]))
        assertThat 'each of our 3 operations should be preserved (original payload, 1st DW, and HTTP)',
                   result,
                   is(equalTo([reply_key: 703]))
    }

    @Test
    void mocks_Properly_target_other_than_payload() {
        // arrange
        def stuff = null
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { Map incoming ->
                    stuff = incoming
                    [reply: 456]
                }
            }
        }

        // act
        def result = runFlow('restRequestToFlowVar') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assertThat stuff,
                   is(equalTo([key: 123]))
        assertThat 'original payload of 123 + 456 from our mock + 1 in the DW',
                   result,
                   is(equalTo([reply_key: 580]))
    }

    @Test
    void mocksProperly_raw() {
        // arrange
        def stuff = null
        mockRestHttpCall('SomeSystem Call') {
            raw {
                whenCalledWith { incoming ->
                    stuff = incoming
                    new ReturnWrapper(JsonOutput.toJson([reply: 456]),
                                      'application/json')
                }
            }
        }

        // act
        def result = runFlow('restRequest') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assertThat result,
                   is(equalTo([reply_key: 457]))
        assertThat new JsonSlurper().parseText(stuff),
                   is(equalTo([key: 123]))
    }

    @Test
    void mocksProperly_raw_map_payload_for_form_url_encoded() {
        // arrange
        def stuff = null

        mockRestHttpCall('SomeSystem Call') {
            raw {
                whenCalledWith { incoming ->
                    stuff = incoming
                    new ReturnWrapper(JsonOutput.toJson([reply: 456]),
                                      'application/json')
                }
            }
        }

        // act
        def result = runFlow('javaPayloadForFormUrlEncoded') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assertThat result,
                   is(equalTo([reply_key: 457]))
        assertThat stuff,
                   is(equalTo([key: 123]))
    }

    @Test
    void mocksProperly_raw_map_payload_null() {
        // arrange
        def stuff = 'should not see this'
        mockRestHttpCall('SomeSystem Call') {
            raw {
                whenCalledWith { incoming ->
                    stuff = incoming
                    new ReturnWrapper(JsonOutput.toJson([reply: 456]),
                                      'application/json')
                }
            }
        }

        // act
        def result = runFlow('javaPayloadNullOnPurpose') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assertThat result,
                   is(equalTo([reply_key: 457]))
        assertThat stuff,
                   is(nullValue())
    }

    @Test
    void mocksProperlyWithChoice() {
        // arrange
        def stuff = null
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { Map incoming ->
                    stuff = incoming
                    [reply: 456]
                }
            }
        }

        // act
        def result = runFlow('restRequestWithChoice') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assertThat stuff,
                   is(equalTo([key: 123]))
        assertThat result,
                   is(equalTo([reply_key: 457]))
    }

    @Test
    void queryParameters() {
        // arrange
        Map actualParams = null
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { HttpRequesterInfo requestInfo ->
                    actualParams = requestInfo.queryParams
                    [reply: 456]
                }
            }
        }

        // act
        def result = runFlow('queryParameters') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assert actualParams
        assertThat actualParams,
                   is(equalTo([stuff: '123']))
        assertThat result,
                   is(equalTo([reply_key: 457]))
    }

    @Test
    void httpVerb() {
        // arrange
        String actualVerb = null
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { Map incoming,
                                 HttpRequesterInfo requestInfo ->
                    actualVerb = requestInfo.method
                    [reply: 456]

                }
            }
        }

        // act
        runFlow('restRequest') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assert actualVerb
        assertThat actualVerb,
                   is(equalTo('POST'))
    }

    @Test
    void request_payload_is_passed() {
        // arrange
        Map actualIncoming = null
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { Map incoming,
                                 HttpRequesterInfo requestInfo ->
                    actualIncoming = incoming
                    [reply: 456]

                }
            }
        }

        // act
        runFlow('restRequest') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assertThat actualIncoming,
                   is(equalTo([key: 123]))
    }

    class Dummy {

    }

    @Test
    void http_get_ignores_payload() {
        def input = new Dummy()
        // arrange
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith {
                    [reply: 456]
                }
            }
        }

        // act
        def result = runFlow('restRequestGet') {
            // using Java to surface attempts to deserialize this payload, etc.
            java {
                inputPayload(input)
            }
        }

        // assert
        assert result
    }

    @Test
    void http_return_set_201_code() {
        // arrange
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith {
                    setHttpReturnCode(201)
                    [reply: 456]
                }
            }
        }

        // act
        def result = runFlow('queryParametersHttpStatus') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assertThat result,
                   is(equalTo([reply_key: 201]))
    }

    @Test
    void http_return_error_code_custom() {
        // arrange
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith {
                    setHttpReturnCode(202)
                    [reply: 456]
                }
            }
        }

        // act
        def result = shouldFail {
            runFlow('queryParametersHttpStatus') {
                json {
                    inputPayload([foo: 123])
                }
            }
        }

        // assert
        assertThat result.message,
                   is(containsString("HTTP GET on resource 'http://localhost:443/some_path/there' failed with status code 202."))
    }

    @Test
    void http_return_error_code() {
        // arrange
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith {
                    setHttpReturnCode(404)
                    [reply: 456]
                }
            }
        }

        // act
        def result = shouldFail {
            runFlow('queryParametersHttpStatus') {
                json {
                    inputPayload([foo: 123])
                }
            }
        }

        // assert
        assertThat result,
                   is(instanceOf(InvokeExceptionWrapper))
        def cause = result.cause
        def causeOfCause = cause.cause
        assertThat causeOfCause.getClass().name,
                   is(equalTo('org.mule.extension.http.api.request.validator.ResponseValidatorTypedException'))
        assertThat cause.message,
                   is(equalTo("HTTP GET on resource 'http://localhost:443/some_path/there' failed: not found (404)."))
        assertThat causeOfCause.getClass().name,
                   is(equalTo('org.mule.extension.http.api.request.validator.ResponseValidatorTypedException'))
        assertThat cause.info['Error type'],
                   is(equalTo('HTTP:NOT_FOUND'))
    }

    @Test
    void http_return_error_code_default_validator_fail() {
        // arrange
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith {
                    setHttpReturnCode(404)
                    [reply: 456]
                }
            }
        }

        // act
        def result = shouldFail {
            runFlow('queryParameters') {
                json {
                    inputPayload([foo: 123])
                }
            }
        }

        // assert
        assertThat result,
                   is(instanceOf(InvokeExceptionWrapper))
        def cause = result.cause
        def causeOfCause = cause.cause
        assertThat causeOfCause.getClass().name,
                   is(equalTo('org.mule.extension.http.api.request.validator.ResponseValidatorTypedException'))
        assertThat cause.message,
                   is(equalTo("HTTP GET on resource 'http://localhost:443/some_path/there' failed: not found (404)."))
        assertThat causeOfCause.getClass().name,
                   is(equalTo('org.mule.extension.http.api.request.validator.ResponseValidatorTypedException'))
        assertThat cause.info['Error type'],
                   is(equalTo('HTTP:NOT_FOUND'))
    }

    @Test
    void http_return_error_code_only_on_first_call() {
        // arrange
        def returnError = true
        // for closure
        def logger = log
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith {
                    if (returnError) {
                        logger.info 'Returning 500 error from mock'
                        setHttpReturnCode(500)
                        return
                    }
                    logger.info 'Returning success from mock'
                    [reply: 456]
                }
            }
        }

        log.info 'Triggering first call, which should fail'
        shouldFail {
            runFlow('queryParametersHttpStatus') {
                json {
                    inputPayload([foo: 123])
                }
            }
        }
        returnError = false

        // act
        log.info 'Triggering 2nd call, which should NOT fail'
        def result = runFlow('queryParametersHttpStatus') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assertThat result,
                   is(equalTo([
                           reply_key: 200
                   ]))
    }

    @Test
    void headers() {
        // arrange
        def actualHeaders = null
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { HttpRequesterInfo requestInfo ->
                    actualHeaders = requestInfo.headers
                    [reply: 456]
                }
            }
        }

        // act
        runFlow('headerTest') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assertThat actualHeaders,
                   is(equalTo(
                           [
                                   theHeaderName: 'theHeaderValue'
                           ]
                   ))
    }

    @Test
    void httpPassString() {
        // arrange
        def stuff = null
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { Map incoming ->
                    stuff = incoming
                    [reply: 456]
                }
            }
        }

        // act
        def result = runFlow('restRequestString') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assertThat stuff,
                   is(equalTo([key: 123]))
        assertThat result,
                   is(equalTo([reply_key: 457]))
    }

    @Test
    void httpConnectIssue() {
        // arrange
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { Map incoming ->
                    httpConnectError()
                }
            }
        }

        // act
        def result = shouldFail {
            runFlow('restRequest') {
                json {
                    inputPayload([foo: 123])
                }
            }
        }

        // assert
        assertThat result,
                   is(instanceOf(InvokeExceptionWrapper))
        def cause = result.cause
        def causeOfCause = cause.cause
        assertThat causeOfCause.getClass().name,
                   is(equalTo('org.mule.extension.http.api.error.HttpRequestFailedException'))
        assertThat causeOfCause.cause.getClass().name,
                   is(equalTo('java.net.ConnectException'))
        assertThat cause.info['Error type'],
                   is(equalTo('HTTP:CONNECTIVITY'))
        assertThat cause.message,
                   is(equalTo("HTTP POST on resource 'http://localhost:443/some_path' failed: Connection refused."))
    }

    @Test
    void httpTimeoutError_test() {
        // arrange
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { Map incoming ->
                    httpTimeoutError()
                }
            }
        }

        // act
        def result = shouldFail {
            runFlow('restRequest') {
                json {
                    inputPayload([foo: 123])
                }
            }
        }

        // assert
        assertThat result,
                   is(instanceOf(InvokeExceptionWrapper))
        def cause = result.cause
        def causeOfCause = cause.cause
        assertThat causeOfCause.getClass().name,
                   is(equalTo('org.mule.extension.http.api.error.HttpRequestFailedException'))
        assertThat causeOfCause.cause.getClass().name,
                   is(equalTo('java.util.concurrent.TimeoutException'))
        assertThat cause.info['Error type'],
                   is(equalTo('HTTP:TIMEOUT'))
        assertThat cause.message,
                   is(equalTo("HTTP POST on resource 'http://localhost:443/some_path' failed: Some timeout error."))
    }

    @Test
    void error_http_params() {
        // arrange
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { Map incoming ->
                    [foo: 123]
                }
            }
        }

        // act
        def exception = shouldFail {
            runFlow('errorInHttpParamsTest') {
                json {
                    inputPayload(null)
                }
            }
        }

        // assert
        assertThat 'We should not fail due to test framework machinery. The problem is in the code, plain and simple',
                   exception.message,
                   is(containsString("Usually HTTP requesters have responseValidationSettings set on them. This one does not. This usually happens when the DW 2.0 logic that builds HTTP headers, query params, etc has a DW error in it. Check your DW logic in <http:headers> etc. carefully"))
    }
}
