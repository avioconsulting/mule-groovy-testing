package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.BaseTest
import com.avioconsulting.mule.testing.OverrideConfigList
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class BatchInvokeTest extends BaseTest implements OverrideConfigList {
    @Override
    List<String> getConfigResourcesList() {
        ['batch_test.xml']
    }

    @Test
    void runs_success() {
        // arrange
        def items = (1..3).collect {
            [foo: 123]
        }
        def httpCalls = []

        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { Map incoming ->
                    httpCalls << incoming
                }
            }
        }

        // act
        runBatch('theJob') {
            java {
                inputPayload(items)
            }
        }

        // assert
        assertThat httpCalls.size(),
                   is(equalTo(4))
        assertThat httpCalls[0],
                   is(equalTo([key: 123]))
        assertThat httpCalls[3],
                   is(equalTo([key: -1]))
    }

    @Test
    void runs_failure_in_steps() {
        // arrange
        def items = (1..3).collect {
            [foo: 123]
        }

        def httpCalls = []
        def index = 0
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { Map incoming ->
                    index++
                    if (index <= 3) {
                        httpTimeoutError()
                    } else {
                        httpCalls << incoming
                    }
                }
            }
        }

        // act
        def result = shouldFail {
            runBatch('theJob') {
                java {
                    inputPayload(items)
                }
            }
        }

        // assert
        assertThat result.message,
                   is(containsString('Expected 0 failed batch records but got 3'))
        assertThat httpCalls.size(),
                   is(equalTo(1)) // our complete handler
    }

    @Test
    void runs_onCompleteFails() {
        // arrange
        def items = (1..3).collect {
            [foo: 123]
        }

        def httpCalls = []
        def index = 0
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { Map incoming ->
                    index++
                    if (index > 3) {
                        httpTimeoutError()
                    } else {
                        httpCalls << incoming
                    }
                }
            }
        }

        // act
        def result = shouldFail {
            runBatch('theJob') {
                java {
                    inputPayload(items)
                }
            }
        }

        // assert
        assertThat result.message,
                   is(containsString('onComplete failed!'))
        assertThat httpCalls.size(),
                   is(equalTo(3))
    }

    @Test
    void secondJobCallsFirst_success() {
        // arrange
        def items = (1..3).collect {
            [foo: 123]
        }

        def httpCalls = []
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { Map incoming ->
                    httpCalls << incoming
                }
            }
        }
        
        // act
        runBatch('secondJobCallsFirst', ['theJob']) {
            java {
                inputPayload(items)
            }
        }
        sleep 5000

        // assert
        assertThat httpCalls.size(),
                   is(equalTo(4))
        assertThat httpCalls[0],
                   is(equalTo([key: 123]))
        assertThat httpCalls[3],
                   is(equalTo([key: -1]))
    }
}
