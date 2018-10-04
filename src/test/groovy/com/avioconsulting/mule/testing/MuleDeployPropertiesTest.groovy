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
    @Override
    File getMuleArtifactPath() {
        new File('src/test/resources/mule-deploy-props-test-artifact.json')
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
        def artifactConfigs = getMuleArtifactJson().configs as List<String>

        // assert
        assertThat artifactConfigs,
                   is(equalTo([
                           'java_test.xml',
                           'http_test.xml'
                   ]))
    }
}
