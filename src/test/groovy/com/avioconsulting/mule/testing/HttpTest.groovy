package com.avioconsulting.mule.testing

import org.junit.Test
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail


class HttpTest extends BaseTest {
    List<String> getConfigResourcesList() {
        ['http_test.xml']
    }

    @Test
    void calledViaMap() {
        // arrange
        def stuff = null
        mockRestHttpCall('SomeSystem Call') {
            post {
                json {
                    whenCalledViaMap { Map incoming ->
                        stuff = incoming
                        [reply: 456]
                    }
                }
            }
        }

        // act
        def result = runMuleFlowWithJsonMap('restRequest', [
                foo: 123
        ])

        // assert
        assertThat stuff,
                   is(equalTo([key: 123]))
        assertThat result,
                   is(equalTo([reply_key: 457]))
    }

    @Test
    void mock2DifferentRequestTypes() {
        // arrange

        // act

        // assert
        fail 'write this'
    }

    @Test
    void mock2DifferentResponseTypes() {
        // arrange

        // act

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
