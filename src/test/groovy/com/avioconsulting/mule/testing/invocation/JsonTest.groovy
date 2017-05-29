package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.BaseTest
import com.avioconsulting.mule.testing.SampleJacksonInput
import com.avioconsulting.mule.testing.SampleJacksonOutput
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class JsonTest extends BaseTest {
    List<String> getConfigResourcesList() {
        ['simple_json_test.xml']
    }

    @Test
    void jackson() {
        // arrange

        // act
        def input = new SampleJacksonInput()
        input.foobar = 123
        def result = runFlow('jsonTest') {
            json {
                jackson(input, SampleJacksonOutput)
            }
        } as SampleJacksonOutput

        // assert
        assertThat result.result,
                   is(equalTo(123))
    }

    @Test
    void jackson_no_return_type() {
        // arrange

        // act
        def input = new SampleJacksonInput()
        input.foobar = 123
        def result = runFlow('jsonTest') {
            json {
                jackson(input)
            }
        }

        // assert
        assertThat result,
                   is(nullValue())
    }


    @Test
    void noStreaming() {
        // arrange

        // act

        // assert
        fail 'write this'
    }

    @Test
    void no_serialization_specified() {
        // arrange

        // act
        shouldFail {
            runFlow('jsonTest') {
                json {
                }
            }
        }
    }

    @Test
    void maps() {
        // arrange

        // act
        def result = runFlow('jsonTest') {
            json {
                map([foo: 123])
            }
        }

        // assert
        assertThat result,
                   is(equalTo([key: 123]))
    }
}
