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
        def input = new SampleJacksonInput()
        input.foobar = 123

        // act
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
        def result = runFlow('stringResponseTest') {
            json {
                jackson(input)
            }
        } as String

        // assert
        assertThat result,
                   is(equalTo('stringResponse'))
    }

    @Test
    void streaming_disabled() {
        // arrange

        // act
        def input = new SampleJacksonInput()
        input.foobar = 123
        def result = runFlow('noStreamingTest') {
            json {
                jackson(input, NoStreamingResponse)
                noStreaming()
            }
        } as NoStreamingResponse

        // assert
        assertThat result.key,
                   is(equalTo('java.lang.String'))
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

    @Test
    void contentTypeNotSet_NoApiKit() {
        // arrange

        // act
        def result = shouldFail {
            runFlow('jsonTestNoContentType') {
                json {
                    map([foo: 123])
                }
            }
        }

        // assert
        assertThat result.message,
                   is(containsString(
                           "Content-Type was not set to 'application/json' within your flow! Add a set-property"))
    }

    @Test
    void contentTypeNotSet_ApiKit() {
        // arrange

        // act
        def result = runFlow('jsonTest') {
            json {
                map([foo: 123])
            }
            apiKitReferencesThisFlow()
        }

        // assert
        assertThat result,
                   is(equalTo([key: 123]))
    }

    @Test
    void filterPayload() {
        // arrange
        def input = new SampleJacksonInput()
        input.foobar = 123

        // act
        def runIt = {
            runFlow('filterJsonTest') {
                json {
                    jackson(input)
                }
            }
        }
        runIt()
        def result = runIt()

        // assert
        assertThat result,
                   is(nullValue())
    }

    @Test
    void nullPayload() {
        // arrange

        // act
        def result = runFlow('nullJsonTest') {
            json {
                map([:])
            }
        }

        // assert
        assertThat result,
                   is(nullValue())
    }
}
