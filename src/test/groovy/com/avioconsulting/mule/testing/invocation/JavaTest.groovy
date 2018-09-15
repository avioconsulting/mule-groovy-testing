package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import org.junit.Test
import org.mule.runtime.api.metadata.TypedValue

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class JavaTest extends BaseJunitTest implements OverrideConfigList {
    List<String> getConfigResourcesList() {
        ['java_test.xml']
    }

    @Test
    void javaObject() {
        // arrange
        def input = new SimpleJavaClass()
        input.howdy = '123'
        mockRestHttpCall('Our Request') {
            raw {
                whenCalledWith { mockInput ->
                    println "mock was called with ${mockInput}"
                }
            }
        }

        // act
        def result = runFlow('javaFlow') {
            java {
                inputPayload(input)
            }
        }

        // assert
        assertThat result,
                   is(equalTo([
                           key  : 'nope',
                           value: 'stuff'
                   ]))
    }
}
