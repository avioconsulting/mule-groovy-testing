package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.BaseTest
import com.avioconsulting.mule.testing.SampleJacksonInput
import com.avioconsulting.mule.testing.SampleJacksonOutput
import org.junit.Test

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class JsonTest extends BaseTest {
    @Test
    void jackson() {
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
        def result = runFlow('restRequest') {
            json {
                jackson(input, SampleJacksonOutput)
            }
        } as SampleJacksonOutput

        // assert
        assertThat result.result,
                   is(equalTo(457))
    }

    @Test
    void jackson_no_return_type() {
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
        def result = runFlow('restRequest') {
            json {
                jackson(input)
            }
        }

        // assert
        assertThat result,
                   is(nullValue())
        assertThat mockCalled,
                   is(equalTo(true))
    }


    @Test
    void noStreaming() {
        // arrange

        // act

        // assert
        fail 'write this'
    }

    @Test
    void noneSpecified() {
        // arrange

        // act

        // assert
        fail 'write this'
    }

    List<String> getConfigResourcesList() {
        ['http_test.xml']
    }

    @Test
    void maps() {
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
}
