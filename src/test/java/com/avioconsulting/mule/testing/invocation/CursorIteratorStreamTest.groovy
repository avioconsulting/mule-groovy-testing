package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import org.junit.Test

class CursorIteratorStreamTest extends
        BaseJunitTest implements
        OverrideConfigList {
    List<String> getConfigResources() {
        ['interceptor_classloader_test.xml']
    }

    @Test
    void returns_ok() {
        // arrange

        // act

        // assert
        fail 'write the test'
    }

}
