package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.BaseApikitTest
import com.avioconsulting.mule.testing.SampleJacksonInput
import com.avioconsulting.mule.testing.SampleJacksonOutput
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class ApikitFlowInvokeTest extends BaseApikitTest {
    @Test
    void getHttpPort_firstPortOpen() {
        // arrange

        // act
        def port = getHttpPort()

        // assert
        assertThat port,
                   is(equalTo(8088))
    }

    @Test
    void getHttpPort_secondPortOpen() {
        // arrange
        def serverSocket = new ServerSocket(8088)
        try {

            // act
            def port = getHttpPort()

            // assert
            assertThat port,
                       is(equalTo(8089))
        }
        finally {
            serverSocket.close()
        }
    }

    @Test
    void propertySetWithHttpPort() {
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
        def result = runApiKitFlow {
            json {
                jackson(input, SampleJacksonOutput)
            }
        } as SampleJacksonOutput

        // assert
        assertThat result.result,
                   is(equalTo(123))
    }

    protected String getApiNameUnderTest() {
        'the-app'
    }

    protected String getApiVersionUnderTest() {
        'v1'
    }
}
