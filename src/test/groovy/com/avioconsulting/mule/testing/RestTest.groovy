package com.avioconsulting.mule.testing

import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail

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
                whenCalledViaMap { Map incoming ->
                    stuff = incoming
                    [reply: 456]
                }
            }
        }

        // act
        def result = runMuleFlowWithJsonMap('restRequest', [
                foo: 123
        ])

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
                whenCalledViaMap { Map incoming ->
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
    void mock_via_jackson() {
        // arrange

        // act

        // assert
        fail 'write this'
    }
}
