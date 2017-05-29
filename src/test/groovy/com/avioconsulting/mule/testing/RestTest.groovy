package com.avioconsulting.mule.testing

import org.junit.Test

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class RestTest extends BaseTest {
    List<String> getConfigResourcesList() {
        ['http_test.xml']
    }

    @Test
    void mock_maps() {
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
                inputMap([foo: 123])
            }
        }

        // assert
        assertThat stuff,
                   is(equalTo([key: 123]))
        assertThat result,
                   is(equalTo([reply_key: 457]))
    }

    @Test
    void call_via_jackson() {
        // arrange
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWithMap { Map incoming ->
                    [reply: 456]
                }
            }
        }

        // act
        def input = new SampleJacksonInput()
        input.foobar = 123
        def result = runMuleWithWithJacksonJson('restRequest',
                                                input,
                                                SampleJacksonOutput)

        // assert
        assertThat result.result,
                   is(equalTo(457))
    }

    @Test
    void callVoidViaJackson() {
        // arrange
        def mockCalled = false
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWithMap { Map incoming ->
                    mockCalled = true
                    [reply: 456]
                }
            }
        }

        // act
        def input = new SampleJacksonInput()
        input.foobar = 123
        def result = runMuleWithWithJacksonJson('restRequest',
                                                input)

        // assert
        assertThat result,
                   is(nullValue())
        assertThat mockCalled,
                   is(equalTo(true))
    }

    @Test
    void mock_via_jackson() {
        // arrange
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
        def input = new SampleJacksonInput()
        input.foobar = 123
        def result = runMuleWithWithJacksonJson('restRequest',
                                                input,
                                                SampleJacksonOutput)

        // assert
        assertThat result.result,
                   is(equalTo(457))
        assertThat mockValue,
                   is(equalTo(123))
    }

    @Test
    void noStreaming() {
        // arrange

        // act

        // assert
        fail 'write this'
    }
}
