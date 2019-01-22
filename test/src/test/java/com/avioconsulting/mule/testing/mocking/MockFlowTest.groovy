package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.ConfigTrait
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class MockFlowTest extends
        BaseJunitTest implements
        ConfigTrait {
    @Override
    List<String> getConfigResources() {
        ['flow_mock.xml']
    }

    @Test
    void mocks_correctly() {
        // arrange
        def actualInput = null
        mockGeneric('flow2') {
            raw {
                whenCalledWith { input ->
                    actualInput = input
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
        assertThat actualInput.value,
                   is(equalTo('the payload'))
    }
}
