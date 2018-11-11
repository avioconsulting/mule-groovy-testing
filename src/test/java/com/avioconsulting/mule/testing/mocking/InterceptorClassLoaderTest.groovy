package com.avioconsulting.mule.testing.mocking


import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class InterceptorClassLoaderTest extends
        BaseJunitTest implements
        OverrideConfigList {
    List<String> getConfigResources() {
        ['interceptor_classloader_test.xml']
    }

    @Test
    void proper_classloader_interceptor() {
        // arrange

        // act
        Object result
        runFlow('classLoaderTest') {
            java {
                inputPayload(null)
            }
            withOutputEvent { EventWrapper event ->
                result = event.messageAsString
            }
        }

        // assert
        assertThat result,
                   is(equalTo(true))
    }
}
