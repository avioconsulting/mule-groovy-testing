package com.avioconsulting.mule.testing

import groovy.util.logging.Log4j2
import org.apache.logging.log4j.Logger
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is
import static org.junit.Assert.assertThat

@Log4j2
class MuleDeployPropertiesTest implements // not inheriting basejunit test because we don't want to start/stop for this test
        BaseMuleGroovyTrait {
    @Override
    File getMuleArtifactPath() {
        new File('src/test/resources/mule-deploy-props-test-artifact.json')
    }

    @Override
    Logger getLogger() {
        log
    }

    @Override
    Map<String, String> getConfigResourceSubstitutes() {
        [
                'simple_json_test.xml': 'java_test.xml',
                'foo.xml'             : null,
                'bar.xml'             : null
        ]
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
