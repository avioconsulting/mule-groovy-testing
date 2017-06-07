package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.BaseTest
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail

class ApikitFlowInvokeTest extends BaseTest {
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
        assertThat props.get('http.port') as String,
                   is(equalTo('8088'))
    }

    @Test
    void runApiKitFlow() {
        // arrange

        // act

        // assert
        fail 'write this'
    }

    @Override
    protected boolean enableApiKitFlows() { true }

    List<String> getConfigResourcesList() {
        []
    }
}
