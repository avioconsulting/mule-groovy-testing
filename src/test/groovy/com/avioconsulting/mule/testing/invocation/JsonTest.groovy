package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.BaseTest
import com.avioconsulting.mule.testing.SampleJacksonInput
import com.avioconsulting.mule.testing.SampleMockedJacksonInput
import com.avioconsulting.mule.testing.SampleMockedJacksonOutput
import org.junit.Test

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class JsonTesting extends BaseTest {
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
}
