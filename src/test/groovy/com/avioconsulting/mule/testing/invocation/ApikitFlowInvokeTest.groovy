package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.BaseApiKitTest
import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.SampleJacksonInput
import com.avioconsulting.mule.testing.SampleJacksonOutput
import org.apache.tools.ant.taskdefs.condition.Os
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class ApikitFlowInvokeTest extends BaseApiKitTest implements OverrideConfigList {
    static int getExpectedPort() {
        // not sure why 8088 is occupied on Windows (nothing shows as listening on that port) but
        // took care of test once I changed this
        // where it's 8088 or 8089 for the first port is not important. What is import is
        // that the 2nd port is used if the first port is taken and that will be 8090 for Windows
        // see getHttpPort_secondPortOpen
        Os.isFamily(Os.FAMILY_WINDOWS) ? 8089 : 8088
    }

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
    void getHttpPort_firstPortOpen() {
        // arrange

        // act
        def port = getHttpPort()

        // assert
        assertThat port,
                   is(equalTo(expectedPort))
    }

    @Test
    void getHttpPort_secondPortOpen() {
        // arrange
        def serverSocket = new ServerSocket(expectedPort)
        try {

            // act
            def port = getHttpPort()

            // assert
            assertThat port,
                       is(equalTo(expectedPort+1))
        }
        finally {
            serverSocket.close()
        }
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

    protected String getApiNameUnderTest() {
        'the-app'
    }

    protected String getApiVersionUnderTest() {
        'v1'
    }

    List<String> getConfigResourcesList() {
        [
                'global-test.xml',
                "${fullApiName}.xml".toString()
        ]
    }
}
