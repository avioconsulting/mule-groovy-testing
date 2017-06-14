package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.BaseTest
import com.avioconsulting.mule.testing.SampleJacksonInput
import org.junit.Test

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
    void contentTypeNotSet_NoApiKit() {
        // arrange
        def input = new SampleJacksonInput()
        input.foobar = 123
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith(SampleMockedJacksonInput) {
                    SampleMockedJacksonInput incoming ->
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
                           "Content-Type was not set to 'application/json' before calling your mock endpoint! Add a set-property"))
    }

    @Test
    void contentTypeNotSet_ApiKit() {
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
}
