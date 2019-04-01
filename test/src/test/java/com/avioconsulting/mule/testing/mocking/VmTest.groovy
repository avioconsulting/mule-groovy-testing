package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.ConfigTrait
import com.avioconsulting.mule.testing.SampleJacksonInput
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class VmTest extends
        BaseJunitTest implements
        ConfigTrait {
    List<String> getConfigResources() {
        ['vm_test.xml']
    }

    @Test
    void mocksProperly_using_vm_shortcut() {
        // arrange
        SampleJacksonInput inputReceived = null
        mockVmReceive('The Queue') {
            json {
                whenCalledWith(SampleJacksonInput) { SampleJacksonInput input ->
                    inputReceived = input
                }
            }
        }

        // act
        runFlow('vmRequest') {
            json {
                def input = new SampleJacksonInput()
                input.foobar = 456
                inputPayload(input)
            }
        }

        // assert
        assert inputReceived
        assertThat inputReceived.foobar,
                   is(equalTo(456))
    }

    @Test
    void mocksProperly_using_generic_style() {
        // arrange
        SampleJacksonInput inputReceived = null
        mockGeneric('The Queue') {
            json {
                whenCalledWith(SampleJacksonInput) { SampleJacksonInput input ->
                    inputReceived = input
                }
            }
        }

        // act
        runFlow('vmRequest') {
            json {
                def input = new SampleJacksonInput()
                input.foobar = 456
                inputPayload(input)
            }
        }

        // assert
        assert inputReceived
        assertThat inputReceived.foobar,
                   is(equalTo(456))
    }
}
