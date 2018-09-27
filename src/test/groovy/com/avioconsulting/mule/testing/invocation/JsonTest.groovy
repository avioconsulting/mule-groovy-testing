package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.SampleJacksonInput
import com.avioconsulting.mule.testing.SampleJacksonOutput
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class JsonTest extends BaseJunitTest implements OverrideConfigList {
    List<String> getConfigResources() {
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
                inputPayload(input, SampleJacksonOutput)
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
                inputPayload(input, String)
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
                inputPayload(input, NoStreamingResponse)
                noStreaming()
            }
        } as NoStreamingResponse

        // assert
        assertThat result.key,
                   is(equalTo('java.lang.String'))
    }

    @Test
    void streaming_disabled_input_only() {
        // arrange

        // act
        def input = new SampleJacksonInput()
        input.foobar = 123
        def result = runFlow('noStreamingTest') {
            json {
                inputOnly(input)
                noStreaming()
            }
        }

        // assert
        assertThat result,
                   is(nullValue())
    }

    @Test
    void streaming_disabled_output_only() {
        // arrange

        // act
        def result = runFlow('noInputTestNoStream') {
            json {
                outputOnly(Map)
                noStreaming()
            }
        }

        // assert
        assertThat result,
                   is(equalTo([key: 123]))
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
                inputPayload([foo: 123])
            }
        }

        // assert
        assertThat result,
                   is(equalTo([key: 123]))
    }

    @Test
    void listOfMaps() {
        // arrange

        // act
        def result = runFlow('jsonListTest') {
            json {
                inputPayload([
                        [foo: 123]
                ])
            }
        } as Map[]

        // assert
        assertThat result.toList(),
                   is(equalTo([
                           [key: 123]
                   ]))
    }

    @Test
    void mapOutputOnly() {
        // arrange

        // act
        def result = runFlow('noInputTest') {
            json {
                outputOnly(Map)
            }
        }

        // assert
        assertThat result,
                   is(equalTo([key: 123]))
    }

    @Test
    void listOfJacksonObjects() {
        // arrange
        def input = new SampleJacksonInput()
        input.foobar = 123
        def list = [input]

        // act
        def result = runFlow('jsonListTest') {
            json {
                inputPayload(list, SampleJacksonOutput[])
            }
        } as SampleJacksonOutput[]

        // assert
        assertThat result.length,
                   is(equalTo(1))
        assertThat result[0].result,
                   is(equalTo(123))
    }

    @Test
    void contentTypeNotSet_CheckDisabled() {
        // arrange

        // act
        def result = runFlow('jsonTest') {
            json {
                inputPayload([foo: 123])
            }
            disableContentTypeCheck()
        }

        // assert
        assertThat result,
                   is(equalTo([key: 123]))
    }

    @Test
    void emptyPayload_StringTest() {
        // arrange

        // act
        def result = runFlow('emptyPayloadTest') {
            json {
                inputPayload([:], String)
            }
        }

        // assert
        assertThat result,
                   is(isEmptyString())
    }

    @Test
    void emptyPayload_MapTest() {
        // arrange

        // act
        def result = runFlow('emptyPayloadTest') {
            json {
                outputOnly(Map)
                noStreaming()
            }
        }

        // assert
        assertThat result,
                   is(nullValue())
    }

    @Test
    void inputOnlyTst() {
        // arrange

        // act
        def result = runFlow('emptyPayloadTest') {
            json {
                inputOnly([:])
            }
        }

        // assert
        assertThat result,
                   is(nullValue())
    }

    @Test
    void stringPayload_Set_Wrong() {
        // arrange

        // act
        def result = shouldFail {
            runFlow('wrongContentTypeTest') {
                json {
                    inputPayload([:], String)
                }
            }
        }

        // assert
        assertThat result.message,
                   is(equalTo(
                           "Expected Content-Type to be of type [text/plain, */*, (not set)] but it actually was application/json. This happened while calling your flow. Ensure your flow's DataWeaves or set-payloads set the mimeType you expect."))
    }

    @Test
    void nullPayloadTest() {
        // arrange

        // act
        def result = runFlow('nullPayloadTest') {
            json {
                inputPayload([:])
            }
        }

        // assert
        assertThat result,
                   is(nullValue())
    }
}
