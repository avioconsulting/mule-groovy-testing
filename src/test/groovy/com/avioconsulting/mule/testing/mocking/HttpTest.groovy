package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.SampleJacksonInput
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import com.avioconsulting.mule.testing.mocks.HttpRequestInfo
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j2
import org.junit.Test
import org.mule.api.MessagingException
import org.mule.api.MuleEvent
import org.mule.module.http.internal.request.DefaultHttpRequester

import java.util.concurrent.TimeoutException

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

@Log4j2
class HttpTest extends BaseJunitTest implements OverrideConfigList {
    List<String> getConfigResourcesList() {
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
    void mocksProperly_raw() {
        // arrange
        def stuff = null
        mockRestHttpCall('SomeSystem Call') {
            raw {
                whenCalledWith { Object incoming ->
                    stuff = incoming
                    new ByteArrayInputStream(JsonOutput.toJson([reply: 456]).bytes)
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
                   is(instanceOf(InputStream))
        assertThat new JsonSlurper().parse(stuff),
                   is(equalTo([key: 123]))
        assertThat result,
                   is(equalTo([reply_key: 457]))
    }

    @Test
    void mocksProperly_raw_with_mule_event() {
        // arrange
        def stuff = null
        MuleEvent actualEvent = null
        mockRestHttpCall('SomeSystem Call') {
            raw {
                whenCalledWith { Object incoming,
                                 MuleEvent event ->
                    stuff = incoming
                    actualEvent = event
                    new ByteArrayInputStream(JsonOutput.toJson([reply: 456]).bytes)
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
                   is(instanceOf(InputStream))
        assertThat new JsonSlurper().parse(stuff),
                   is(equalTo([key: 123]))
        assertThat result,
                   is(equalTo([reply_key: 457]))
        assertThat actualEvent.getFlowVariable('foobar') as String,
                   is(equalTo('howdy'))
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
    void contentTypeNotSet_for_flow() {
        // arrange
        def input = new SampleJacksonInput()
        input.foobar = 123
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith(SampleMockedJacksonInput) {
                    SampleMockedJacksonInput incoming ->
                        def reply = new SampleMockedJacksonOutput()
                        reply.foobar = 456
                        reply
                }
            }
        }

        // act
        def result = shouldFail {
            runFlow('restRequestContentTypeNotSet') {
                json {
                    inputPayload(input,
                                 JacksonOutput)
                }
            }
        }

        // assert
        assertThat result.message,
                   is(containsString(
                           "Expected Content-Type to be of type [application/json, application/json;charset=UTF-8, application/json;charset=utf-8, application/json;charset=windows-1252] but it actually was null. This happened while calling your flow. Add a set-property before the end of the flow."))
    }

    @Test
    void contentTypeNotSet_checkDisabled_for_flow() {
        // arrange
        def input = new SampleJacksonInput()
        input.foobar = 123
        def mockValue = 0
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith(SampleMockedJacksonInput) {
                    SampleMockedJacksonInput incoming ->
                        mockValue = incoming.foobar
                        def reply = new SampleMockedJacksonOutput()
                        reply.foobar = 456
                        reply
                }
            }
        }

        // act
        def result = runFlow('restRequestContentTypeNotSet') {
            json {
                inputPayload(input,
                             JacksonOutput)
            }

            disableContentTypeCheck()
        } as JacksonOutput

        // assert
        assertThat result.result,
                   is(equalTo(457))
        assertThat mockValue,
                   is(equalTo(123))
    }

    @Test
    void contentTypeNotSet_for_mock() {
        // arrange
        def input = new SampleJacksonInput()
        input.foobar = 123
        def mockValue = 0
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith(SampleMockedJacksonInput) {
                    SampleMockedJacksonInput incoming ->
                        mockValue = incoming.foobar
                        def reply = new SampleMockedJacksonOutput()
                        reply.foobar = 456
                        reply
                }
            }
        }

        // act
        def result = shouldFail {
            runFlow('restRequestContentTypeNotSetForMock') {
                json {
                    inputPayload(input,
                                 JacksonOutput)
                }
            }
        }

        // assert
        assertThat result.message,
                   is(containsString(
                           "Expected Content-Type to be of type [application/json, application/json;charset=UTF-8, application/json;charset=utf-8, application/json;charset=windows-1252] but it actually was null. Check your mock endpoints."))
    }

    @Test
    void contentTypeNotSet_for_mock_checkDisabled() {
        // arrange
        def input = new SampleJacksonInput()
        input.foobar = 123
        def mockValue = 0
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith(SampleMockedJacksonInput) {
                    SampleMockedJacksonInput incoming ->
                        mockValue = incoming.foobar
                        def reply = new SampleMockedJacksonOutput()
                        reply.foobar = 456
                        reply
                }
            }
            disableContentTypeCheck()
        }

        // act
        def result = runFlow('restRequestContentTypeNotSetForMock') {
            json {
                inputPayload(input,
                             JacksonOutput)
            }
        } as JacksonOutput

        // assert
        assertThat result.result,
                   is(equalTo(457))
        assertThat mockValue,
                   is(equalTo(123))
    }

    @Test
    void queryParameters() {
        // arrange
        Map actualParams = null
        String actualUri = null
        String actualHttpVerb = null
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { HttpRequestInfo requestInfo ->
                    actualParams = requestInfo.queryParams
                    actualUri = requestInfo.uri
                    actualHttpVerb = requestInfo.httpVerb
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
        assert actualUri
        assertThat actualUri,
                   is(equalTo('/some_path/there'))
        assertThat result,
                   is(equalTo([reply_key: 457]))
        assert actualHttpVerb
        assertThat actualHttpVerb,
                   is(equalTo('GET'))
    }

    @Test
    void httpVerb() {
        // arrange
        String actualVerb = null
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { Map incoming,
                                 HttpRequestInfo requestInfo ->
                    actualVerb = requestInfo.httpVerb
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
                                 HttpRequestInfo requestInfo ->
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
    void content_Type_Not_Required_For_get() {
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
                inputPayload([foo: 123])
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
                   is(containsString('Response code 202 mapped as failure'))
    }

    @Test
    void qhttp_return_error_code() {
        // arrange
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith {
                    setHttpReturnCode(500)
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
                   is(containsString('Response code 500 mapped as failure'))
    }

    @Test
    void http_return_error_code() {
        // arrange
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith {
                    setHttpReturnCode(500)
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
        } as MessagingException

        // assert
        assertThat result.message,
                   is(equalTo('Response code 500 mapped as failure.'))
        assertThat result.failingMessageProcessor,
                   is(instanceOf(DefaultHttpRequester))
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
    void queryParameters_Enricher() {
        // arrange
        Map actualParams = null
        String actualUri = null
        String actualHttpVerb = null

        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { HttpRequestInfo requestInfo ->
                    actualParams = requestInfo.queryParams
                    actualUri = requestInfo.uri
                    actualHttpVerb = requestInfo.httpVerb
                    [reply: 456]

                }
            }
        }

        // act
        def result = runFlow('queryParametersEnricher') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assert actualParams
        assertThat actualParams,
                   is(equalTo([stuff: '123']))
        assert actualUri
        assertThat actualUri,
                   is(equalTo('/some_path/there'))
        assertThat result,
                   is(equalTo([reply_key: 457]))
        assert actualHttpVerb
        assertThat actualHttpVerb,
                   is(equalTo('GET'))
    }

    @Test
    void headers() {
        // arrange
        def actualHeaders = null
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { HttpRequestInfo requestInfo ->
                    actualHeaders = requestInfo.headers
                    [reply: 456]
                }
            }
        }

        // act
        runFlow('queryParametersEnricher') {
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
    void queryParameters_ProcessorChain() {
        // arrange
        Map actualParams = null
        String actualUri = null
        String actualHttpVerb = null

        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { HttpRequestInfo requestInfo ->
                    actualParams = requestInfo.queryParams
                    actualUri = requestInfo.uri
                    actualHttpVerb = requestInfo.httpVerb
                    [reply: 456]
                }
            }
        }

        // act
        def result = runFlow('queryParamsNoAnnotationsProcessorChain') {
            json {
                inputPayload([foo: 123])
            }
        }

        // assert
        assert actualParams
        assertThat actualParams,
                   is(equalTo([stuff: '123']))
        assert actualUri
        assertThat actualUri,
                   is(equalTo('/some_path/there'))
        assertThat result,
                   is(equalTo([reply_key: 457]))
        assert actualHttpVerb
        assertThat actualHttpVerb,
                   is(equalTo('GET'))
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
        } as MessagingException

        // assert
        assertThat result.cause,
                   is(instanceOf(ConnectException))
        assertThat result.failingMessageProcessor,
                   is(instanceOf(DefaultHttpRequester))
    }

    @Test
    void httpTimeoutError() {
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
        } as MessagingException

        // assert
        assertThat result.cause,
                   is(instanceOf(TimeoutException))
        assertThat result.failingMessageProcessor,
                   is(instanceOf(DefaultHttpRequester))
    }
}
