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

        mockRestHttpCall('SomeSystem Call from Complete') {
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
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { Map incoming ->
                    httpTimeoutError()
                }
            }
        }

        mockRestHttpCall('SomeSystem Call from Complete') {
            json {
                whenCalledWith { Map incoming ->
                    httpCalls << incoming
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
                   is(containsString(
                           'Expected no failed job instances but got [Job: theJob, failed records: 3 onComplete fail: false]'))
        assertThat httpCalls.size(),
                   is(equalTo(1)) // our complete handler
    }

    @Test
    void suppress_batch_failure() {
        // arrange
        def items = (1..3).collect {
            [foo: 123]
        }

        def httpCalls = []
        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { Map incoming ->
                    httpTimeoutError()
                }
            }
        }

        mockRestHttpCall('SomeSystem Call from Complete') {
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
        fail 'write this'
    }

    @Test
    void runs_onCompleteFails() {
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

        mockRestHttpCall('SomeSystem Call from Complete') {
            json {
                whenCalledWith { Map incoming ->
                    httpTimeoutError()
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
                   is(containsString(
                           'Expected no failed job instances but got [Job: theJob, failed records: 0 onComplete fail: true]'))
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

        mockRestHttpCall('SomeSystem Call from Complete') {
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

        // assert
        assertThat httpCalls.size(),
                   is(equalTo(4))
        assertThat httpCalls[0],
                   is(equalTo([key: 123]))
        assertThat httpCalls[3],
                   is(equalTo([key: -1]))
    }

    @Test
    void secondJobCallsFirst_use_default_wait_list() {
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

        mockRestHttpCall('SomeSystem Call from Complete') {
            json {
                whenCalledWith { Map incoming ->
                    httpCalls << incoming
                }
            }
        }

        // act
        runBatch('secondJobCallsFirst') {
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
    void secondJobCallsFirst_second_fails() {
        // arrange
        def items = (1..3).collect {
            [foo: 123]
        }

        mockRestHttpCall('SomeSystem Call') {
            json {
                whenCalledWith { Map incoming ->
                    httpTimeoutError()
                }
            }
        }

        mockRestHttpCall('SomeSystem Call from Complete') {
            json {
                whenCalledWith { Map incoming ->
                }
            }
        }

        // act
        def result = shouldFail {
            runBatch('secondJobCallsFirst', ['theJob']) {
                java {
                    inputPayload(items)
                }
            }
        }

        // assert
        assertThat result.message,
                   is(containsString(
                           'Expected no failed job instances but got [Job: theJob, failed records: 3 onComplete fail: false]'))
    }
}
