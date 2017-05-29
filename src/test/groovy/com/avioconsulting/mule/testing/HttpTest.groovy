package com.avioconsulting.mule.testing

import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
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
            json {
                whenCalledViaMap { Map incoming ->
                    stuff = incoming
                    [reply: 456]
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
    
    // TODO: baseTest stuff
    @Test
    void runFlowWithJacksonObject() {
        // arrange

        // act

        // assert
        fail 'write this'
    }
}
