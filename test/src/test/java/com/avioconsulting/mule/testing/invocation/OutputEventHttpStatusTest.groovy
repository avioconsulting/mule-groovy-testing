package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.ConfigTrait
import com.avioconsulting.mule.testing.SampleJacksonInput
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class OutputEventHttpStatusTest extends
        BaseJunitTest implements
        ConfigTrait {
    List<String> getConfigResources() {
        ['http_test.xml']
    }

    @Test
    void getAccessToEvent() {
        // arrange
        EventWrapper saveOutput = null
        def input = new SampleJacksonInput()
        input.foobar = 123

        // act
        runFlow('hasHttpStatus') {
            json {
                inputPayload(input)
            }
            withOutputEvent { EventWrapper output ->
                saveOutput = output
            }
        }

        // assert
        assert saveOutput
        assertThat saveOutput.getVariable('httpStatus').value,
                   is(equalTo(201))
    }

    @Test
    void getAccessToHttpStatus() {
        // arrange
        Integer httpStatus = null
        def input = new SampleJacksonInput()
        input.foobar = 123

        // act
        runFlow('hasHttpStatus') {
            json {
                inputPayload(input)
            }
            withOutputHttpStatus { Integer status ->
                httpStatus = status
            }
        }

        // assert
        assert httpStatus
        assertThat httpStatus,
                   is(equalTo(201))
    }

    @Test
    void noHttpStatus() {
        // arrange
        def input = new SampleJacksonInput()
        input.foobar = 123

        // act
        def result = shouldFail {
            runFlow('noHttpStatus') {
                json {
                    inputPayload(input)
                }
                withOutputHttpStatus {}
            }
        }

        // assert
        assertThat result.message,
                   is(containsString('No HTTP status was returned from your flow in the httpStatus variable. Did you forget?'))
    }
}
