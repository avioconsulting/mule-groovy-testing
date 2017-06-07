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
                   is(equalTo(8081))
    }

    @Test
    void getHttpPort_secondPortOpen() {
        // arrange

        // act

        // assert
        fail 'write this'
    }

    @Test
    void propertySetWithHttpPort() {
        // arrange

        // act

        // assert
        fail 'write this'
    }

    @Test
    void runApiKitFlow() {
        // arrange

        // act

        // assert
        fail 'write this'
    }

    List<String> getConfigResourcesList() {
        []
    }
}
