package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.ConfigTrait
import com.avioconsulting.mule.testing.SampleJacksonInput
import com.avioconsulting.mule.testing.SampleJacksonOutput
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.InvokeExceptionWrapper
import groovy.json.JsonSlurper
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class JsonTest extends
        BaseJunitTest implements
        ConfigTrait {
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
                inputPayload(input,
                             SampleJacksonOutput)
            }
        } as SampleJacksonOutput

        // assert
        assertThat result.result,
                   is(equalTo(123))
    }

    @Test
    void non_repeatable_stream() {
        // arrange
        def input = new SampleJacksonInput()
        input.foobar = 123

        // act
        def result = runFlow('non-repeatable-stream-test') {
            json {
                inputPayload(input)
                nonRepeatableStream()
            }
        }

        // assert
        assertThat result,
                   is(nullValue())
    }

    @Test
    void jackson_no_return_type() {
        // arrange

        // act
        def input = new SampleJacksonInput()
        input.foobar = 123
        def result = runFlow('stringResponseTest') {
            json {
                inputPayload(input,
                             String)
            }
        } as String

        // assert
        assertThat result,
                   is(equalTo('stringResponse'))
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
                inputPayload(list,
                             SampleJacksonOutput[])
            }
        } as SampleJacksonOutput[]

        // assert
        assertThat result.length,
                   is(equalTo(1))
        assertThat result[0].result,
                   is(equalTo(123))
    }

    @Test
    void emptyPayload_StringTest() {
        // arrange

        // act
        def result = runFlow('emptyPayloadTest') {
            json {
                inputPayload([:],
                             String)
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

    @Test
    void thrown_fault() {
        // arrange

        // act
        def exception = shouldFail {
            runFlow('jsonTestException') {
                json {
                    inputPayload(null)
                }
            }
        }

        // assert
        assertThat exception,
                   is(instanceOf(InvokeExceptionWrapper))
        // for IDE/syntax complete
        assert exception instanceof InvokeExceptionWrapper
        assertThat exception.cause.getClass().getName(),
                   is(containsString('MessagingException'))
        def asMap = new JsonSlurper().parseText(exception.muleMessage.messageAsString)
        assertThat asMap,
                   is(equalTo([
                           key: 'An error occurred.'
                   ]))
        def event = exception.muleEvent
        assertThat event.getVariable('httpStatus').value,
                   is(equalTo(500))
    }
}
