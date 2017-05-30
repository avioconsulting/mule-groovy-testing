package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.BaseTest
import com.avioconsulting.mule.testing.SampleJacksonInput
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat


class VmTest extends BaseTest {
    List<String> getConfigResourcesList() {
        ['vm_test.xml']
    }

    @Test
    void mocksProperly_Already_String() {
        // arrange
        SampleJacksonInput inputReceived = null
        mockVmReceive('The Queue') {
            json {
                whenCalledWithJackson(SampleJacksonInput) { SampleJacksonInput input ->
                    inputReceived = input
                }
            }
        }

        // act
        runFlow('vmRequest') {
            json {
                def input = new SampleJacksonInput()
                input.foobar = 456
                jackson(input)
            }
        }

        // assert
        assert inputReceived
        assertThat inputReceived.foobar,
                   is(equalTo(456))
    }

    @Test
    void mock_gives_good_error_not_string() {
        // arrange
        mockVmReceive('The Queue') {
            json {
                whenCalledWithJackson(SampleJacksonInput) { SampleJacksonInput input ->
                }
            }
        }

        // act
        def exception = shouldFail {
            runFlow('vmRequest_NotString') {
                json {
                    def input = new SampleJacksonInput()
                    input.foobar = 456
                    jackson(input)
                }
            }
        }

        // assert
        assertThat exception.message,
                   is(equalTo('Expected payload to be of type class java.lang.String here but it actually was class java.io.ByteArrayInputStream. Check the connectors you\'re mocking and make sure you transformed the payload properly! (e.g. payload into VMs must be Strings) (java.lang.Exception).'))
    }
}
