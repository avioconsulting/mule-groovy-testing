package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.ConfigTrait
import com.avioconsulting.mule.testing.SampleJacksonInput
import com.avioconsulting.mule.testing.SampleJacksonOutput
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.CoreMatchers.containsString
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class GenericFlowInvokeTest extends
        BaseJunitTest implements
        ConfigTrait {
    List<String> getConfigResources() {
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
    void flow_stop_start() {
        // arrange
        def input = new SampleJacksonInput()
        input.foobar = 123
        def flow = runtimeBridge.getFlow('jsonTest')
        flow.stop()

        // act
        def exception = shouldFail {
            runFlow('jsonTest') {
                json {
                    inputPayload(input,
                                 SampleJacksonOutput)
                }
            }
        }

        // assert
        assertThat exception.message,
                   is(containsString('Cannot process event as "jsonTest" is stopped'))
        flow.start()
        runFlow('jsonTest') {
            json {
                inputPayload(input,
                             SampleJacksonOutput)
            }
        }
    }
}
