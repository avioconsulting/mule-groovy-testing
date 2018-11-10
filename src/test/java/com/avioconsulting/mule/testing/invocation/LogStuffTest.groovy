package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class LogStuffTest extends
        BaseJunitTest implements
        OverrideConfigList {
    List<String> getConfigResources() {
        ['log_stuff.xml']
    }

    @Test
    void debugs_right() {
        // arrange

        // act
        def result = runFlow('listenerFlow') {
            java {
                inputPayload(null)
            }
        }

        // assert
        assertThat result,
                   is(equalTo(true))
    }
}
