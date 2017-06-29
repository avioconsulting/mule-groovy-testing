package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.BaseTest
import com.avioconsulting.mule.testing.SampleJacksonInput
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class JsonMockingTest extends BaseTest {
    List<String> getConfigResourcesList() {
        ['http_test.xml']
    }

    @Test
    void mockViaMap() {
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
    void queryParams_returns_map() {
        // arrange
        Map actualParams = null
        String actualUri = null
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith {
                    withHttpOptions { String httpVerb, String uri, Map queryParams ->
                        actualParams = queryParams
                        actualUri = uri
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
    }

    @Test
    void queryParams_returns_jackson_object() {
        // arrange
        Map actualParams = null
        String actualUri = null

        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith {
                    withHttpOptions { String httpVerb, String uri, Map queryParams ->
                        actualParams = queryParams
                        actualUri = uri
                        def reply = new SampleMockedJacksonOutput()
                        reply.foobar = 456
                        reply
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
    }

    @Test
    void mock_via_jackson() {
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
        def result = runFlow('restRequest') {
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
}