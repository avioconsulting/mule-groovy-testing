package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.BaseTest
import com.avioconsulting.mule.testing.SampleJacksonInput
import com.mulesoft.weave.reader.ByteArraySeekableStream
import org.junit.Test
import org.mule.api.MessagingException
import org.mule.module.http.internal.request.DefaultHttpRequester

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class HttpTest extends BaseTest {
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
                    inputPayload(input, JacksonOutput)
                }
            }
        }

        // assert
        assertThat result.message,
                   is(containsString(
                           "Expected Content-Type to be of type [application/json, application/json;charset=UTF-8] but it actually was null. This happened while calling your flow. Add a set-property before the end of the flow."))
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
                inputPayload(input, JacksonOutput)
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
                    inputPayload(input, JacksonOutput)
                }
            }
        }

        // assert
        assertThat result.message,
                   is(containsString(
                           "Expected Content-Type to be of type [application/json, application/json;charset=UTF-8] but it actually was null. Check your mock endpoints."))
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
                inputPayload(input, JacksonOutput)
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
                whenCalledWith {
                    withHttpOptions { String httpVerb, String uri, Map queryParams ->
                        actualParams = queryParams
                        actualUri = uri
                        actualHttpVerb = httpVerb
                        [reply: 456]
                    }
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
                whenCalledWith { Map incoming ->
                    withHttpOptions { String httpVerb, String uri, Map queryParams ->
                        actualVerb = httpVerb
                        [reply: 456]
                    }
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

    class Dummy {

    }

    @Test
    void http_get_ignores_payload() {
        def input = new Dummy()
        // arrange
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith {
                    withHttpOptions { String httpVerb, String uri, Map queryParams ->
                        [reply: 456]
                    }
                }
            }
        }

        // act
        def result = runFlow('restRequestGet') {
            // using Java to surface attempts to deserialize this payload, etc.
            java {
                inputPayload(input)
            }
        } as ByteArraySeekableStream

        // assert
        assert result
    }

    @Test
    void content_Type_Not_Required_For_get() {
        // arrange
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith {
                    withHttpOptions { String httpVerb, String uri, Map queryParams ->
                        [reply: 456]
                    }
                }
            }
        }

        // act
        def result = runFlow('restRequestGet') {
            // using Java to surface attempts to deserialize this payload, etc.
            java {
                inputPayload([foo: 123])
            }
        } as ByteArraySeekableStream

        // assert
        assert result
    }

    @Test
    void queryParameters_http_return_set_201_code() {
        // arrange
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith {
                    withHttpOptions { String httpVerb, String uri, Map queryParams ->
                        setHttpReturnCode(201)
                        [reply: 456]
                    }
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
    void queryParameters_http_return_error_code_custom() {
        // arrange
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith {
                    withHttpOptions { String httpVerb, String uri, Map queryParams ->
                        setHttpReturnCode(202)
                        [reply: 456]
                    }
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
    void queryParameters_http_return_error_code() {
        // arrange
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith {
                    withHttpOptions { String httpVerb, String uri, Map queryParams ->
                        setHttpReturnCode(500)
                        [reply: 456]
                    }
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
                   is(containsString('Response code 500 mapped as failure'))
    }

    @Test
    void queryParameters_Enricher() {
        // arrange
        Map actualParams = null
        String actualUri = null
        String actualHttpVerb = null

        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith {
                    withHttpOptions { String httpVerb, String uri, Map queryParams ->
                        actualParams = queryParams
                        actualUri = uri
                        actualHttpVerb = httpVerb
                        [reply: 456]
                    }
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
}
