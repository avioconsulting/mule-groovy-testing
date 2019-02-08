package com.avioconsulting.mule.testing.invocation

import com.avioconsulting.mule.testing.ConfigTrait
import com.avioconsulting.mule.testing.junit.BaseJunitTest
import com.avioconsulting.mule.testing.muleinterfaces.containers.Dependency
import groovy.json.JsonSlurper
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class FilterDependencyTest extends BaseJunitTest implements
        ConfigTrait {
    List<String> getConfigResources() {
        ['java_test.xml']
    }

    @Override
    List<Dependency> getDependenciesToFilter() {
        //   <dependency>
        //            <groupId>org.mule.connectors</groupId>
        //            <artifactId>mule-wsc-connector</artifactId>
        //            <version>1.2.1</version>
        //            <classifier>mule-plugin</classifier>
        //        </dependency>
        [
                new Dependency('org.mule.connectors',
                               'mule-wsc-connector',
                               '.*',
                               '.*')
        ]
    }

    @Test
    void classloader_model_is_free() {
        // arrange

        // act
        def modelStream = runtimeBridge.appClassloader.getResourceAsStream('META-INF/mule-artifact/classloader-model.json')
        assert modelStream

        // assert
        def map = new JsonSlurper().parse(modelStream)
        def dependencyArtifactIds = map.dependencies.collect { dep ->
            dep.artifactCoordinates.artifactId
        }.findAll { dep ->
            dep == 'mule-wsc-connector'
        } as List<String>
        assertThat dependencyArtifactIds,
                   is(equalTo([]))
    }

    @Test
    void invokes_ok() {
        // arrange
        def input = new SimpleJavaClass().with {
            howdy = '123'
            it
        }

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
