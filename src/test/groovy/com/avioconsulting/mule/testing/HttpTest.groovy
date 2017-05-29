package com.avioconsulting.mule.testing

import org.junit.Test

class HttpTest extends BaseTest {
    List<String> getConfigResourcesList() {
        ['http_test.xml']
    }

    @Test
    void doStuff() {
        // arrange
        mockRestHttpCall('SomeSystem Call') {
            post {
                json()
            }
        }

        // act
        runMuleFlowWithJsonMap('restRequest', [
                foo: 123
        ])

        // assert
        fail 'write this'
    }

    // TODO: baseTest stuff
    @Test
    void runFlowWithJacksonObject() {
        // arrange

        // act

        // assert
        fail 'write this'
    }
}
