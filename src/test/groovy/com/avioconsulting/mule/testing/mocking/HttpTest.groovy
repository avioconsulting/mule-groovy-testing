package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.SampleJacksonInput
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import com.avioconsulting.mule.testing.mulereplacements.wrappers.MessageWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.ReturnWrapper
import com.avioconsulting.mule.testing.mulereplacements.wrappers.connectors.HttpRequesterInfo
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j2
import org.junit.Test

import java.util.concurrent.TimeoutException

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

@Log4j2
class HttpTest extends
        BaseJunitTest implements
        OverrideConfigList {
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
    void mocksProperly_raw() {
        // arrange
        MessageWrapper stuff = null
        mockRestHttpCall('SomeSystem Call') {
            raw {
                whenCalledWith { MessageWrapper incoming ->
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
        assertThat new JsonSlurper().parseText(stuff.messageAsString),
                   is(equalTo([key: 123]))
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
        }

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
        }

        // assert
        assertThat result.cause,
                   is(instanceOf(TimeoutException))
        assertThat result.failingMessageProcessor,
                   is(instanceOf(DefaultHttpRequester))
    }
}
