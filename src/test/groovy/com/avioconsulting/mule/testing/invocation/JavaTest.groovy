package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.BaseTest
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class JavaTest extends BaseTest {
    List<String> getConfigResourcesList() {
        ['java_test.xml']
    }

    @Test
    void javaObject() {
        // arrange
        def input = new SimpleJavaClass()
        input.howdy = '123'

        // act
        def result = runFlow('javaFlow') {
            java {
                inputPayload(input)
            }
        }

        // assert
        assertThat result,
                   is(equalTo([
                           key  : '123',
                           value: SimpleJavaClass.name
                   ]))
    }
}
