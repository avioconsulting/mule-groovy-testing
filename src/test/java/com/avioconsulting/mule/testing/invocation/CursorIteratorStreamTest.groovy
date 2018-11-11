package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

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
        List<String> result = null
        runFlow('cursorIteratorStreamTest') {
            java {
                inputPayload(null)
            }
            withOutputEvent { EventWrapper event ->
                result = event.message.messageIteratorAsList
            }
        }

        // assert
        assertThat result,
                   is(equalTo(['item1', 'item2']))
    }
}
