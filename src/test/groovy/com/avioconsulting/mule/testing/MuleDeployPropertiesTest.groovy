package com.avioconsulting.mule.testing

import com.avioconsulting.mule.testing.junit.BaseJunitTest
import groovy.json.JsonSlurper
import org.junit.Before
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

class MuleDeployPropertiesTest extends
        BaseJunitTest {
    Map getMuleArtifactJson() {
        def file = new File('src/test/resources/mule-deploy-props-test-artifact.json')
        assert file.exists(): "Could not find ${file}. Has the Mule Maven plugin built your project yet. If you are not going to create this file, override getMuleArtifactJson"
        new JsonSlurper().parse(file) as Map
    }

    @Override
    Map<String, String> getConfigResourceSubstitutes() {
        [
                'simple_json_test.xml': 'java_test.xml',
                'foo.xml'             : null,
                'bar.xml'             : null
        ]
    }

    @Before
    void startMule() {
        // don't actually start anything
    }

    @Test
    void correct_resources() {
        // arrange

        // act
        def result = this.configResources

        // assert
        assertThat result,
                   is(equalTo([
                           'java_test.xml',
                           'http_test.xml'
                   ]))
    }

    @Test
    void artifact_descriptor_includes_them() {
        // arrange

        // act
        def artifact = getMuleArtifactJson()

        // assert
        fail 'write the test'
    }
}
