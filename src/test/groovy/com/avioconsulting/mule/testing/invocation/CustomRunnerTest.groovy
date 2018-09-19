package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import com.avioconsulting.mule.testing.junit.MuleGroovyParameterizedRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(MuleGroovyParameterizedRunner)
class CustomRunnerTest extends BaseJunitTest implements OverrideConfigList {
    @Parameterized.Parameters(name = "{0}")
    static Collection<Object[]> testData() {
        [
                ['foo']
        ].collect { list ->
            list.toArray(new Object[0])
        }
    }

    CustomRunnerTest(String ignored) {
    }

    List<String> getConfigResources() {
        ['java_test.xml']
    }

    @Test
    void foo() {
        // arrange
        def input = new SimpleJavaClass()
        input.howdy = '123'

        // act
        runFlow('javaFlow') {
            java {
                inputPayload(input)
            }
        }

        // assert
    }
}
