package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.SampleJacksonInput
import com.avioconsulting.mule.testing.SampleJacksonOutput
import com.avioconsulting.mule.testing.junit.BaseApiKitTest
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class ApikitFlowInvokeTest extends
        BaseApiKitTest implements
        OverrideConfigList {
    String getApiNameUnderTest() {
        'the-APP'
    }

    String getApiVersionUnderTest() {
        'v1'
    }

    List<String> getConfigResources() {
        [
                'api-the-app-v1.xml'
        ]
    }

    @Test
    void runApiKitFlow() {
        // arrange
        def input = new SampleJacksonInput()
        input.foobar = 123

        // act
        def result = runApiKitFlow('POST',
                                   '/resources') {
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
    void runApiKitFlow_validates() {
        // arrange

        // act
        def result = shouldFail {
            runApiKitFlow('POST',
                          '/resources') {
                json {
                    inputPayload([howdy: 123])
                }
            }
        }

        // assert
        assertThat result.message,
                   is(containsString('Missing required field'))
    }

    @Test
    void runApiKitFlow_Get_single() {
        // arrange

        // act
        def result = runApiKitFlow('GET',
                                   '/resources/2') {
            json {
                outputOnly(SampleJacksonOutput)
            }
        } as SampleJacksonOutput

        // assert
        assertThat result.result,
                   is(equalTo(123))
    }

    @Test
    void runApiKitFlow_Get_Query_Params() {
        // arrange
        def queryParams = [
                foo: 123,
                bar: 10
        ]

        // act
        def result = runApiKitFlow('GET',
                                   '/resources',
                                   queryParams) {
            json {
                outputOnly(SampleJacksonOutput)
            }
        } as SampleJacksonOutput

        // assert
        assertThat result.result,
                   is(equalTo(133))
    }
}
