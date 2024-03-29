package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.ConfigTrait
import com.avioconsulting.mule.testing.SampleJacksonInput
import com.avioconsulting.mule.testing.SampleJacksonOutput
import com.avioconsulting.mule.testing.junit.BaseApiKitTest
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class ApikitFlowInvokeTest extends
        BaseApiKitTest implements
        ConfigTrait {
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
                   is(containsString('required key [foo] not found'))
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
                outputOnly(Map)
            }
        } as Map

        // assert
        assertThat result.key,
                   is(equalTo(133))
        assertThat result.requestUri,
                   is(equalTo('/the-APP/api/v1/resources?foo=123&bar=10'))
    }

    @Test
    void runApiKitFlow_headers() {
        // arrange

        // act
        def result = runApiKitFlow('GET',
                                   '/resourceforheader',
                                   null,
                                   [
                                           foo: 'nope'
                                   ]) {
            json {
                outputOnly(Map)
            }
        } as Map

        // assert
        assertThat result,
                   is(equalTo([
                           key: 'nope'
                   ]))
    }
}
