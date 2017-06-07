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
                whenCalledWithMap { Map incoming ->
                    stuff = incoming
                    [reply: 456]
                }
            }
        }

        // act
        def result = runFlow('restRequest') {
            json {
                map([foo: 123])
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
                whenCalledWithJackson(SampleMockedJacksonInput) {
                    SampleMockedJacksonInput incoming ->
                }
            }
        }

        // act
        def result = shouldFail {
            runFlow('restRequestContentTypeNotSet') {
                json {
                    jackson(input, JacksonOutput)
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
                whenCalledWithJackson(SampleMockedJacksonInput) {
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
                jackson(input, JacksonOutput)
            }

            apiKitReferencesThisFlow()
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
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWithQueryParams { Map queryParams, String uri ->
                    actualParams = queryParams
                    actualUri = uri
                    [reply: 456]
                }
            }
        }

        // act
        def result = runFlow('queryParameters') {
            json {
                map([foo: 123])
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
}
