package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.OverrideConfigList
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class DomainTest extends
        BaseJunitTest implements
        OverrideConfigList {
    @Override
    Map getClassLoaderModel() {
        def model = super.getClassLoaderModel()
        // domains now come solely from Maven. Mule's maven plugin puts this in
        // the classloader model
        model.dependencies << [
                artifactCoordinates: [
                        groupId   : 'some-group',
                        artifactId: 'some-domain',
                        version   : '1.0.0',
                        type      : 'jar',
                        classifier: 'mule-domain',
                        scope     : 'provided'
                ],
                uri                : ''
        ]
        model
    }

    List<String> getConfigResources() {
        ['java_test.xml']
    }

    @Test
    void invokes_loads_ok() {
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
