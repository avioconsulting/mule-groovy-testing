package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class MockFlowTest extends
        BaseJunitTest implements
        OverrideConfigList {
    @Override
    List<String> getConfigResources() {
        ['flow_mock.xml']
    }

    @Test
    void mocks_correctly() {
        // arrange
        mockGeneric('flow2') {
            raw {
                whenCalledWith { input ->
                    'should see this'
                }
            }
        }

        // act
        def result = runFlow('flow1') {
            java {
                inputPayload(null)
            }
        } as String

        // assert
        assertThat result,
                   is(equalTo('should see this'))
    }
}
