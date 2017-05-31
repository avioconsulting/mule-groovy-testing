package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.BaseTest
import com.avioconsulting.mule.testing.SampleJacksonInput
import org.junit.Test
import org.mule.api.MuleEvent

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class OutputEventHttpStatusTest extends BaseTest {
    List<String> getConfigResourcesList() {
        ['http_test.xml']
    }

    @Test
    void getAccessToEvent() {
        // arrange
        MuleEvent saveOutput = null
        def input = new SampleJacksonInput()
        input.foobar = 123

        // act
        runFlow('hasHttpStatus') {
            json {
                jackson(input)
            }
            withOutputEvent { MuleEvent output ->
                saveOutput = output
            }
        }

        // assert
        assert saveOutput
        assertThat saveOutput.message.getOutboundProperty('http.status'),
                   is(equalTo('201'))
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
                jackson(input)
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
                    jackson(input)
                }
                withOutputHttpStatus {}
            }
        }

        // assert
        assertThat result.message,
                   is(containsString('No HTTP status was returned from your flow. Did you forget?'))
    }
}
