package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.BaseJunitTest
import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.SampleJacksonInput
import com.avioconsulting.mule.testing.SampleJacksonOutput
import org.junit.Test
import org.mule.api.MuleEvent

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class WithInputEventTest extends BaseJunitTest implements OverrideConfigList {
    List<String> getConfigResourcesList() {
        ['input_message_test.xml']
    }

    @Test
    void changeFlowVars() {
        // arrange
        def input = new SampleJacksonInput()
        input.foobar = 123

        // act
        def result = runFlow('inputMessageTest') {
            json {
                inputPayload(input, SampleJacksonOutput)
            }

            withInputEvent { MuleEvent inputEvent ->
                inputEvent.setFlowVariable('foo', 123)
            }
        } as SampleJacksonOutput

        // assert
        assertThat result.result,
                   is(equalTo(123))
    }
}
