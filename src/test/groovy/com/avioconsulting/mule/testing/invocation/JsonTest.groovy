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
        }

        // assert
        assertThat result,
                   is(equalTo([
                           [key: 123]
                   ]))
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
    void contentTypeNotSet() {
        // arrange

        // act
        def result = shouldFail {
            runFlow('jsonTestNoContentType') {
                json {
                    inputPayload([foo: 123])
                }
            }
        }

        // assert
        assertThat result.message,
                   is(containsString(
                           "Content-Type was not set to 'application/json' within your flow! Add a set-property"))
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
    void filterPayload() {
        // arrange
        def input = new SampleJacksonInput()
        input.foobar = 123

        // act
        def runIt = {
            runFlow('filterJsonTest') {
                json {
                    inputPayload(input)
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
    void emptyPayloadTest() {
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
                           "Content-Type was not set to 'text/plain' within your flow! Add a set-property or remove the incorrect type.. Actual type was application/json. Expression: (actualContentType == text/plain). Values: actualContentType = application/json"))
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
