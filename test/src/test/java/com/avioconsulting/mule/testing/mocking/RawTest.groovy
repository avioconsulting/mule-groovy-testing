package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.ConfigTrait
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class RawTest extends
        BaseJunitTest implements
        ConfigTrait {
    List<String> getConfigResources() {
        ['java_test.xml']
    }

    @Test
    void mock_normal() {
        // arrange
        String captured = null
        String actualClassName = null
        mockGeneric('Something to mock') {
            raw {
                whenCalledWith { stuff ->
                    actualClassName = stuff.getClass().name
                    captured = stuff.value
                    'howdy'
                }
            }
        }

        // act
        def result = runFlow('javaMockFlow') {
            java {
                inputPayload('nope')
            }
        } as Map

        // assert
        assertThat result,
                   is(equalTo([
                           key: 'howdy'
                   ]))
        assertThat captured,
                   is(equalTo('nope'))
        assertThat actualClassName,
                   is(equalTo('org.mule.runtime.api.metadata.TypedValue'))
    }
}
