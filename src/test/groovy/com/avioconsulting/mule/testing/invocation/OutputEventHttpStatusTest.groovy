package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.junit.BaseJunitTest
import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.SampleJacksonInput
import org.junit.Test
import org.mule.api.MuleEvent

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class OutputEventHttpStatusTest extends BaseJunitTest implements OverrideConfigList {
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
                inputPayload(input)
            }
            disableContentTypeCheck()
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
                inputPayload(input)
            }
            disableContentTypeCheck()
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
                disableContentTypeCheck()
                withOutputHttpStatus {}
            }
        }

        // assert
        assertThat result.message,
                   is(containsString('No HTTP status was returned from your flow. Did you forget?'))
    }

    @Test
    void nullEvent() {
        // arrange
        def input = new SampleJacksonInput()
        input.foobar = 123

        // act
        runFlow('nullEvent') {
            json {
                inputPayload(input)
            }
            disableContentTypeCheck()
        }
        def result = shouldFail {
            runFlow('nullEvent') {
                json {
                    inputPayload(input)
                }
                disableContentTypeCheck()
                withOutputHttpStatus {}
            }
        }

        // assert
        assertThat result.message,
                   is(containsString(
                           'A null event was returned (filter?) so No HTTP status was returned from your flow. With the real flow, an HTTP status of 200 will usually be set by default so this test is usually not required.'))
    }
}
