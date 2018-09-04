package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

class GenericFlowInvokeTest extends BaseJunitTest implements OverrideConfigList {
    List<String> getConfigResourcesList() {
        ['simple_json_test.xml']
    }

    @Test
    void no_format_specified() {
        // arrange

        // act
        shouldFail {
            runFlow('jsonTest') {
            }
        }
    }
}
