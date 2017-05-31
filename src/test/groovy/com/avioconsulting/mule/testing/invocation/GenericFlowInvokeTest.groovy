package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.BaseTest
import com.avioconsulting.mule.testing.SampleJacksonInput
import org.junit.Test
import org.mule.api.MuleEvent

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class GenericFlowInvokeTest extends BaseTest {
    List<String> getConfigResourcesList() {
        ['simple_json_test.xml']
    }

    @Test
    void no_format_specified() {
        // arrange

        // act
        shouldFail {
            runFlow('jsonTest') {
            }
        }
    }

    @Test
    void getAccessToEvent() {
        // arrange
        MuleEvent saveOutput = null

        // act
        def input = new SampleJacksonInput()
        input.foobar = 123
        runFlow('jsonTest') {
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
}
