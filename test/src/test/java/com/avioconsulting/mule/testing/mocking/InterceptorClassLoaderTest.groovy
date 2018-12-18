package com.avioconsulting.mule.testing.mocking

import com.avioconsulting.mule.testing.ConfigTrait
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import com.avioconsulting.mule.testing.muleinterfaces.wrappers.EventWrapper
import org.junit.Test

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class InterceptorClassLoaderTest extends
        BaseJunitTest implements
        ConfigTrait {
    List<String> getConfigResources() {
        ['interceptor_classloader_test.xml']
    }

    @Test
    void proper_classloader_interceptor() {
        // arrange

        // act
        List<String> result = null
        runFlow('classLoaderTest') {
            java {
                inputPayload(null)
            }
            withOutputEvent { EventWrapper event ->
                result = event.message.messageIteratorAsList
            }
        }

        // assert
        assertThat result.size(),
                   is(equalTo(3))
        assertThat result[0],
                   is(equalTo('our debug enabled true'))
        assertThat result[1],
                   is(startsWith('the classloader before paging is org.mule.runtime.module.artifact.api.classloader.MuleArtifactClassLoader[domain/default/app/tests-for-the-test'))
        assertThat result[2],
                   is(startsWith('the classloader now is org.mule.runtime.deployment.model.internal.application.MuleApplicationClassLoader[domain/default/app/tests-for-the-test'))
    }
}
