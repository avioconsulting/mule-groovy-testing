package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.BaseTest
import com.avioconsulting.mule.testing.OverrideConfigList
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class BatchInvokeTest extends BaseTest implements OverrideConfigList {
    @Override
    List<String> getConfigResourcesList() {
        ['batch_test.xml']
    }

    @Test
    void canInvoke() {
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
}
