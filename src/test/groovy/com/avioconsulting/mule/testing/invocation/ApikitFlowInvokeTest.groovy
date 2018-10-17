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
    @Test
    void getConfigResourceSubstitutes_hasCorrectGlobalXmls() {
        // arrange

        // act
        def result = this.configResourceSubstitutes

        // assert
        assertThat result,
                   is(equalTo(['global.xml': 'global-test.xml']))
    }

    @Test
    void propertySetWithListenerConfig() {
        // arrange

        // act
        def props = getStartUpProperties()

        // assert
        assertThat props.get('http.listener.config') as String,
                   is(equalTo('test-http-listener-config'))
    }

    @Test
    void runApiKitFlow() {
        // arrange
        def input = new SampleJacksonInput()
        input.foobar = 123

        // act
        def result = runApiKitFlow('POST', '/resources') {
            json {
                inputPayload(input, SampleJacksonOutput)
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
            runApiKitFlow('POST', '/resources') {
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
        def result = runApiKitFlow('GET', '/resources/2') {
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

    String getApiNameUnderTest() {
        'the-app'
    }

    String getApiVersionUnderTest() {
        'v1'
    }

    List<String> getConfigResources() {
        [
                'global-test.xml',
                "${fullApiName}.xml".toString()
        ]
    }
}
