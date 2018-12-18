package com.avioconsulting.mule.testing.invocation


import com.avioconsulting.mule.testing.ConfigTrait
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class PropertiesOverrideTest extends
        BaseJunitTest implements
        ConfigTrait {
    List<String> getConfigResources() {
        ['properties_override_test.xml']
    }

    @Override
    Map getStartUpProperties() {
        [
                howdy: 123
        ]
    }

    @Test
    void convertsToString() {
        // arrange
        def input = new SimpleJavaClass()
        input.howdy = '123'

        // act
        def result = runFlow('propertiesFlow') {
            java {
                inputPayload(input)
            }
        }

        // assert
        assertThat result,
                   is(equalTo([
                           key  : '123',
                           value: '123'
                   ]))
    }
}
